package com.cs595.uwm.chatbylocation.controllers;

import android.content.Context;

import com.cs595.uwm.chatbylocation.objModel.RoomBan;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by Jason on 4/22/2017.
 */

public class BanController {
    //stores a RoomBan object for each room that is populated for the user in selectActivity
    private static HashMap<String,RoomBan> userBannedRoomList = new HashMap< String,RoomBan>();

    public static void addRoom(String roomID) {
        userBannedRoomList.put(roomID, new RoomBan(roomID));
    }

    public static RoomBan getRoomBanForSpecificRoom(String roomID) {
        return userBannedRoomList.get(roomID);
    }

    public static void addToRoomBanList(Context context, String userID, String roomID) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("roomUsers");
        dRef.child(roomID).child("bannedUsers").child(userID).setValue(true);
        //immediately ban current user, for demo purposes
        if(Database.getUserId().equals(userID) ) {
            userBannedRoomList.put(roomID, new RoomBan(roomID));
            userBannedRoomList.get(roomID).setCurrentUserBanned(true);
        }

    }

    public static void removeFromRoomBanList(Context context, String userID, String roomID) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("roomUsers").child(roomID);
        dRef.child("bannedUsers").child(userID).setValue(false);
    }

    public static boolean isCurrentUserBanned(String roomID) {
        if(getRoomBanForSpecificRoom(roomID) == null) {
            return false;
        }
        else {
            return getRoomBanForSpecificRoom(roomID).isCurrentUserBanned();
        }
    }


}
