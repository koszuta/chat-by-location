package com.cs595.uwm.chatbylocation.objModel;

/**
 * Created by Jason on 4/3/2017.
 */

public class UserIdentity {
    private String name;
    private String icon;

    public UserIdentity(){
    }

    public UserIdentity(String name, int icon) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
