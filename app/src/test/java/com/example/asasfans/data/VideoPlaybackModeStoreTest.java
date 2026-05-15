package com.example.asasfans.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoPlaybackModeStoreTest {
    @Test
    public void normalizeMode_defaultsToAppPlayback() {
        assertEquals(VideoPlaybackModeStore.MODE_APP, VideoPlaybackModeStore.normalizeMode(null));
        assertEquals(VideoPlaybackModeStore.MODE_APP, VideoPlaybackModeStore.normalizeMode("unknown"));
        assertEquals(VideoPlaybackModeStore.MODE_EXTERNAL, VideoPlaybackModeStore.normalizeMode(VideoPlaybackModeStore.MODE_EXTERNAL));
    }

    @Test
    public void nextMode_togglesBetweenAppAndExternal() {
        assertEquals(VideoPlaybackModeStore.MODE_EXTERNAL, VideoPlaybackModeStore.nextMode(VideoPlaybackModeStore.MODE_APP));
        assertEquals(VideoPlaybackModeStore.MODE_APP, VideoPlaybackModeStore.nextMode(VideoPlaybackModeStore.MODE_EXTERNAL));
    }
}
