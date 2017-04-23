package com.cs595.uwm.chatbylocation.objModel;

import android.content.Context;
import android.content.Intent;

import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.view.SelectActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Jason on 4/14/2017.
 */

public class RoomBan {
    //Ban object for a specific room for current user
    private String roomID;
    private String userID;



    boolean isCurrentUserBanned;
    private ChildEventListener roomBanListener;

    public RoomBan(String roomID) {
        this.roomID = roomID;
        roomBanListener = createBanListenerForSpecificRoom();
        userID = Database.getUserId();
        isCurrentUserBanned = false;
    }

    public void setCurrentUserBanned(boolean currentUserBanned) {
        isCurrentUserBanned = currentUserBanned;
    }
    public boolean isCurrentUserBanned() {
        return isCurrentUserBanned;
    }
    //part of the constructor of this object - to create a listener
    private ChildEventListener createBanListenerForSpecificRoom() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("roomUsers").child(roomID);
        return dRef.child("bannedUsers").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getKey() != null && dataSnapshot.getKey().equals(Database.getUserId())) {
                    Boolean isBanned = (Boolean) dataSnapshot.getValue();
                    if (isBanned) {
                        isCurrentUserBanned = true;
                    } else {
                        isCurrentUserBanned = false;
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
