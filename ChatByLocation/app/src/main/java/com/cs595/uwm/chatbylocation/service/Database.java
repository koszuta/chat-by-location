package com.cs595.uwm.chatbylocation.service;

import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {

    public static void createUser(){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("currentRoomID").setValue(null);

    }

    public static void addUserToRoom(String roomID){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("currentRoomID").setValue(roomID);

        DatabaseReference roomUsersRef = FirebaseDatabase.getInstance().getReference().child("roomUsers").child(roomID);
        roomUsersRef.child("users").child(userID).setValue(true);

    }

    public static void removeUserFromRoom(){
        String userID = getUserID();

        DatabaseReference roomUsersRef = FirebaseDatabase.getInstance().getReference().child("roomUsers").child(getUserCurrentRoomID());
        roomUsersRef.child("users").child(userID).setValue(null);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("currentRoomID").setValue(null);

        //todo: if this user is the owner, transfer ownership

    }

    public static String getUserCurrentRoomID(){
        //todo: this won't work

        String userID = getUserID();

        final String[] roomID = new String[1];
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        roomID[0] = String.valueOf(snapshot.child("currentRoomID").getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });

        return roomID[0];
    }

    public static String getUserUsername(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getDisplayName();
        return null;

    }

    public static boolean getUsernameUnique(final String username){

        //todo

        return true;
    }

    public static String createRoom(String ownerID, String name, String longg, String lat,
                                  int rad, String password){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference roomIDRef = dbRef.child("roomIdentity");

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

    public static void sendChatMessage(ChatMessage chatMessage, String roomID){
        FirebaseDatabase.getInstance().getReference().child("roomMessages").child(roomID)
                .push().setValue(chatMessage);

    }

    private static String getUserID(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
