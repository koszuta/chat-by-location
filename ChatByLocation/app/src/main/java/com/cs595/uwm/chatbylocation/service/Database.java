package com.cs595.uwm.chatbylocation.service;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.objModel.UserIdentity;
import com.cs595.uwm.chatbylocation.view.ChatActivity;
import com.firebase.ui.auth.ui.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {
    private static final long MAX_BYTES = 1024 * 1024 * 10;

    private static String currentRoomID;
    private static String removeFromRoom;

    private static long joinTimeMillis;
    private static ChildEventListener messageCountListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
            if (message.getMessageTime() < joinTimeMillis) {
                ChatActivity.incrNumMessages();
            }
        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };

    private static ValueEventListener changeOwnerListener;
    private static ValueEventListener roomUsersListener;
    private static boolean isOwner = false;
    private static int ownerTransferTask;

    private static boolean listening = false;
    private static boolean listeningToUsers = false;
    private static boolean shouldSignOut = false;

    private static Map<String, UserIdentity> users = new HashMap<>();
    private static Map<String, RoomIdentity> rooms = new HashMap<>();
    private static Map<String, UserIdentity> roomUsers = new HashMap<>();

    private static Map<String, Bitmap> userImages = new HashMap<>();

    // These need to be defined here so they can be removed when a user signs out
    private static ValueEventListener cridListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String roomID = String.valueOf(dataSnapshot.getValue());
            trace("roomIDListener sees roomID: " + roomID);
            currentRoomID = roomID;

            if (roomID == null || roomID.equals("")) return;

            String userId = getUserId();
            if (userId != null) {
                getRoomUsersReference().child(roomID).child(userId).setValue(true);
                getCurrentUserReference().child("roomJoinTime").setValue(System.currentTimeMillis());
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
    };

    private static ValueEventListener rfrListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String removeFrom = String.valueOf(dataSnapshot.getValue());
            trace("removeFromListener sees removeFrom: " + removeFrom);

            String userId = getUserId();
            if (!(removeFrom == null || removeFrom.equals(""))) {
                // Remove user from roomUsers list
                if (userId != null) {
                    getRoomUsersReference().child(removeFrom).child(userId).removeValue();
                    getCurrentUserReference().child("currentRoomID").setValue("");

                    if(isOwner){
                        if(roomUsers.size() <= 1){
                            destroyRoom(removeFrom);
                        } else {
                            String nextOwnerID = getNextOwnerID(userId);
                            setRoomOwner(currentRoomID, nextOwnerID);
                        }

                        //todo lowell
                        //destroy onDisconnect task

                    }

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
    };

    public static UserIdentity getUserByID(final String userID){
        if(users == null) return null;
        return users.get(userID);
    }

    public static String getCurrentRoomName() {
        return (rooms.containsKey(currentRoomID)) ? rooms.get(currentRoomID).getName() : null;
    }

    public static RoomIdentity getRoomIdentity(final String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId) : null;
    }

    public static String getRoomName(final String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId).getName() : null;
    }

    public static String getRoomPassword(final String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId).getPassword() : null;
    }

    public static String getNextOwnerID(String currentOwnerID){

        Map.Entry<String, UserIdentity> nextOwner = null;

        for(Map.Entry<String, UserIdentity> userEntry : users.entrySet() ){
            if(nextOwner == null) nextOwner = userEntry;

            if(userEntry.getKey().equals(currentOwnerID)) continue;

            if(Long.valueOf(userEntry.getValue().getRoomJoinTime())
                    < Long.valueOf(nextOwner.getValue().getRoomJoinTime())){
                nextOwner = userEntry;
            }

        }

        return nextOwner.getKey();
    }

    public static int getRoomRadius(final String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId).getRad() : 0;
    }

    public static double getRoomLat(final String roomId) {
        return (rooms.containsKey(roomId)) ? Double.valueOf(rooms.get(roomId).getLat()) : 0;
    }

    public static double getRoomLng(final String roomId) {
        return (rooms.containsKey(roomId)) ? Double.valueOf(rooms.get(roomId).getLongg()) : 0;
    }

    public static String getUserName(final String userId) {
        return (users.containsKey(userId)) ? users.get(userId).getUsername() : null;
    }

    public static String getUserIcon(final String userId) {
        return (users.containsKey(userId)) ? users.get(userId).getIcon() : UserIcon.NONE;
    }

    public static String getCurrentUserIcon() {
        return getUserIcon(getUserId());
    }

    public static String getUserId(final String userName) {
        if (userName == null) return null;
        for (Map.Entry<String, UserIdentity> entry : users.entrySet()) {
            String userId = entry.getKey();
            UserIdentity u = entry.getValue();
            if (userName.equals(u.getUsername())) {
                return userId;
            }
        }
        return null;
    }

    public static void setIcon(final String icon) {
        String userId = getUserId();
        if (userId != null) {
            getUsersReference().child(userId).child("icon").setValue(icon);
        }
    }

    public static Map<String, UserIdentity> getUsers() {
        return users;
    }

    public static Bitmap getUserImage(final String userId) {
        if (!userImages.containsKey(userId)) {
            updateUserImage(userId);
            return null;
        } else {
            return userImages.get(userId);
        }
    }

    public static Bitmap getCurrentUserImage() {
        return getUserImage(getUserId());
    }

    public static void updateUserImage(final String userId) {
        if (userId == null) return;
        getImageStorageReference(userId).getBytes(MAX_BYTES)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (image == null) {
                            trace("User image for " + users.get(userId).getUsername() + " is null");
                            return;
                        }
                        userImages.put(userId, image);
                        trace("User image successfully updated for " + users.get(userId).getUsername());
                    }
                });
    }

    public static StorageReference getImageStorageReference(final String userId) {
        return FirebaseStorage.getInstance().getReference().child(userId);
    }

    public static void initRoomMessagesListener(final long joinTimeMillis) {
        if (getCurrentRoomID() != null) {
            Database.joinTimeMillis = joinTimeMillis;
            getRoomMessagesReference().child(getCurrentRoomID())
            .addChildEventListener(messageCountListener);
            trace("Added room messages listener");
        }
    }

    public static void registerRoomUsersListener(String roomID){

        //DatabaseReference roomUsersRef = getRoomUsersReference().child("roomID").child("users");
        DatabaseReference roomUsersRef = getRoomUsersReference().child("roomID");

        if(roomUsersListener != null) {
            roomUsersRef.removeEventListener(roomUsersListener);
            roomUsers = new HashMap<>();
        }

        roomUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(String userID : ((HashMap<String, Object>) dataSnapshot.getValue()).keySet())
                    roomUsers.put(userID, Database.getUserByID(userID));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        Database.getRoomUsersReference().child(roomID).addValueEventListener(roomUsersListener);
    }

    public static void registerChangeOwnerListener(final String roomID){

        DatabaseReference ownerIDRef = getRoomIdentityReference().child(roomID).child("ownerID");
        if(changeOwnerListener != null) ownerIDRef.removeEventListener(changeOwnerListener);

        changeOwnerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String ownerID = String.valueOf(dataSnapshot.getValue());
                if(ownerID == null) return;
                trace("OwnerID of room " + roomID + " changed to " + ownerID);
                if(getUserId().equals(ownerID)) { // the owner is this client
                    if(isOwner) return;
                    //do stuff in gui here if required

                    //todo lowell
                    //listen to nextOwnerID value
                        //inside listener: create onDisconnect task that makes nextOwnerID the owner

                    isOwner = true;
                } else if(isOwner){
                    isOwner = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ownerIDRef.addValueEventListener(changeOwnerListener);

    }

    public static void removeRoomMessagesListener() {
        if (getCurrentRoomID() != null) {
            getRoomMessagesReference().child(getCurrentRoomID()).removeEventListener(messageCountListener);
            trace("Removed room messages listener");
        }
    }

    public static void initCurrentUserListeners() {
        DatabaseReference userRef = getCurrentUserReference();
        if (userRef != null) {
            userRef.child("currentRoomID").addValueEventListener(cridListener);
            userRef.child("removeFrom").addValueEventListener(rfrListener);

            trace("assigned listeners to user.currentRoomID and user.removeFrom");
        }
    }

    public static void removeCurrentUserListeners() {
        DatabaseReference userRef = getCurrentUserReference();
        if (listening && userRef != null) {
            userRef.child("currentRoomID").removeEventListener(cridListener);
            userRef.child("removeFrom").removeEventListener(rfrListener);
        }
    }

    public static void initUsersListener() {
        if (listeningToUsers) return;

        String userId = getUserId();
        if (userId != null) {
            getUsersReference().child(userId).child("username").setValue(getUserUsername());
        }

        getUsersReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();
                UserIdentity user = dataSnapshot.getValue(UserIdentity.class);

                // Add UserIdentity to list
                users.put(userId, user);

                // Add user image to list
                updateUserImage(userId);

                trace("added user to local users list: " + user.getUsername());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();
                UserIdentity user = dataSnapshot.getValue(UserIdentity.class);

                // Update UserIdentity
                users.put(userId, user);

                // Update user image
                if (UserIcon.PHOTO.equals(user.getIcon())) {
                    updateUserImage(userId);
                }

                trace("Updated data in users list: " + user.getUsername());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String userId = dataSnapshot.getKey();
                UserIdentity user = dataSnapshot.getValue(UserIdentity.class);
                trace("removing user from local users list: " + user.getUsername());

                users.remove(userId);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        trace("Assigned listener to users list");
        listeningToUsers = true;
    }

    public static void initRoomsListener() {
        if (listening) return;
        getRoomIdentityReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " added to \'roomIdentity\'");

                RoomIdentity room = dataSnapshot.getValue(RoomIdentity.class);
                rooms.put(roomId, room);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " data changed in \'roomIdentity\'");

                RoomIdentity room = dataSnapshot.getValue(RoomIdentity.class);
                rooms.put(roomId, room);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String roomId = dataSnapshot.getKey();
                trace("Child " + roomId + " removed from \'roomIdentity\'");

                // Remove room when it's deleted
                rooms.remove(roomId);
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

    public static void createUser(final String username) {

        if (getCurrentUserReference() != null) {
            getCurrentUserReference().child("currentRoomID").setValue("");
            getCurrentUserReference().child("icon").setValue(UserIcon.NONE);
            getCurrentUserReference().child("removeFrom").setValue("");
            getCurrentUserReference().child("username").setValue(username);
        }
        trace("created user");
    }

    public static void setUserRoom(final String roomID) { // roomid = null removes from room

        trace("setUserRoom: " + roomID);

        if (getCurrentUserReference() != null) {
            if (roomID == null) {
                getCurrentUserReference().child("removeFrom").setValue(currentRoomID);
            } else {
                getCurrentUserReference().child("currentRoomID").setValue(roomID);
            }
        }
    }

    public static void setRoomOwner(String roomID, String userID){
        getRoomIdentityReference().child(roomID).child("ownerID").setValue(userID);
    }

    public static void signOutUser() {
        shouldSignOut = true;
        removeCurrentUserListeners();
        setUserRoom(null);
    }

    public static void setUsersListener(final ValueEventListener usersListener) {
        getRoomUsersReference().child(currentRoomID).addListenerForSingleValueEvent(usersListener);
    }

    public static String getUserUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getDisplayName();
        return null;

    }

    public static String createRoom(final String ownerID, final String name,
                                    final String longg, final String lat,
                                    final int rad, final String password) {

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

    public static void destroyRoom(String roomID){
        getRoomIdentityReference().child(roomID).setValue(null);
        getRoomMessagesReference().child(roomID).setValue(null);
        getRoomUsersReference().child(roomID).setValue(null);
    }

    public static void sendChatMessage(final ChatMessage chatMessage, final String roomID, final Activity activity) {
        getRoomMessagesReference()
                .child(roomID).push()
                .setValue(chatMessage)
                .addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                trace("Failed to send message: " + e.getLocalizedMessage());
                ChatActivity.setTextInput(chatMessage.getMessageText());
                Toast.makeText(activity, "Failed to send message", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getUid();
        return null;
    }

    public static DatabaseReference getCurrentUserReference() {
        String userId = getUserId();
        trace("userID = " + userId);
        return (userId != null) ? getUsersReference().child(userId) : null;
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
        return FirebaseDatabase.getInstance().getReference().child("usersMap");
    }

    private static void trace(final String message) {
        System.out.println("Database >> " + message); //todo android logger
    }

    public static boolean isCurrentUserAdminOfRoom() {
        return isOwner;
    }
}
