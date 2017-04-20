package com.cs595.uwm.chatbylocation.objModel;

import java.util.Date;

/**
 * Created by Jason on 2/27/2017.
 */

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private int    messageColor;
    private long messageTime;

    public ChatMessage(String messageText, String messageUser, int messageColor) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageColor = messageColor;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public int getMessageColor() { return messageColor; }

    public void setMessageColor(int messageColor) {
        this.messageColor = messageColor;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
