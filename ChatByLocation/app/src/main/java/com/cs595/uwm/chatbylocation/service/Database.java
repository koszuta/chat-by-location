package com.cs595.uwm.chatbylocation.service;

import android.graphics.Color;

import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.cs595.uwm.chatbylocation.objModel.UserIdentity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {

    private static String currentRoomID;
    private static String removeFromRoom;

    private static int textSize = 14;
    private static boolean listening = false;

    private static boolean shouldSignOut = false;

    // TODO: Move this somewhere better
    private static Map<String, String> roomNames = new HashMap<>();
    private static Map<String, String> roomPasswords = new HashMap<>();
    private static int textColor = Color.parseColor("#000000");
    private static ArrayList<UserIdentity> users;

    public static String getCurrentRoomName() {
        return (currentRoomID == null) ? null : roomNames.get(currentRoomID);
    }

    public static String getRoomName(String roomId) {
        return roomNames.get(roomId);
    }

    public static String getRoomPassword(String roomId) {
        return roomPasswords.get(roomId);
    }

    public static int getTextSize() {
        return textSize;
    }

    public static void setTextSize(int size) {
        textSize = size;
    }

    public static ArrayList<UserIdentity> getUsers() {
        return users;
    }

    public static void setUsers(ArrayList<UserIdentity> users) {
        Database.users = users;
    }

    public static void initListeners() {
        if (listening) return;

        DatabaseReference userRef = getCurrentUserReference();

        userRef.child("currentRoomID")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String roomID = String.valueOf(dataSnapshot.getValue());
                        trace("roomIDListener sees roomID: " + roomID);
                        currentRoomID = roomID;

                        if (roomID == null || roomID.equals("")) return;

                        String userId = getUserID();
                        if (userId != null) {
                            getRoomUsersReference().child(roomID).child(userId).setValue(true);
                        }

                        // Sign out user
                        if (shouldSignOut && "".equals(currentRoomID) && "".equals(removeFromRoom)) {
                            shouldSignOut = false;
                            FirebaseAuth.getInstance().signOut();
                            trace("User signed out");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        userRef.child("removeFrom")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String removeFrom = String.valueOf(dataSnapshot.getValue());
                        trace("removeFromListener sees removeFrom: " + removeFrom);

                        String userId = getUserID();
                        if (!(removeFrom == null || removeFrom.equals(""))) {
                            // Remove user from roomUsers list
                            if (userId != null) {
                                getRoomUsersReference().child(removeFrom).child(userId).removeValue();
                                getCurrentUserReference().child("currentRoomID").setValue("");
                            }
                            
                            getCurrentUserReference().child("removeFrom").setValue("");

                            trace("removeFromListener has removed the user from their room");
                        }

                        // Sign out user
                        if (shouldSignOut && "".equals(currentRoomID) && "".equals(removeFromRoom)) {
                            shouldSignOut = false;
                            FirebaseAuth.getInstance().signOut();
                            trace("User signed out");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        trace("assigned listeners to user.currentRoomID and user.removeFrom");

        getRoomIdentityReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " added to \'roomIdentity\'");

                // Add room name to list
                String name = String.valueOf(dataSnapshot.child("name").getValue());
                roomNames.put(roomId, name);

                // Add room password to list
                Object pw = dataSnapshot.child("password").getValue();
                String password = (pw == null) ? null : pw.toString();
                roomPasswords.put(roomId, password);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " removed from \'roomIdentity\'");

                // Remove room name and password when room is deleted
                roomNames.remove(roomId);
                roomPasswords.remove(roomId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " data changed in \'roomIdentity\'");

                // Update room name
                String name = String.valueOf(dataSnapshot.child("name").getValue());
                roomNames.put(roomId, name);

                // Update room password
                Object pw = dataSnapshot.child("password").getValue();
                String password = (pw == null) ? null : pw.toString();
                roomPasswords.put(roomId, password);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " moved in \'roomIdentity\'");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                trace("Child event canceled in \'roomIdentity\'");
                System.err.println(databaseError.getMessage());
            }
        });

        trace("Assigned listener to \'roomIdentity\'");

        listening = true;
    }

    public static String getCurrentRoomID() {
        return currentRoomID;
    }

    public static void createUser(String username) {

        getCurrentUserReference().child("currentRoomID").setValue("");
        getCurrentUserReference().child("username").setValue(username);
        trace("created user");

    }

    public static void setUserRoom(String roomID) { // roomid = null removes from room

        trace("setUserRoom: " + roomID);

        if (roomID == null) {
            getCurrentUserReference().child("removeFrom").setValue(currentRoomID);
        } else {
            getCurrentUserReference().child("currentRoomID").setValue(roomID);
        }

    }

    public static void signOutUser() {
        shouldSignOut = true;
        setUserRoom(null);
    }

    public static void setUsersListener(ValueEventListener usersListener){
        getRoomUsersReference().child(currentRoomID).addListenerForSingleValueEvent(usersListener);
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

    public static String getUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getUid();
        return null;
    }

    public static DatabaseReference getCurrentUserReference() {
        String userID = getUserID();
        trace("userID = " + userID);
        if (userID == null) return null;
        return getUsersReference().child(userID);
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

    public static DatabaseReference getUsersReference(){
        return FirebaseDatabase.getInstance().getReference().child("users");
    }

    public static void initUsersListener() {
        getRoomUsersReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserIdentity user = dataSnapshot.getValue(UserIdentity.class);
                trace("adding user to local users list: " + user.getUsername());
                Database.getUsers().add(user);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                UserIdentity user = dataSnapshot.getValue(UserIdentity.class);
                trace("removing user from local users list: " + user.getUsername());
                Iterator<UserIdentity> usersIt = Database.getUsers().iterator();
                while (usersIt.hasNext()){
                    UserIdentity u = usersIt.next();
                    if(u.equals(user)) usersIt.remove();
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void trace(String message) {
        System.out.println("Database >> " + message); //todo android logger
    }


}
