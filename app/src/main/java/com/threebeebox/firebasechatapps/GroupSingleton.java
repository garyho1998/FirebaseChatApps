package com.threebeebox.firebasechatapps;

public class GroupSingleton {

    private static GroupSingleton INSTANCE = null;
    public Boolean isAdmin = false;

    private GroupSingleton() {};

    public static GroupSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GroupSingleton();
        }
        return(INSTANCE);
    }
    // other instance methods can follow
}