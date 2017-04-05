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

    private static String currentRoomID;

    public static void createUser(){
        String userID = getUserID();

        FirebaseDatabase.getInstance().getReference().child("users")
                .child(userID).child("currentRoomID").setValue("");
        System.out.println("created user");

    }

    public static void setUserRoom(String roomID){ // roomid = null removes from room
        String userID = getUserID();

        if(roomID == null){
            FirebaseDatabase.getInstance().getReference().child("users").child(userID)
                    .child("removeFrom").setValue(currentRoomID);
        } else {
            FirebaseDatabase.getInstance().getReference().child("users").child(userID)
                    .child("currentRoomID").setValue(roomID);
        }

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) return user.getUid();
        return null;
    }

    public static void listenToRoomChange(){

        ValueEventListener roomIDListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomID = String.valueOf(dataSnapshot.getValue());
                currentRoomID = roomID;
                System.out.println("setting current room to " + currentRoomID);

                if(roomID == null || roomID.equals("")) return;

                System.out.println("Adding user to room " + roomID);
                FirebaseDatabase.getInstance().getReference().child("roomUsers").child(roomID)
                        .child("users").child(getUserID()).setValue(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ValueEventListener removeFromListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String removeFrom = String.valueOf(dataSnapshot.getValue());

                if(removeFrom == null || removeFrom.equals("")) return;

                System.out.println("Removing from room " + removeFrom);
                FirebaseDatabase.getInstance().getReference().child("roomUsers").child(removeFrom)
                        .child("users").child(getUserID()).setValue(null);
                FirebaseDatabase.getInstance().getReference().child("users").child(getUserID())
                        .child("currentRoomID").setValue("");
                FirebaseDatabase.getInstance().getReference().child("users").child(getUserID())
                        .child("removeFrom").setValue("");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUserID());
        userRef.child("currentRoomID").addValueEventListener(roomIDListener);
        userRef.child("removeFrom").addValueEventListener(removeFromListener);

    }

}
