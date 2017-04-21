package com.cs595.uwm.chatbylocation.service;

import android.util.Log;

import com.cs595.uwm.chatbylocation.objModel.UserIdentity;

import java.util.ArrayList;

/**
 * Created by Jason on 4/16/2017.
 */

public class Registration {

    private static String lastEmail = "";

    private static String lastUsedName = "";

    public static String getLastEmail() {
        return lastEmail;
    }

    public static void setLastEmail(String lastEmail) {
        Registration.lastEmail = lastEmail;
    }

    public static String getLastUsedName() {
        return lastUsedName;
    }

    public static void setLastUsedName(String lastName) {
        Registration.lastUsedName = lastName;
    }

}
