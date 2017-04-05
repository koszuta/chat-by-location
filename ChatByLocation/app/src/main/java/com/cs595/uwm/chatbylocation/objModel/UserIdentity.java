package com.cs595.uwm.chatbylocation.objModel;

/**
 * Created by Jason on 4/3/2017.
 */

public class UserIdentity {
    private String name;
    private int icon;

    public UserIdentity(String name, int icon) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
