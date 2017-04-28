package com.cs595.uwm.chatbylocation.objModel;

/**
 * Created by Jason on 4/3/2017.
 */

public class UserIdentity {
    private String icon;
    private String username;
    private String currentRoomID;
    private String removeFrom;
    private Long roomJoinTime;

    public UserIdentity(){
        icon = UserIcon.NONE;
    }

    public UserIdentity(String username, String icon) {
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

    public Long getRoomJoinTime() {
        return roomJoinTime;
    }

    public void setRoomJoinTime(Long roomJoinTime) {
        this.roomJoinTime = roomJoinTime;
    }
}
