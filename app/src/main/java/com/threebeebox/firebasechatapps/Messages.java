package com.threebeebox.firebasechatapps;

import androidx.annotation.Nullable;

public class Messages
{
    public String from, message, type, to, messageID, time, date, name;
    public Long timestamp;

    public Messages()
    {

    }

    public Messages(String from, String message, String type, String to, String messageID, String time, String date, String name, Long timestamp) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean isEqual = false;

        if (obj!=null && obj instanceof Messages && this.messageID!=null) {
            isEqual = (this.messageID.equals(((Messages) obj).getMessageID()));
        }

        return isEqual;
    }

//    @Override
//    public int hashCode() {
//        return Integer.parseInt(this.messageID);
//    }
}
