package com.cs595.uwm.chatbylocation.controllers;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ToggleButton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jason on 4/14/2017.
 */

public class MuteController {

    public static final String MUTE_FILENAME = "MutedUsersFile";
    private static final char NAME_SEPARATOR = '\u0007';
    private static final char LINE_SEPARATOR = '\n';

    private static final Map<String, Long> muteList = new HashMap<>();

    public static void initMuteList(Context context) {
        try {
            // Open file for reading
            FileInputStream fis = context.openFileInput(MUTE_FILENAME);
            // Put file contents in string
            String list = "";
            int nextByte;
            while ((nextByte = fis.read()) != -1) {
                list += (char) nextByte;
            }

            // Go through each entry in list and add to muteList
            int start = 0;
            while ((start = list.indexOf(NAME_SEPARATOR, start)) != -1) {
                int mid = list.indexOf(NAME_SEPARATOR, start + 1);
                int end = list.indexOf(LINE_SEPARATOR, mid);
                // Clear the line
                String username = list.substring(start + 1, mid);
                Long time = Long.valueOf(list.substring(mid + 1, end));
                muteList.put(username, time);
                start = end;
            }
        } catch (IOException e) {
            Log.d("MuteController:", "Error initializing mute list: " + e.getLocalizedMessage());
        }
    }

    public static void muteUser(final String username, final Context context) {
        // Format username and time for file
        long muteTime = System.currentTimeMillis();
        String muteLine = NAME_SEPARATOR + username + NAME_SEPARATOR + muteTime + LINE_SEPARATOR;
        FileOutputStream fos;
        try {
            // Open file for writing
            fos = context.openFileOutput(MUTE_FILENAME, Context.MODE_APPEND);
            // Write formatted line to mute file
            fos.write(muteLine.getBytes());
            muteList.put(username, muteTime);
            fos.close();

        } catch (IOException e) {
            Log.d("MuteController:", "Error muting user: " + e.getLocalizedMessage());
        }
    }

    public static void unmuteUser(final String username, final Context context) {
        try {
            // Open file for reading
            FileInputStream fis = context.openFileInput(MUTE_FILENAME);
            // Put file contents in string
            String list = "";
            int nextByte;
            while ((nextByte = fis.read()) != -1) {
                list += (char) nextByte;
            }

            // Find the lines in the file corresponding to the username
            int start;
            while ((start = list.indexOf(NAME_SEPARATOR + username + NAME_SEPARATOR)) != -1) {
                int end = list.indexOf(LINE_SEPARATOR, start);
                // Clear the line
                String beforeLine = list.substring(0, start);
                String afterLine = list.substring(end, list.length());
                list = beforeLine + afterLine;
            }

            // Open file and write in updated list
            FileOutputStream fos = context.openFileOutput(MUTE_FILENAME, Context.MODE_PRIVATE);
            fos.write(list.getBytes());
            muteList.remove(username);
            fos.close();
        } catch (IOException e) {
            Log.d("MuteController:", "Error unmuting user: " + e.getLocalizedMessage());
        }
    }

    /**
     * Checks whether a specified user is muted returning the time the user was muted in milliseconds.
     * -1 is returned if the specified user is not currently muted.
     *
     * @param username of muted user
     * @return time user was muted in milliseconds, otherwise -1
     */
    public static long isMuted(final String username) {
        return (muteList.containsKey(username)) ? (long) muteList.get(username) : -1;
    }

    public static void onMuteClick(final String username, Context context) {
        if (MuteController.isMuted(username) != -1) {
            MuteController.unmuteUser(username, context);
        } else {
            MuteController.muteUser(username, context);
        }
    }

    public static void toggleMuteButton(ToggleButton button, String username) {
        if(MuteController.isMuted(username) != -1) {
            trace("Toggled mute button on for " + username);
            button.setChecked(true);
        }
    }


    public static void printMuteList(final Context context) {
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
        } catch (IOException e) {
            Log.d("MuteController:", "Could not print mute list: " + e.getLocalizedMessage());
        }
    }

    private static void trace(String message) {
        System.out.println("MuteController >> " + message);
    }
}
