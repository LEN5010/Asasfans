package com.example.asasfans.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author LEN5010
 * @description 视频列表本地过滤规则，处理屏蔽词、屏蔽 Tag 和珈乐相关内容过滤。
 */
public class VideoListRules {
    public static final long CAROL_MID = 351609538L;

    private VideoListRules() {
    }

    /**
     * 屏蔽词是宽匹配，会命中标题、简介和分区名，不用于精确 Tag 判断。
     */
    public static boolean matchesBlackWord(String title, String desc, String tag, String tname, Iterable<String> words) {
        String haystack = normalize(title) + "\n" + normalize(desc) + "\n" + normalize(tname);
        for (String word : words) {
            String normalized = normalize(word);
            if (!normalized.isEmpty() && haystack.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 屏蔽 Tag 只做逗号分隔后的精确匹配，避免一个短词误伤整页视频。
     */
    public static boolean matchesBlackTag(String tag, Iterable<String> blockedTags) {
        Set<String> videoTags = new HashSet<>(parseTags(tag));
        if (videoTags.isEmpty()) {
            return false;
        }
        for (String blockedTag : blockedTags) {
            String normalized = normalize(blockedTag);
            if (!normalized.isEmpty() && videoTags.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 后端 tag 字段是逗号分隔字符串，先归一化再参与过滤和列表移除。
     */
    public static List<String> parseTags(String tag) {
        List<String> tags = new ArrayList<>();
        if (tag == null) {
            return tags;
        }
        String normalized = tag.replace("'", "");
        String[] parts = normalized.split(",");
        for (String part : parts) {
            String value = normalize(part);
            if (!value.isEmpty()) {
                tags.add(value);
            }
        }
        return tags;
    }

    /**
     * 本地订阅 UP 不额外拉流，只在当前批次中把订阅 UP 的视频排到前面。
     */
    public static int compareSubscribedUp(long leftMid, long rightMid, Set<Long> subscribedMids) {
        boolean leftSubscribed = subscribedMids.contains(leftMid);
        boolean rightSubscribed = subscribedMids.contains(rightMid);
        if (leftSubscribed == rightSubscribed) {
            return 0;
        }
        return leftSubscribed ? -1 : 1;
    }

    /**
     * 珈乐内容按固定 UID 和文本关键词统一过滤，应用于所有视频列表入口。
     */
    public static boolean isCarolRelated(long mid, String title, String desc, String tag, String ownerName) {
        if (mid == CAROL_MID) {
            return true;
        }
        String haystack = normalize(title) + "\n" + normalize(desc) + "\n" + normalize(tag) + "\n" + normalize(ownerName);
        return haystack.contains("珈乐") || haystack.contains("carol");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
