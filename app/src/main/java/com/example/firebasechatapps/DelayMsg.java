package com.example.firebasechatapps;

public class DelayMsg {
    public String message, displayDate, displayTime, messageID;
//    public int display_day, display_month, display_yr;
//    public int display_hr, display_minute, display_sec;

    public DelayMsg()
    {
        message = "String: A default delay message";
        displayTime = "String: A default display time";

    }

    public DelayMsg(String messageID, String message, String displayTime, String displayDate) {
        this.messageID = messageID;
        this.message = message;
        this.displayTime = displayTime;
        this.displayDate = displayDate;

    }

    public  String getId() { return messageID; }

    public String getMessage() { return message; }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDisplayTime() { return displayTime; }

    public void setDisplayTime(String displayTime) { this.displayTime = displayTime; }

    public String getDisplayDate() { return displayDate; }

    public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }




}
