package com.cs595.uwm.chatbylocation.controllers;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.cs595.uwm.chatbylocation.service.Database;

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

    public static void addUserToMuteList(Context context, String username) {
        String addEndLine = username + "\n";
        FileOutputStream oStream;

        try {

            oStream = context.openFileOutput(MUTE_FILENAME, Context.MODE_APPEND);

            oStream.write(username.getBytes());
            oStream.close();
        }
        catch (Exception e) {
            Log.d("MuteController:", "Error adding user to mute list");
        }
    }

    public static void removeUserFromMuteList(Context context, String username) {
        FileInputStream iStream = null;
        String fullFile = "";

        try {

            iStream = context.openFileInput(MUTE_FILENAME);
            //store contents in fullFile
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
            Log.d("MuteController:", "Error removing user to mute list");
        }
    }

    public static boolean isMuted(Context context, String username) {
        FileInputStream iStream = null;
        boolean isMuted = false;
        String fullFile = "";

        try {
            iStream = context.openFileInput(MUTE_FILENAME);

            //store contents in fullFile and substring search for username
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
        catch (Exception e) {
            Log.d("MuteController:", "Error checking user in mute list");
        }
        return isMuted;
    }

    public static void printMuteList(Context context) {
        FileInputStream iStream = null;
        String fullFile = "";

        try {
            iStream = context.openFileInput(MUTE_FILENAME);
            //store contents in fullFile and replaces username with empty string
            int nextByte;
            char c;
            while ((nextByte = iStream.read()) != -1) {
                c = (char) nextByte;
                fullFile += c;
            }
            Log.d("MuteController List:", fullFile + "  END_OF_LOG");
        }
        catch (Exception e) {
            Log.d("MuteController:", "Could not print mute list");
        }
    }


}
