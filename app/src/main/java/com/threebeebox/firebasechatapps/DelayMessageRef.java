package com.threebeebox.firebasechatapps;

public class DelayMessageRef {
    private String ref;
    private String type;

    DelayMessageRef(){

    }

    DelayMessageRef(String ref, String type){
        this.ref = ref;
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
