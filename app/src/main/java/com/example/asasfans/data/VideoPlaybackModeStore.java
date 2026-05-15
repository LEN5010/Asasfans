package com.example.asasfans.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class VideoPlaybackModeStore {
    public static final String MODE_APP = "app";
    public static final String MODE_EXTERNAL = "external";

    private static final String PREFS_NAME = "video_playback_mode";
    private static final String KEY_MODE = "mode";

    private VideoPlaybackModeStore() {
    }

    public static String getMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return normalizeMode(preferences.getString(KEY_MODE, MODE_APP));
    }

    public static boolean isExternalMode(Context context) {
        return MODE_EXTERNAL.equals(getMode(context));
    }

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

    public static String menuTitle(String mode) {
        return MODE_EXTERNAL.equals(normalizeMode(mode)) ? "播放模式：跳转B站" : "播放模式：App播放";
    }
}
