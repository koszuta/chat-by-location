package com.cs595.uwm.chatbylocation.service;

import com.cs595.uwm.chatbylocation.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {

    public static void createUser(String username){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("username").setValue(username);
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
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        return userRef.child("currentRoomID").toString();
    }

    public static String getUserUsername(){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        return userRef.child("username").toString();
    }

    public static void setUserUsername(String username){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("username").setValue(username);
    }

    public static boolean getUsernameUnique(final String username){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        final boolean[] unique = {true};
        usersRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (Map.Entry<String, Object> entry : ((Map<String,Object>) dataSnapshot.getValue()).entrySet()){
                            Map user = (Map) entry.getValue();
                            if(String.valueOf(user.get("username")).toLowerCase().equals(username.toLowerCase())){
                                unique[0] = false;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        return unique[0];
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
