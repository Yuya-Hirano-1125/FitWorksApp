package com.example.demo.dto;

public class Message {
    private String sender; // "user" または "ai"
    private String text;
    
    public Message() {}
    
    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }
    
    public String getSender() { return sender; }
    public String getText() { return text; }
    
    public void setSender(String sender) { this.sender = sender; }
    public void setText(String text) { this.text = text; }
}
