package com.threebeebox.firebasechatapps;

public class DelayMsg extends Messages{
    public String displayDate, displayTime;
    public String from, message, type, to, messageID, time, date, name;
    public Long timestamp , displayTimestamp;
    public DelayMsg()
    {
    }

    public DelayMsg(Long displayTimestamp, String displayTime, String displayDate,
                    String from, String message, String type, String to, String messageID, String time, String date, String name, Long timestamp) {
        super(from, message, type, to, messageID, time, date, name, timestamp);
        this.displayTime = displayTime;
        this.displayDate = displayDate;
        this.displayTimestamp = displayTimestamp;
    }
    public Long getDisplayTimestamp() {
        return displayTimestamp;
    }

    public void setDisplayTimestamp(Long displayTimestamp) {
        this.displayTimestamp = displayTimestamp;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public Messages toParent(){
        return new Messages(super.from, super.message, super.type, super.to, super.messageID, this.displayTime, this.displayDate, super.name, this.displayTimestamp);
    };
}
