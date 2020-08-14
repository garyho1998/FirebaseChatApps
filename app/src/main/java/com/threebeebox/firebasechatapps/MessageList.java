package com.threebeebox.firebasechatapps;

import java.util.List;

public class MessageList{
    List<ChatMessage> messages;

    MessageList(){}
    MessageList(List<ChatMessage> messages){
        this.messages = messages;
    }
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
    public List<ChatMessage> getMessages() {
        return messages;
    }

    @Override
    public String toString(){
        String result = "";
        if(messages!=null){
            for(ChatMessage message : messages){
                result += message.getMessageID();
            }
        }else{
            return "NULL MessageList";
        }
        return messages.toString();
    };

}
