package com.threebeebox.firebasechatapps;

public class Group {

    private String GroupName;
    private String image;
    private String id;

    public Group(){

    }
    public Group(String id, String GroupName) {
        this.GroupName = GroupName;
        this.id = id;
    }
    public String getID() {
        return id;
    }
    public String getGroupName() {
        return GroupName;
    }
    @Override
    public String toString() {
        return GroupName;
    }
}
