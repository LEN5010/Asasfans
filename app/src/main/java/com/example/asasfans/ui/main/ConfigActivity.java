package com.example.asasfans.ui.main;

import static com.example.asasfans.TestActivity.floatHelper;
import static com.example.asasfans.TestActivity.getVersionCode;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.asasfans.AsApplication;
import com.example.asasfans.R;
import com.example.asasfans.TestActivity;
import com.example.asasfans.data.GithubVersionBean;
import com.example.asasfans.util.ACache;
import com.google.gson.Gson;
import com.google.android.material.materialswitch.MaterialSwitch;
import coil.Coil;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int GET_DATA_SUCCESS = 1;
    private static final int NETWORK_ERROR = 2;
    private static final String LATEST_RELEASE_PAGE = "https://github.com/LEN5010/Asasfans-Next/releases/latest";
    private ConstraintLayout config_check_version;
    private ImageView config_check_version_icon;
    private ConstraintLayout config_contract_us;
    private ConstraintLayout config_clear_pic_cache;
    private ConstraintLayout config_clear_web_cache;
    private LinearLayout config;
    private TextView config_check_version_number;
    private MaterialSwitch config_floating_ball_switch;
    private View emptyView;
    private String latestVersion = "https://api.github.com/repos/LEN5010/Asasfans-Next/releases/latest";
    private RotateAnimation mRotateAnimation;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        config_check_version = findViewById(R.id.config_check_version);
        config_check_version_icon = findViewById(R.id.config_check_version_icon);
        config_contract_us = findViewById(R.id.config_contract_us);
        config_clear_pic_cache = findViewById(R.id.config_clear_pic_cache);
        config_clear_web_cache = findViewById(R.id.config_clear_web_cache);
        config = findViewById(R.id.config);
        emptyView = findViewById(R.id.emptyViewConfig);
        config_floating_ball_switch = findViewById(R.id.config_floating_ball_switch);

        config_check_version_number = findViewById(R.id.config_check_version_number);

        config_check_version_number.setText("当前版本号:" + getVersionName(ConfigActivity.this));

        config_check_version.setOnClickListener(this::onClick);
        config_contract_us.setOnClickListener(this::onClick);
        config_clear_pic_cache.setOnClickListener(this::onClick);
        config_clear_web_cache.setOnClickListener(this::onClick);
        config.setOnClickListener(this::onClick);

        ACache aCache = ACache.get(this);
        String tmpACache =  aCache.getAsString("isShowFloatingBall"); // yes or no
        String isNoLongerShowFloatingBall =  aCache.getAsString("isNoLongerShowFloatingBall"); // yes or no
        if (tmpACache == null || isNoLongerShowFloatingBall == null){
            config_floating_ball_switch.setChecked(false);
            aCache.put("isShowFloatingBall", "no");
        } else if(tmpACache.equals("yes") && isNoLongerShowFloatingBall.equals("no")){
            config_floating_ball_switch.setChecked(true);
        }else {
            config_floating_ball_switch.setChecked(false);
        }

        config_floating_ball_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ACache aCache = ACache.get(ConfigActivity.this);
                if (b){
                    aCache.put("isShowFloatingBall", "yes");
                    if (!Settings.canDrawOverlays(ConfigActivity.this)){
                        aCache.put("isNoLongerShowFloatingBall", "no");
                        Toast.makeText(ConfigActivity.this, "需要手动开启悬浮窗权限才能使用悬浮球，回到主页可再次开启", Toast.LENGTH_SHORT).show();
                    }
                    if (floatHelper != null) {
                        floatHelper.show();
                    }
                }else {
                    aCache.put("isShowFloatingBall", "no");
                    if (floatHelper != null) {
                        floatHelper.dismiss();
                    }
                }
            }
        });
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AsApplication.Companion.getStatusBarHeight());
        emptyView.setLayoutParams(layoutParams);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (floatHelper != null) {
            floatHelper.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ACache aCache = ACache.get(this);
        String tmpACache =  aCache.getAsString("isShowFloatingBall"); // yes or no
        if (tmpACache == null){
            if (floatHelper != null) {
                floatHelper.show();
            }
//            Toast.makeText(TestActivity.this, "悬浮球默认打开哦，可以在设置关闭", Toast.LENGTH_SHORT).show();
        }else if (tmpACache.equals("yes")){
            if (floatHelper != null) {
                floatHelper.show();
            }
        }else if (tmpACache.equals("no")){

        }
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.config_check_version) {
            if (mRotateAnimation == null) {
                mRotateAnimation = new RotateAnimation(0, 360,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                mRotateAnimation.setDuration(800);
                mRotateAnimation.setRepeatCount(-1);
            }
            config_check_version_icon.setAnimation(mRotateAnimation);
            config_check_version_icon.startAnimation(mRotateAnimation);
            new Thread(networkTask).start();
        } else if (id == R.id.config) {
            ConfigActivity.this.finish();
        } else if (id == R.id.config_contract_us) {
            Intent intentContractUs = new Intent();
            intentContractUs.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://github.com/LEN5010/Asasfans-Next/issues");
            intentContractUs.setData(content_url);
            startActivity(intentContractUs);
        } else if (id == R.id.config_clear_pic_cache) {
            Coil.imageLoader(ConfigActivity.this).getDiskCache().clear();
            Coil.imageLoader(ConfigActivity.this).getMemoryCache().clear();
            Toast.makeText(this, "清除图片缓存成功", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.config_clear_web_cache) {
            new WebView(ConfigActivity.this).clearCache(true);
            Toast.makeText(this, "清除WEB缓存成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * get App versionName
     * @param context
     * @return
     */
    public String getVersionName(Context context){
        PackageManager packageManager=context.getPackageManager();
        PackageInfo packageInfo;
        String versionName="";
        try {
            packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
            versionName=packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("latestVersion", "");
            Log.i("latestVersion", "请求结果为-->" + val);
            if (msg.what == GET_DATA_SUCCESS){
                if (val != null && val.startsWith("{\"url\"")) {
                    handleLatestReleaseJson(val);
                }else {
                    Toast.makeText(ConfigActivity.this, "403，请手动对比当前与最新版本号", Toast.LENGTH_SHORT).show();
                    openReleaseUrl(null);
                }
            }else {
                Toast.makeText(ConfigActivity.this, "网络错误，版本号获取失败", Toast.LENGTH_SHORT).show();
            }
            config_check_version_icon.clearAnimation();
        }
    };

    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            // TODO
            // 在这里进行 http request.网络请求相关操作
            OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url(latestVersion)
                    .get().build();
            Call call = client.newCall(request);
            try (Response response = call.execute()) {
                msg.what = GET_DATA_SUCCESS;
                data.putString("latestVersion", response.body() == null ? "" : response.body().string());

            } catch (Exception e) {
                e.printStackTrace();
                msg.what = NETWORK_ERROR;
                data.putString("latestVersion", "");
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void handleLatestReleaseJson(String val) {
        try {
            GithubVersionBean githubVersionBean = new Gson().fromJson(val, GithubVersionBean.class);
            int versionCode = parseReleaseVersionCode(githubVersionBean == null ? null : githubVersionBean.getTag_name());
            if (versionCode > getVersionCode(ConfigActivity.this)) {
                showUpgradeDialog(githubVersionBean);
            } else {
                Toast.makeText(ConfigActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.w("latestVersion", "Failed to parse latest release", e);
            Toast.makeText(ConfigActivity.this, "版本信息解析失败，请手动查看发布页", Toast.LENGTH_SHORT).show();
            openReleaseUrl(null);
        }
    }

    private void showUpgradeDialog(GithubVersionBean githubVersionBean) {
        DialogPlus dialog = DialogPlus.newDialog(ConfigActivity.this)
                .setContentHolder(new ViewHolder(R.layout.dialog_upgrade))
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setCancelable(true)
                .setContentBackgroundResource(R.color.transparent)
                .setGravity(Gravity.CENTER)
                .create();
        View dialogView = dialog.getHolderView();
        TextView content = dialogView.findViewById(R.id.upgrade_content);
        TextView cancel = dialogView.findViewById(R.id.close);
        TextView confirm = dialogView.findViewById(R.id.upgrade);

        content.setText(githubVersionBean.getBody());

        confirm.setOnClickListener(view -> {
            openReleaseUrl(githubVersionBean.getHtml_url());
            dialog.dismiss();
        });
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private int parseReleaseVersionCode(@Nullable String tagName) {
        if (tagName == null) {
            return -1;
        }
        String normalized = tagName.startsWith("v") ? tagName.substring(1) : tagName;
        String[] versionCodeString = normalized.split("\\.");
        if (versionCodeString.length < 3) {
            return -1;
        }
        try {
            return Integer.parseInt(versionCodeString[0]) * 100
                    + Integer.parseInt(versionCodeString[1]) * 10
                    + Integer.parseInt(versionCodeString[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void openReleaseUrl(@Nullable String releaseUrl) {
        String targetUrl = (releaseUrl == null || releaseUrl.isEmpty()) ? LATEST_RELEASE_PAGE : releaseUrl;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
        startActivity(intent);
    }
}
