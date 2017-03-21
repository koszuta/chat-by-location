package com.cs595.uwm.chatbylocation.Singleton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {

    public static void addUserToRoom(String roomID){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("currentRoomID").setValue(roomID);

        DatabaseReference roomUsersRef = FirebaseDatabase.getInstance().getReference().child("roomUsers").child(roomID);
        roomUsersRef.child("users").child(userID).setValue(true);

    }

    public static void createUser(String username){
        String userID = getUserID();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef.child("username").setValue(username);
        userRef.child("currentRoomID").setValue(null);

    }

    public static void createRoom(String ownerID, String name, String longg, String lat,
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

    }

    private static String getUserID(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
