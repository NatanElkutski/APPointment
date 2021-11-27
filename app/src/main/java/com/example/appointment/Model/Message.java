package com.example.appointment.Model;

import com.google.firebase.Timestamp;

public class Message {
    private String senderName,time,message,subject;
    private Boolean seen;
    private Boolean expandable;
    private com.google.firebase.Timestamp timestamp;

    public Message() {
    }

    public Message(String senderName,String subject, String message,String time, Timestamp timestamp) {
        this.senderName = senderName;
        this.time = time;
        this.message = message;
        this.seen = false;
        this.subject = subject;
        this.expandable = false;
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean getExpandable() {
        return expandable;
    }

    public void setExpandable(Boolean expandable) {
        this.expandable = expandable;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
