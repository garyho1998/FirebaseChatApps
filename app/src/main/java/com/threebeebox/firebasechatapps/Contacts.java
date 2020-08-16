package com.threebeebox.firebasechatapps;

public class Contacts {
    public String name;
    public String status;
    public String image;
    public String phoneNumber;

    public Contacts() {

    }

    public Contacts(String name, String status, String image, String phoneNumber) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.phoneNumber = phoneNumber;

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
}
