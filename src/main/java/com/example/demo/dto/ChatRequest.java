package com.example.demo.dto;
public class ChatRequest {
    private String text; // ユーザーメッセージ
    private String userName; // ユーザー名
    
    // 省略: コンストラクタ、ゲッター、セッター
    public String getText() { return text; }
    public String getUserName() { return userName; }
}