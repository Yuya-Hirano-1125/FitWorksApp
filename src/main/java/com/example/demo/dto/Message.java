package com.example.demo.dto;

public class Message {
    private String sender; // "user" or "ai"
    private String text;

    // コンストラクタ (省略可)
    public Message() {}
    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    // Getter and Setter (必須)
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}