package com.example.asasfans.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author LEN5010
 * @description 全局视频播放模式持久化，保存 App 内播放或跳转 B 站的用户选择。
 */
public final class VideoPlaybackModeStore {
    public static final String MODE_APP = "app";
    public static final String MODE_EXTERNAL = "external";

    private static final String PREFS_NAME = "video_playback_mode";
    private static final String KEY_MODE = "mode";

    private VideoPlaybackModeStore() {
    }

    /**
     * 读取持久化播放模式，异常或未知值统一回退到 App 内播放。
     */
    public static String getMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return normalizeMode(preferences.getString(KEY_MODE, MODE_APP));
    }

    public static boolean isExternalMode(Context context) {
        return MODE_EXTERNAL.equals(getMode(context));
    }

    /**
     * 侧边栏播放模式按钮使用二态切换，并立即写入本地偏好。
     */
    public static String toggle(Context context) {
        String nextMode = nextMode(getMode(context));
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_MODE, nextMode)
                .apply();
        return nextMode;
    }

    public static String normalizeMode(String mode) {
        return MODE_EXTERNAL.equals(mode) ? MODE_EXTERNAL : MODE_APP;
    }

    public static String nextMode(String mode) {
        return MODE_EXTERNAL.equals(normalizeMode(mode)) ? MODE_APP : MODE_EXTERNAL;
    }

    /**
     * 把播放模式转换成侧边栏菜单文案，保证 UI 和实际状态一致。
     */
    public static String menuTitle(String mode) {
        return MODE_EXTERNAL.equals(normalizeMode(mode)) ? "播放模式：跳转B站" : "播放模式：App播放";
    }
}
