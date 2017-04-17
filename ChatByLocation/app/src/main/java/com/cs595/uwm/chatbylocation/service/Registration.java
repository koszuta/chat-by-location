package com.cs595.uwm.chatbylocation.service;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Jason on 4/16/2017.
 */

public class Registration {


    public static ArrayList<String> usernames;

    public static ArrayList<String> getUsernames() {
        return usernames;
    }

    public static void setUsernames(ArrayList<String> usernames) {
        Registration.usernames = usernames;
    }

    public static int getNameCount(String username) {
        int counter = 0;
        Log.d("Registration", "Username count for registration file: " +usernames.size() );
        for(String user: usernames) {
            if(user.equals(username))
                counter++;
        }

        return counter;
    }
}
