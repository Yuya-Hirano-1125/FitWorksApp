package com.example.demo.dto;

public class ChatRequest {
    private String text;
    private String userName;

    // コンストラクタ (省略可)
    public ChatRequest() {}
    public ChatRequest(String text, String userName) {
        this.text = text;
        this.userName = userName;
    }

    // Getter and Setter (必須)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}