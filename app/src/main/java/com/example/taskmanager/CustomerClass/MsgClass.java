package com.example.taskmanager.CustomerClass;

public class MsgClass {
    String title, msg, documentId;
    long timeStamp;

    public MsgClass() {
        // empty constructor for Firebase
    }

    public MsgClass(String title, String msg, String documentId, long timeStamp) {
        this.title = title;
        this.msg = msg;
        this.documentId = documentId;
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}