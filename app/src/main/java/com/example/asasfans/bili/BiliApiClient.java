package com.example.asasfans.bili;

import android.text.TextUtils;

import androidx.annotation.OptIn;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.common.util.UnstableApi;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author LEN5010
 * @description Bilibili HTTP 客户端，统一请求头、Cookie、JSON 解析和媒体数据源配置。
 */
public class BiliApiClient {
    public static final String WEB_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    public static final String BILI_REFERER = "https://www.bilibili.com/";

    private final OkHttpClient client;
    private final BiliCredentialStore credentialStore;
    private final Gson gson = new Gson();

    public BiliApiClient(BiliCredentialStore credentialStore) {
        this.credentialStore = credentialStore;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public <T> T get(String url, Class<T> type) throws IOException {
        return get(url, BILI_REFERER, type);
    }

    /**
     * 统一执行 Bilibili GET 请求，允许调用方按接口要求覆盖 Referer。
     */
    public <T> T get(String url, String referer, Class<T> type) throws IOException {
        Request request = applyHeaders(new Request.Builder().url(url), referer)
                .get()
                .build();
        return executeForJson(request, type);
    }

    /**
     * 统一执行表单 POST 请求，主要用于登录和后续需要 csrf 的接口。
     */
    public <T> T postForm(String url, FormBody formBody, Class<T> type) throws IOException {
        Request request = applyHeaders(new Request.Builder().url(url), BILI_REFERER)
                .post(formBody)
                .build();
        return executeForJson(request, type);
    }

    /**
     * 返回原始响应给需要读取 Set-Cookie 等响应头的调用方。
     */
    public Response executeRaw(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    /**
     * Bilibili Web 接口对 User-Agent、Referer 和 Cookie 较敏感，所有请求从这里补齐。
     */
    public Request.Builder applyHeaders(Request.Builder builder, String referer) {
        builder.header("User-Agent", WEB_USER_AGENT)
                .header("Referer", TextUtils.isEmpty(referer) ? BILI_REFERER : referer);
        String cookie = credentialStore.buildCookieHeader();
        if (!TextUtils.isEmpty(cookie)) {
            builder.header("Cookie", cookie);
        }
        return builder;
    }

    @OptIn(markerClass = UnstableApi.class)
    public OkHttpDataSource.Factory newMediaDataSourceFactory(String referer) {
        // 播放 CDN 链接也要带视频页 Referer，否则部分 m4s/mp4 会被拒绝。
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", WEB_USER_AGENT);
        headers.put("Referer", TextUtils.isEmpty(referer) ? BILI_REFERER : referer);
        String cookie = credentialStore.buildCookieHeader();
        if (!TextUtils.isEmpty(cookie)) {
            headers.put("Cookie", cookie);
        }
        return new OkHttpDataSource.Factory(client)
                .setDefaultRequestProperties(headers);
    }

    public static String appendQuery(String baseUrl, String query) {
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + query;
    }

    /**
     * 用 OkHttp 的结构化 URL 构造器避免手写 query 时漏转义。
     */
    public static HttpUrl.Builder urlBuilder(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        return httpUrl.newBuilder();
    }

    private <T> T executeForJson(Request request, Class<T> type) throws IOException {
        // response/body 必须在这里关闭，避免频繁刷新列表时泄漏连接。
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code());
            }
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }
            return gson.fromJson(response.body().string(), type);
        }
    }
}
