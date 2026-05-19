package com.example.asasfans.bili;

import java.io.IOException;

/**
 * @author LEN5010
 * @description 带错误码的 Bilibili API 异常，用于向 UI 传递接口失败原因。
 */
public class BiliException extends IOException {
    private final int code;

    public BiliException(int code, String message) {
        super(message == null || message.isEmpty() ? "Bilibili API error: " + code : message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
