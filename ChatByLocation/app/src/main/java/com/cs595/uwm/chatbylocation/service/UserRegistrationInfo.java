package com.cs595.uwm.chatbylocation.service;

/**
 * Created by Jason on 3/15/2017.
 */

import com.google.firebase.auth.FirebaseAuth;

/**
 * Note: Static class. Example use - UserRegistrationInfo.getInstance().getChatName()
 */
public class UserRegistrationInfo {
    private static UserRegistrationInfo regInfo;

    private UserRegistrationInfo() {
    }

    public static UserRegistrationInfo getInstance() {
        if(regInfo == null) {
            regInfo = new UserRegistrationInfo();
        }
        return regInfo;
    }

    /**
     * Sets the chat name (not real name) of the user
     * @param name new chat name
     * @return true if chat name was set, false otherwise
     *
     */
    public boolean setChatName(String name) {
        if(name.equals("") || !Database.getUsernameUnique(name)) return false;
        Database.setUserUsername(name);
        return true;
    }

    /**
     * Gets the real name of the user
     * @return real name
     */
    public String getUserName() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
