package com.threebeebox.firebasechatapps;

public class User {
    public String name;
    public String status;
    public String image;
    public String phoneNumber;
    public String deviceToken;
    public String uid;
    public String groups;
    public String userState;


    public User() {

    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getUserState() {
        return userState;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }

    public User(String name, String status, String image, String phoneNumber, String deviceToken, String uid, String userState, String groups) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.phoneNumber = phoneNumber;
        this.deviceToken = deviceToken;
        this.uid = uid;
        this.userState = userState;
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String image) {
        this.phoneNumber = phoneNumber;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public class GroupRef{
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        String id;
        String groupName;
    }

    public class UserState{
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        String date;
        String state;
        String time;
    }
}
