package com.cs595.uwm.chatbylocation.service;

import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {

    private static String currentRoomID;

    public static String getCurrentRoomID() {
        return currentRoomID;
    }

    public static void createUser() {

        getCurrentUserReference().child("currentRoomID").setValue("");
        trace("created user");

    }

    public static void setUserRoom(String roomID) { // roomid = null removes from room

        trace("setUserRoom:" + roomID);

        if (roomID == null) {
            getCurrentUserReference().child("removeFrom").setValue(currentRoomID);
        } else {
            getCurrentUserReference().child("currentRoomID").setValue(roomID);
        }

    }

    public static String getUserUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getDisplayName();
        return null;

    }

    public static String createRoom(String ownerID, String name, String longg, String lat,
                                    int rad, String password) {

        DatabaseReference roomIDRef = getRoomIdentityReference();
        String roomID = roomIDRef.push().getKey();

        DatabaseReference roomIDInst = roomIDRef.child(roomID);
        roomIDInst.child("ownerID").setValue(ownerID);
        roomIDInst.child("name").setValue(name);
        roomIDInst.child("longg").setValue(longg);
        roomIDInst.child("lat").setValue(lat);
        roomIDInst.child("rad").setValue(rad);
        roomIDInst.child("password").setValue(password);

        return roomID;

    }

    public static void sendChatMessage(ChatMessage chatMessage, String roomID) {
        getRoomMessagesReference().child(roomID).push().setValue(chatMessage);
    }

    public static void listenToRoomChange() {

        ValueEventListener roomIDListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomID = String.valueOf(dataSnapshot.getValue());
                trace("roomIDListener sees roomID: " + roomID);
                currentRoomID = roomID;

                if (roomID == null || roomID.equals("")) return;

                getRoomUsersReference().child(roomID).child(getUserID()).setValue(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        ValueEventListener removeFromListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String removeFrom = String.valueOf(dataSnapshot.getValue());
                trace("removeFromListener sees removeFrom: " + removeFrom);

                if (!(removeFrom == null || removeFrom.equals(""))) {
                    getRoomUsersReference().child(removeFrom).child(getUserID()).setValue(null);
                    getCurrentUserReference().child("currentRoomID").setValue("");
                    getCurrentUserReference().child("removeFrom").setValue("");
                    trace("removeFromListener has removed the user from their room");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        DatabaseReference userRef = getCurrentUserReference();
        userRef.child("currentRoomID").addValueEventListener(roomIDListener);
        userRef.child("removeFrom").addValueEventListener(removeFromListener);

    }

    public static String getUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getUid();
        return null;
    }

    public static DatabaseReference getCurrentUserReference() {
        String userID = getUserID();
        if (userID == null) userID = "-";
        return FirebaseDatabase.getInstance().getReference().child("users").child(userID);
    }

    public static DatabaseReference getRoomUsersReference() {
        return FirebaseDatabase.getInstance().getReference().child("roomUsers");
    }

    public static DatabaseReference getRoomIdentityReference() {
        return FirebaseDatabase.getInstance().getReference().child("roomIdentity");
    }

    public static DatabaseReference getRoomMessagesReference() {
        return FirebaseDatabase.getInstance().getReference().child("roomMessages");
    }

    public static void trace(String message) {
        System.out.println("Database >> " + message); //todo android logger
    }
}
