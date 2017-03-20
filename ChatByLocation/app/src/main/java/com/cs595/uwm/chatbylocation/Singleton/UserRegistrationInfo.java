package com.cs595.uwm.chatbylocation.Singleton;

/**
 * Created by Jason on 3/15/2017.
 */

import com.google.firebase.auth.FirebaseAuth;

/**
 * Note: Static class. Example use - UserRegistrationInfo.getInstance().getChatName()
 */
public class UserRegistrationInfo {
    private static UserRegistrationInfo regInfo;

    private String chatName;


    private UserRegistrationInfo() {
        chatName = "";
    }

    public static UserRegistrationInfo getInstance() {
        if(regInfo == null) {
            regInfo = new UserRegistrationInfo();
        }
        return regInfo;
    }

    /**
     * Gets the chat name (not real name) of the user
     * //TODO: Retrieve from database
     * @return chat name
     */
    public String getChatName() {
        return chatName;
    }

    /**
     * Sets the chat name (not real name) of the user
     * //TODO: Store value in database
     * @param name new chat name
     * @return true if chat name was set, false otherwise
     *
     */
    public boolean setChatName(String name) {
        boolean isSet = false;
        if(isUniqueName() && name != "") {
            chatName = name;
            isSet = true;
        }
        return isSet;
    }

    /**
     * Checks if chat name is unique
     * //TODO: implement method checking against database chatnames
     * @return true if chat name does not yet exist, false otherwise
     */
    private boolean isUniqueName() {
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
