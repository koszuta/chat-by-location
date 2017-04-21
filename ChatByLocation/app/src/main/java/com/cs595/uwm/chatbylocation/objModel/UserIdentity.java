package com.cs595.uwm.chatbylocation.objModel;

/**
 * Created by Jason on 4/3/2017.
 */

public class UserIdentity {
    private String name;
    private String icon;
    private String username;
    private int icon;
    private String currentRoomID;
    private String removeFrom;

    public UserIdentity(){
    }

    public UserIdentity(String username, int icon) {
        this.username = username;
        this.icon = icon;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCurrentRoomID() {
        return currentRoomID;
    }

    public void setCurrentRoomID(String currentRoomID) {
        this.currentRoomID = currentRoomID;
    }

    public String getRemoveFrom() {
        return removeFrom;
    }

    public void setRemoveFrom(String removeFrom) {
        this.removeFrom = removeFrom;
    }
}
