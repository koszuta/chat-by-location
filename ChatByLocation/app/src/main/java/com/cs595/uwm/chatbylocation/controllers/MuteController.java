package com.cs595.uwm.chatbylocation.controllers;

import android.content.Context;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by Jason on 4/14/2017.
 */

public class MuteController {


    public static final String MUTE_FILENAME = "MutedUsersFile";

    public void addUserToMuteList(Context context, String username) {
        String addEndLine = username + "\n";
        FileOutputStream oStream;

        try {
            File file = new File(MUTE_FILENAME);
            if(file.exists()) {
                oStream = context.openFileOutput(MUTE_FILENAME, Context.MODE_APPEND);
            }
            else {
                oStream = context.openFileOutput(MUTE_FILENAME, Context.MODE_PRIVATE);
            }
            oStream.write(username.getBytes());
            oStream.close();
        }
        catch (Exception e) {
            //TODO
        }
    }

    public void removeUserFromMuteList(Context context, String username) {
        FileInputStream iStream = null;
        String fullFile = "";

        try {
            File file = new File(MUTE_FILENAME);
            if (file.exists()) {
                iStream = context.openFileInput(MUTE_FILENAME);

            }
            //store contents in fullFile and replaces username with empty string
            int nextByte;
            char c;
            while ((nextByte = iStream.read()) != -1) {
                c = (char) nextByte;
                fullFile += c;
            }
            //replace old mute file
            String newMuteFileString = fullFile.replace(username, "");
            FileOutputStream oStream = context.openFileOutput(MUTE_FILENAME, Context.MODE_PRIVATE);

            oStream.write(newMuteFileString.getBytes());
            oStream.close();

        }
        catch (Exception e) {
            //TODO
        }
    }

    public boolean isUserMuted(Context context, String username) {
        FileInputStream iStream = null;
        boolean isMuted = false;
        boolean isOpenSuccessful = false;
        String fullFile = "";

        try {
            File file = new File(MUTE_FILENAME);
            if (file.exists()) {
                iStream = context.openFileInput(MUTE_FILENAME);
                isOpenSuccessful = true;
            }
            //store contents in fullFile and substring search for username
            if(isOpenSuccessful) {
                int nextByte;
                char c;
                while ((nextByte = iStream.read()) != -1) {
                    c = (char) nextByte;
                    fullFile += c;
                }
                if (fullFile.indexOf(username) != -1) {
                    isMuted = true;
                }
            }

        }
        catch (Exception e) {
            //TODO
        }
        return isMuted;
    }

    public void onMuteClick(View v) {

    }
}
