package com.cs595.uwm.chatbylocation.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.objModel.UserIdentity;
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
import java.util.Map;

/**
 * Created by Lowell on 3/21/2017.
 */

public class Database {
    private static final long MAX_BYTES = 1024 * 1024 * 10;

    private static String currentRoomID;
    private static String removeFromRoom;

    private static int textSize = 14;
    private static boolean listening = false;
    private static boolean listeningToUsers = false;
    private static boolean shouldSignOut = false;

    private static Map<String, UserIdentity> users = new HashMap<>();
    private static Map<String, RoomIdentity> rooms = new HashMap<>();

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

    public static UserIdentity getUserByID(String userID){
        if(users == null) return null;
        return users.get(userID);
    }

    public static String getCurrentRoomName() {
        return (rooms.containsKey(currentRoomID)) ? rooms.get(currentRoomID).getName() : null;
    }

    public static String getRoomName(String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId).getName() : null;
    }

    public static String getRoomPassword(String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId).getPassword() : null;
    }

    public static int getRoomRadius(String roomId) {
        return (rooms.containsKey(roomId)) ? rooms.get(roomId).getRad() : 0;
    }

    public static double getRoomLat(String roomId) {
        return (rooms.containsKey(roomId)) ? Double.valueOf(rooms.get(roomId).getLat()) : 0;
    }

    public static double getRoomLng(String roomId) {
        return (rooms.containsKey(roomId)) ? Double.valueOf(rooms.get(roomId).getLongg()) : 0;
    }

    public static String getUserName(String userId) {
        return (users.containsKey(userId)) ? users.get(userId).getUsername() : null;
    }

    public static String getUserIcon(String userId) {
        return (users.containsKey(userId)) ? users.get(userId).getIcon() : UserIcon.NONE;
    }
    public static String getUserId(String userName) {
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

    public static void setIcon(String icon) {
        String userId = getUserId();
        if (userId != null) {
            getUsersReference().child(userId).child("icon").setValue(icon);
        }
    }

    public static int getTextSize() {
        return textSize;
    }

    public static void setTextSize(int size) {
        textSize = size;
    }

    public static Map<String, UserIdentity> getUsers() {
        return users;
    }

    public static Bitmap getUserImage(String userId) {
        if (!userImages.containsKey(userId)) {
            updateUserImage(userId);
            return null;
        } else {
            return userImages.get(userId);
        }
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

    public static StorageReference getImageStorageReference(String userId) {
        return FirebaseStorage.getInstance().getReference().child(userId);
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

    public static void createUser(String username) {

        if (getCurrentUserReference() != null) {
            getCurrentUserReference().child("currentRoomID").setValue("");
            getCurrentUserReference().child("icon").setValue(UserIcon.NONE);
            getCurrentUserReference().child("removeFrom").setValue("");
            getCurrentUserReference().child("username").setValue(username);
        }
        trace("created user");
    }

    public static void setUserRoom(String roomID) { // roomid = null removes from room

        trace("setUserRoom: " + roomID);

        if (getCurrentUserReference() != null) {
            if (roomID == null) {
                getCurrentUserReference().child("removeFrom").setValue(currentRoomID);
            } else {
                getCurrentUserReference().child("currentRoomID").setValue(roomID);
            }
        }
    }

    public static void signOutUser() {
        shouldSignOut = true;
        removeCurrentUserListeners();
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

    private static void trace(String message) {
        System.out.println("Database >> " + message); //todo android logger
    }
}
