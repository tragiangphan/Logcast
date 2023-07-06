package com.programme.logcast;

public class LogItem {
    private String date;
    private String time;
    private String PID;
    private char tag;
    private String name;
    private String content;

    public LogItem() {
    }

    public LogItem(String date, String time, String PID, char tag, String name, String content) {
        this.date = date;
        this.time = time;
        this.PID = PID;
        this.tag = tag;
        this.name = name;
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPID() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public char getTag() {
        return tag;
    }

    public void setTag(char tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

