package com.example.firebasechatapps;

public class Contacts {
    public String name;
    public String status;
    public String image;
    public String contact;

    public Contacts(){

    }

    public Contacts(String name, String status, String image, String contact) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.contact = contact;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String image) {
        this.contact = contact;
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
