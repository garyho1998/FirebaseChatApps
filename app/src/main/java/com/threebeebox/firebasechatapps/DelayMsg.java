package com.threebeebox.firebasechatapps;

public class DelayMsg extends Messages{
    public String displayDate;
    public String displayTime;
    public String displayTimestamp;

    public DelayMsg()
    {
        message = "String: A default delay message";
        displayTime = "String: A default display time";

    }

    public DelayMsg(String from, String message, String type, String to, String messageID, String time, String date, String name, String displayTimestamp, String displayTime, String displayDate) {
        super(from, message, type, to, messageID, time, date, name);
        this.displayTime = displayTime;
        this.displayDate = displayDate;
        this.displayTimestamp = displayTimestamp;
    }

    public String getDisplayTime() { return displayTime; }

    public void setDisplayTime(String displayTime) { this.displayTime = displayTime; }

    public String getDisplayDate() { return displayDate; }

    public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }

    public String getDisplayTimestamp() {
        return displayTimestamp;
    }

    public void setDisplayTimestamp(String displayTimestamp) {
        this.displayTimestamp = displayTimestamp;
    }



}
