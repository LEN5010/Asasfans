package com.example.asasfans.ui.main.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.asasfans.ui.main.fragment.BlacklistTabsFragment;
import com.example.asasfans.ui.main.fragment.NullFragment;

/**
 * @author LEN5010
 * @description 名单管理分页 Adapter，负责屏蔽词、屏蔽 Tag、黑名单和订阅 UP 页面切换。
 */
public class BlacklistPagerAdapter extends FragmentStateAdapter {
    private static final String[] TAB_TITLES = new String[]{"屏蔽词", "屏蔽Tag", "黑名单UP", "视频黑名单", "订阅UP"};

    public BlacklistPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return BlacklistTabsFragment.newInstance("blackWord", "word", "word");
            case 1:
                return BlacklistTabsFragment.newInstance("blackTag", "tag", "tag");
            case 2:
                return BlacklistTabsFragment.newInstance("blackMid", "mid", "mid");
            case 3:
                return BlacklistTabsFragment.newInstance("blackBvid", "Title", "bvid");
            case 4:
                return BlacklistTabsFragment.newInstance("subscribedUp", "subscribedUp", "mid");
            default:
                return NullFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_TITLES.length;
    }

    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }
}
