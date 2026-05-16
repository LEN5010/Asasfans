package com.example.asasfans.ui.main.adapter;

import static com.example.asasfans.util.ViewUtilsKt.dip2px;
import static com.example.asasfans.util.ViewUtilsKt.setMargin;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asasfans.R;
import com.example.asasfans.data.AdvancedSearchDataBean;
import com.example.asasfans.data.DBOpenHelper;
import com.example.asasfans.data.VideoListRules;
import com.example.asasfans.data.VideoPlaybackModeStore;
import com.example.asasfans.ui.bili.BiliVideoDetailActivity;
import com.google.android.flexbox.FlexboxLayout;
import coil.Coil;
import coil.request.ImageRequest;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author akarinini
 * @description 视频列表 Adapter。
 */

public class PubdateVideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {
    private Context mContext;
    private final String PackageName = "tv.danmaku.bili";
    private List<AdvancedSearchDataBean.DataBean.ResultBean> resultBeans = new ArrayList<>();
    private DialogPlus dialog;
    private View dialogView;
    private List<CheckBox> checkBoxs = new ArrayList<CheckBox>();
    DBOpenHelper dbOpenHelper;
    SQLiteDatabase db;

    public PubdateVideoAdapter(Context context, List<AdvancedSearchDataBean.DataBean.ResultBean> resultBeans, int pageNums) {
        this.mContext = context;
        this.resultBeans = resultBeans;
        dbOpenHelper = new DBOpenHelper(context, "blackList.db", null, DBOpenHelper.DB_VERSION);
        db = dbOpenHelper.getWritableDatabase();
        initDialog();
    }

    public void closeSQL(){
        db.close();
        dbOpenHelper.close();
    }

    void initDialog(){
        dialog = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(R.layout.dialog_video_more))
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .create();
        dialogView = dialog.getHolderView();
    }

    public static String[] tagFormat(String tag){
        List<String> tags = VideoListRules.parseTags(tag);
        return tags.toArray(new String[0]);
    }
    public static String stampToDatetime(String s) {
        if(s.length() == 10){
            s=s+"000";
        }
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //如果它本来就是long类型的,则不用写这一步
        long lt = Long.valueOf(s);
//        Date date = new Date(lt * 1000);
        Date date = new Date(lt );
        res = simpleDateFormat.format(date);
        return res;
    }

    private void openVideoDetail(int position) {
        if (!isValidPosition(position)) {
            return;
        }
        AdvancedSearchDataBean.DataBean.ResultBean video = resultBeans.get(position);
        Intent intent = new Intent(mContext, BiliVideoDetailActivity.class);
        intent.putExtra(BiliVideoDetailActivity.EXTRA_BVID, video.getBvid());
        intent.putExtra(BiliVideoDetailActivity.EXTRA_TITLE, video.getTitle());
        intent.putExtra(BiliVideoDetailActivity.EXTRA_COVER, video.getPic());
        intent.putExtra(BiliVideoDetailActivity.EXTRA_OWNER, video.getName());
        mContext.startActivity(intent);
    }

    private void openVideo(int position) {
        if (VideoPlaybackModeStore.isExternalMode(mContext)) {
            openExternalVideo(position);
        } else {
            openVideoDetail(position);
        }
    }

    private void openExternalVideo(int position) {
        if (!isValidPosition(position)) {
            return;
        }
        String bvid = resultBeans.get(position).getBvid();
        try {
            Intent it = new Intent();
            it.setAction(Intent.ACTION_VIEW);
            it.setData(Uri.parse("bilibili://video/" + bvid));
            mContext.startActivity(it);
        } catch (Exception e) {
            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("bvid", bvid);
            cm.setPrimaryClip(mClipData);
            Toast.makeText(mContext, "没有找到或无法用bilibili打开，尝试采用浏览器打开", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri contentUrl = Uri.parse("https://www.bilibili.com/video/" + bvid);
            intent.setData(contentUrl);
            mContext.startActivity(intent);
        }
    }

    private void subscribeUp(int position) {
        if (!isValidPosition(position)) {
            return;
        }
        AdvancedSearchDataBean.DataBean.ResultBean video = resultBeans.get(position);
        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
        SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("mid", video.getMid());
            values.put("name", video.getName());
            values.put("face", video.getFace() == null ? "" : video.getFace());
            values.put("note", "");
            values.put("updatedAt", System.currentTimeMillis() / 1000);
            sqliteDatabase.insertWithOnConflict("subscribedUp", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Toast.makeText(mContext, "订阅UP成功", Toast.LENGTH_SHORT).show();
        } finally {
            sqliteDatabase.close();
            dbOpenHelper.close();
        }
    }

    private boolean isValidPosition(int position) {
        return position != RecyclerView.NO_POSITION && position >= 0 && position < resultBeans.size();
    }

    private void removeVideoAt(int position) {
        if (!isValidPosition(position)) {
            return;
        }
        resultBeans.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    private void removeVideosByMid(long mid) {
        int removedCount = 0;
        for (int i = resultBeans.size() - 1; i >= 0; i--) {
            if (resultBeans.get(i).getMid() == mid) {
                resultBeans.remove(i);
                removedCount++;
            }
        }
        if (removedCount > 0) {
            notifyDataSetChanged();
        }
    }

    private void removeVideosByTags(List<String> tags) {
        int removedCount = 0;
        for (int i = resultBeans.size() - 1; i >= 0; i--) {
            if (VideoListRules.matchesBlackTag(resultBeans.get(i).getTag(), tags)) {
                resultBeans.remove(i);
                removedCount++;
            }
        }
        if (removedCount > 0) {
            notifyDataSetChanged();
        }
    }


    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_recyclerview, parent,false);
        final VideoViewHolder videoViewHolder = new VideoViewHolder(view);
        videoViewHolder.videoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatButton blacklistVideo =  dialogView.findViewById(R.id.dialog_black_list);
                AppCompatButton blacklistAuthor =  dialogView.findViewById(R.id.dialog_black_list_add_author);
                AppCompatButton blacklistTag =  dialogView.findViewById(R.id.dialog_black_list_add_tag);
                AppCompatButton openBili = dialogView.findViewById(R.id.dialog_open_bili);
                AppCompatButton subscribeUpButton = dialogView.findViewById(R.id.dialog_subscribe_up);
                FlexboxLayout flexboxLayout = dialogView.findViewById(R.id.dialog_black_list_tag_flexbox);
                TextView videoUpdateTime = dialogView.findViewById(R.id.video_update_time);
                TextView dialog_black_list_video_desc = dialogView.findViewById(R.id.dialog_black_list_video_desc);

                videoUpdateTime.setText(stampToDatetime(String.valueOf(resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getPubdate())));
                dialog_black_list_video_desc.setText(resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getDesc());

                flexboxLayout.removeAllViews();
                checkBoxs.clear();
                String[] tagList = tagFormat(resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getTag());
                for (int i = 0; i < tagList.length; i++){
                    CheckBox checkBox = (CheckBox) LayoutInflater.from(mContext).inflate(R.layout.checkbox_tag, parent,false);

                    checkBox.setText(tagList[i]);
                    checkBoxs.add(checkBox);
                    //有些tag是空格，不加入布局
                    if (!checkBoxs.get(i).getText().equals("")) {
                        flexboxLayout.addView(checkBox);
                    }
                }
                blacklistVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("appCompatButton", "onClick");
                        int position = videoViewHolder.getBindingAdapterPosition();
                        if (!isValidPosition(position)) {
                            dialog.dismiss();
                            return;
                        }
                        AdvancedSearchDataBean.DataBean.ResultBean video = resultBeans.get(position);
                        try {
                            DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
                            SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("bvid", video.getBvid());
                            values.put("PicUrl", video.getPic());
                            values.put("Title", video.getTitle());
                            values.put("Duration", video.getDuration());
                            values.put("Author", video.getName());
                            values.put("ViewNum", video.getView());
                            values.put("LikeNum", video.getLike());
                            values.put("Tname", video.getTname());
                            sqliteDatabase.insertWithOnConflict("blackBvid", null, values, SQLiteDatabase.CONFLICT_IGNORE);

                            dbOpenHelper.close();
                            sqliteDatabase.close();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        removeVideoAt(position);
                        Toast.makeText(mContext,"屏蔽视频成功",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                openBili.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openExternalVideo(videoViewHolder.getBindingAdapterPosition());
                        dialog.dismiss();
                    }
                });
                subscribeUpButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PubdateVideoAdapter.this.subscribeUp(videoViewHolder.getBindingAdapterPosition());
                        dialog.dismiss();
                    }
                });
                blacklistAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = videoViewHolder.getBindingAdapterPosition();
                        if (!isValidPosition(position)) {
                            dialog.dismiss();
                            return;
                        }
                        long mid = resultBeans.get(position).getMid();
                        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
                        SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("mid", mid);
                        sqliteDatabase.insertWithOnConflict("blackMid", null, values, SQLiteDatabase.CONFLICT_IGNORE);

                        dbOpenHelper.close();
                        sqliteDatabase.close();

                        removeVideosByMid(mid);

                        dialog.dismiss();
                        Toast.makeText(mContext,"屏蔽UP主成功",Toast.LENGTH_SHORT).show();
                    }
                });
                blacklistTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
                        SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        List<String> tags = new ArrayList<>();

                        for (CheckBox checkBox : checkBoxs){
                            if (checkBox.isChecked()){
                                tags.add(checkBox.getText().toString());
                            }
                        }
                        if (tags.size() == 0){
                            Toast.makeText(mContext,"请选择至少一个TAG",Toast.LENGTH_SHORT).show();
                        }else {

                            for (String tmp : tags){
                                values.clear();
                                values.put("tag", tmp);
                                sqliteDatabase.insertWithOnConflict("blackTag", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                Log.i("blacklistTag", tmp);
                            }

                            Toast.makeText(mContext,"屏蔽TAG成功",Toast.LENGTH_SHORT).show();

                            removeVideosByTags(tags);

                            dialog.dismiss();
                        }
                        sqliteDatabase.close();
                        dbOpenHelper.close();
                    }
                });
                dialog.show();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideo(videoViewHolder.getBindingAdapterPosition());
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Toast.makeText(mContext,resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getBvid() + "已复制到剪贴板",Toast.LENGTH_SHORT).show();
//                //获取剪贴板管理器：
//                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData mClipData = ClipData.newPlainText("bvid", resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getBvid());
//                cm.setPrimaryClip(mClipData);
                AppCompatButton blacklistVideo =  dialogView.findViewById(R.id.dialog_black_list);
                AppCompatButton blacklistAuthor =  dialogView.findViewById(R.id.dialog_black_list_add_author);
                AppCompatButton blacklistTag =  dialogView.findViewById(R.id.dialog_black_list_add_tag);
                AppCompatButton openBili = dialogView.findViewById(R.id.dialog_open_bili);
                AppCompatButton subscribeUpButton = dialogView.findViewById(R.id.dialog_subscribe_up);
                FlexboxLayout flexboxLayout = dialogView.findViewById(R.id.dialog_black_list_tag_flexbox);
                TextView videoUpdateTime = dialogView.findViewById(R.id.video_update_time);
                TextView dialog_black_list_video_desc = dialogView.findViewById(R.id.dialog_black_list_video_desc);

                videoUpdateTime.setText(stampToDatetime(String.valueOf(resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getPubdate())));
                dialog_black_list_video_desc.setText(resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getDesc());

                flexboxLayout.removeAllViews();
                checkBoxs.clear();
                String[] tagList = tagFormat(resultBeans.get(videoViewHolder.getBindingAdapterPosition()).getTag());
                for (int i = 0; i < tagList.length; i++){
                    CheckBox checkBox = (CheckBox) LayoutInflater.from(mContext).inflate(R.layout.checkbox_tag, parent,false);

                    checkBox.setText(tagList[i]);
                    checkBoxs.add(checkBox);
                    //有些tag是空格，不加入布局
                    if (!checkBoxs.get(i).getText().equals("")) {
                        flexboxLayout.addView(checkBox);
                    }
                }
                blacklistVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("appCompatButton", "onClick");
                        int position = videoViewHolder.getBindingAdapterPosition();
                        if (!isValidPosition(position)) {
                            dialog.dismiss();
                            return;
                        }
                        AdvancedSearchDataBean.DataBean.ResultBean video = resultBeans.get(position);
                        try {
                            DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
                            SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("bvid", video.getBvid());
                            values.put("PicUrl", video.getPic());
                            values.put("Title", video.getTitle());
                            values.put("Duration", video.getDuration());
                            values.put("Author", video.getName());
                            values.put("ViewNum", video.getView());
                            values.put("LikeNum", video.getLike());
                            values.put("Tname", video.getTname());
                            sqliteDatabase.insertWithOnConflict("blackBvid", null, values, SQLiteDatabase.CONFLICT_IGNORE);

                            dbOpenHelper.close();
                            sqliteDatabase.close();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        removeVideoAt(position);
                        Toast.makeText(mContext,"屏蔽视频成功",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                openBili.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openExternalVideo(videoViewHolder.getBindingAdapterPosition());
                        dialog.dismiss();
                    }
                });
                subscribeUpButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PubdateVideoAdapter.this.subscribeUp(videoViewHolder.getBindingAdapterPosition());
                        dialog.dismiss();
                    }
                });
                blacklistAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = videoViewHolder.getBindingAdapterPosition();
                        if (!isValidPosition(position)) {
                            dialog.dismiss();
                            return;
                        }
                        long mid = resultBeans.get(position).getMid();
                        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
                        SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("mid", mid);
                        sqliteDatabase.insertWithOnConflict("blackMid", null, values, SQLiteDatabase.CONFLICT_IGNORE);

                        dbOpenHelper.close();
                        sqliteDatabase.close();

                        removeVideosByMid(mid);

                        dialog.dismiss();
                        Toast.makeText(mContext,"屏蔽UP主成功",Toast.LENGTH_SHORT).show();
                    }
                });
                blacklistTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext,"blackList.db",null,DBOpenHelper.DB_VERSION);
                        SQLiteDatabase sqliteDatabase = dbOpenHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        List<String> tags = new ArrayList<>();

                        for (CheckBox checkBox : checkBoxs){
                            if (checkBox.isChecked()){
                                tags.add(checkBox.getText().toString());
                            }
                        }
                        if (tags.size() == 0){
                            Toast.makeText(mContext,"请选择至少一个TAG",Toast.LENGTH_SHORT).show();
                        }else {

                            for (String tmp : tags){
                                values.clear();
                                values.put("tag", tmp);
                                sqliteDatabase.insertWithOnConflict("blackTag", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                Log.i("blacklistTag", tmp);
                            }

                            Toast.makeText(mContext,"屏蔽TAG成功",Toast.LENGTH_SHORT).show();

                            removeVideosByTags(tags);

                            dialog.dismiss();
                        }
                        sqliteDatabase.close();
                        dbOpenHelper.close();
                    }
                });
                dialog.show();
                return true;
            }
        });

        return videoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
//        holder.imageView.startAnimation(fadeIn);

        if (position == 0) {
            setMargin(holder.videoLayout, dip2px(10), dip2px(5), dip2px(10), 0);
        } else {
            setMargin(holder.videoLayout, dip2px(10), 0, dip2px(10), 0);
        }
        Log.i("onBindViewHolder", resultBeans.get(position).getTitle());
        holder.videoTitle.setText(resultBeans.get(position).getTitle());
        Coil.imageLoader(mContext).enqueue(new ImageRequest.Builder(mContext)
                .data(resultBeans.get(position).getPic() + "@480w_300h_1e_1c.jpg")
                .target(holder.imageView)
                .build());
        holder.videoAuthor.setText(resultBeans.get(position).getName());
        holder.videoDuration.setText(secondsToTime(Integer.valueOf(resultBeans.get(position).getDuration())));
        holder.videoLike.setText(viewNumFormat(resultBeans.get(position).getLike()));
        holder.videoView.setText(viewNumFormat(resultBeans.get(position).getView()));
        holder.videoTname.setText(resultBeans.get(position).getTname());

    }


    @Override
    public int getItemCount() {
        return resultBeans.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return resultBeans.get(position).hashCode();
    }




    /**
     * @description 视频时长为秒，更改显示
     * @param
     * @return
     * @author akari
     * @time 2022/2/27 10:39
     */
    private static String secondsToTime(int seconds){
        int h=seconds/3600;			//小时
        int m=(seconds%3600)/60;		//分钟
        int s=(seconds%3600)%60;		//秒
        if(h>0){
            return String.format(Locale.CHINA, "%02d:%02d:%02d", h, m, s);
        }
        return String.format(Locale.CHINA, "%02d:%02d", m, s);
    }

    /**
     * @description 更改播放量与点赞显示
     * @param
     * @return
     * @author akari
     * @time 2022/2/27 10:40
     */
    private static String viewNumFormat(int viewNum){
        if ((viewNum - 10000) < 0){
            return viewNum + "";
        }else {
            return String.format(Locale.CHINA, "%.1f万", viewNum / 10000.0);
        }
    }
}
