package com.threebeebox.firebasechatapps;

public class SwipeSingleton  {

    private static SwipeSingleton INSTANCE = null;
    public Boolean isSwipable = false;
    public Boolean isAdmin = false;

    private SwipeSingleton() {};

    public static SwipeSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SwipeSingleton();
        }
        return(INSTANCE);
    }
    // other instance methods can follow
}