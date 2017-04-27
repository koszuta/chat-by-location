package com.cs595.uwm.chatbylocation.view;

import android.Manifest;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.BanController;
import com.cs595.uwm.chatbylocation.controllers.MuteController;
import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.GeofenceTransitionsIntentService;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created by Nathan on 3/13/17.
 */

public class ChatActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int REQUEST_FINE_LOCATION_ACCESS = 19;

    public static final int PAST_MESSAGES_LIMIT = 100;
    public static final long TEN_MINUTES_IN_MILLIS = 10 * 60 * 1000;
    
    public static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final long ONE_WEEK_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS;
    public static final long ONE_YEAR_IN_MILLIS = 365 * ONE_DAY_IN_MILLIS;

    public static final String NAME_ARGUMENT = "usernameForBundle";
    public static final String ICON_ARGUMENT = "iconForBundle";
    private static final String USER_ID_ARGUMENT = "userIdForBundle";
    private static final String ROOM_ID_ARGUMENT = "roomIdForBundle";
    private static final long GEO_DURATION = 60 * 60 * 1000;

    private DialogFragment messageDialog;
    private ListView messageListView;
    private Intent banUserIntent;
    Bundle args = new Bundle();

    private static boolean shouldGetNumMessages = true;
    private static long tenMinutesBeforeJoin;
    private static int numMessagesAtJoin;
    private Geofence mGeofence;
    private static EditText textInput;
    private FirebaseListAdapter<ChatMessage> chatListAdapter;
    private PendingIntent mGeofencePendingIntent;
    private String GEOFENCE_REQ_ID = "Geofence";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        setTitle(Database.getCurrentRoomName());

        createGoogleApi();
        buildGeofence();

        //construct objects
        banUserIntent = new Intent(this, SelectActivity.class);
        messageListView = (ListView) this.findViewById(R.id.messageList);
        messageListView.setItemsCanFocus(false);
        messageDialog = new MessageDetailsDialog();

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMessage message = (ChatMessage)parent.getItemAtPosition(position);
                String messageUser = message.getMessageUser();
                String userId = Database.getUserId(messageUser);
                int iconRes = UserIcon.getIconResource(Database.getUserIcon(userId));

                messageDialog = new MessageDetailsDialog();

                args.putString(NAME_ARGUMENT, messageUser);
                args.putInt(ICON_ARGUMENT, iconRes);
                args.putString(USER_ID_ARGUMENT, userId);
                args.putString(ROOM_ID_ARGUMENT, Database.getCurrentRoomID());
                messageDialog.setArguments(args);
                messageDialog.show(getFragmentManager(), "message details");
            }
        });

        if (shouldGetNumMessages) {
            trace("Setting join time and number of messages before join");
            numMessagesAtJoin = 0;
            long now = System.currentTimeMillis();
            tenMinutesBeforeJoin = now - ChatActivity.TEN_MINUTES_IN_MILLIS;
            Database.initRoomMessagesListener(now);
            shouldGetNumMessages = false;
        }

        displayChatMessages();
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public static void incrNumMessages() {
        numMessagesAtJoin++;
    }

    public void onMuteClick(View view) {
        String name = messageDialog.getArguments().getString(NAME_ARGUMENT);
        MuteController.onMuteClick(name, getApplicationContext());
        chatListAdapter.notifyDataSetChanged();
    }


    public void banUserClick(View view) {
        String userId = args.getString(USER_ID_ARGUMENT);
        String roomID = Database.getCurrentRoomID();
        if(Database.isCurrentUserAdminOfRoom(roomID)) {
            BanController.addToRoomBanList(view.getContext(), userId, roomID);
            Toast.makeText(view.getContext(), "You have been banned from the room! Shame.", Toast.LENGTH_SHORT).show();
        }
        if(isUserBannedFromCurrentRoom()) {
            startActivity(banUserIntent);
            finish();
        }
    }

    public void toBottomClick(View view) {
        ListView list = (ListView) findViewById(R.id.messageList);
        list.smoothScrollToPosition(list.getCount());
    }

    public void sendMessageClick(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null) {
            Toast.makeText(this, "No network connectivity", Toast.LENGTH_LONG).show();
            return;
        }


        // TODO: User exited Geofence can't send message


        // Nathan TODO: Use better ban method
        //block message and kick out user if banned from current room
        if(isUserBannedFromCurrentRoom()) {
            doLeaveRoom();
        }

        // Get color for message from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int color = prefs.getInt("color1", Color.BLACK);

        textInput = (EditText) findViewById(R.id.textInput);
        String message = String.valueOf(textInput.getText());
        String roomId = Database.getCurrentRoomID();
        if (roomId != null && !"".equals(message) && message != null) {
            Database.sendChatMessage(
                    new ChatMessage(message, Database.getUserUsername(), color),
                    roomId,
                    this);
        }

        textInput.setText("");
    }

    public static void setTextInput(String text) {
        if (textInput != null) {
            textInput.setText(text);
        }
    }

    private void displayChatMessages() {
        DatabaseReference currentUserRef = Database.getCurrentUserReference();
        if (currentUserRef != null) {

            currentUserRef.child("currentRoomID").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String roomID = String.valueOf(dataSnapshot.getValue());
                    trace("roomIDListener sees roomid = " + roomID);

                    chatListAdapter = new FirebaseListAdapter<ChatMessage>(
                            ChatActivity.this,
                            ChatMessage.class,
                            R.layout.message_item,
                            Database.getRoomMessagesReference().child(roomID)) {
                        @Override
                        protected void populateView(View view, ChatMessage chatMessage, int position) {
                            //trace(position + ") " + chatMessage.getMessageText());

                            // Get reference to the views of message_item
                            final TextView messageText = (TextView) view.findViewById(R.id.messageText);
                            final ImageView userIcon = (ImageView) view.findViewById(R.id.userIcon);
                            final RelativeLayout divider = (RelativeLayout) view.findViewById(R.id.messageDivider);

                            // Decide whether message should be visible
                            messageText.setVisibility(View.GONE);
                            userIcon.setVisibility(View.GONE);
                            divider.setVisibility(View.GONE);
                            view.setVisibility(View.GONE);
                            /*
                            if (muteTime != -1 && muteTime < chatMessage.getMessageTime()) {
                                //trace(username + " was muted at " + muteTime);
                                return;
                            }
                            //*/
                            // Limit number of messages from before entering chat room
                            if (position < numMessagesAtJoin - PAST_MESSAGES_LIMIT) {
                                //trace("Too many messages: " + position + " < " + (numMessagesAtJoin - PAST_MESSAGES_LIMIT));
                                return;
                            }
                            // Limit messages to within 10 minutes of entering chat room
                            else if (chatMessage.getMessageTime() < tenMinutesBeforeJoin) {
                                //trace("Message too old: " + chatMessage.getMessageTime() + " < " + (tenMinutesBeforeJoin));
                                return;
                            }
                            // Otherwise show message
                            else {
                                messageText.setVisibility(View.VISIBLE);
                                userIcon.setVisibility(View.VISIBLE);
                                divider.setVisibility(View.VISIBLE);
                                view.setVisibility(View.VISIBLE);
                            }

                            String username = chatMessage.getMessageUser();
                            if (username == null) username = "no name";

                            SpannableString ss;

                            // Check if user was muted when the message was sent
                            long muteTime = MuteController.isMuted(username);
                            if (muteTime != -1 && muteTime < chatMessage.getMessageTime()) {
                                ss = new SpannableString("         This user is muted");
                                userIcon.setVisibility(View.GONE);
                            }
                            else {
                                // Format message text
                                String timestamp = formatTimestamp(chatMessage.getMessageTime());
                                if (timestamp == null) timestamp = "";
                                int timeLength = timestamp.length() + 1;

                                ss = new SpannableString(timestamp + ' ' + username + ": " + chatMessage.getMessageText());

                                RelativeSizeSpan timeSize = new RelativeSizeSpan(0.8f);
                                ss.setSpan(timeSize, 0, timeLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                                ForegroundColorSpan timeColor = new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.timestamp, null));
                                ss.setSpan(timeColor, 0, timeLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                                StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
                                ss.setSpan(bold, timeLength, timeLength + username.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                                ForegroundColorSpan textColor = new ForegroundColorSpan(chatMessage.getMessageColor());
                                ss.setSpan(textColor, timeLength + username.length() + 1, ss.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                                // Use icon from corresponding user
                                String userId = Database.getUserId(username);
                                int iconRes = UserIcon.getIconResource(Database.getUserIcon(userId));
                                if (iconRes == 0) {
                                    Bitmap image = Database.getUserImage(userId);
                                    if (image != null) {
                                        userIcon.setImageBitmap(image);
                                    } else {
                                        userIcon.setImageResource(UserIcon.NONE_RESOURCE);
                                    }
                                } else {
                                    userIcon.setImageResource(iconRes);
                                }
                            }

                            messageText.setText(ss);
                        }
                    };

                    // Nathan TODO: Implement kicked from room using database listener
                    ListView listOfMessages = (ListView) findViewById(R.id.messageList);
                    if (roomID.equals("")) {
                        chatListAdapter.cleanup();
                        listOfMessages.setAdapter(null);
                        trace("roomIDListener removing adapter");
                    } else {
                        listOfMessages.setAdapter(chatListAdapter);
                        trace("roomIDListener setting adapter");

                    }
                    chatListAdapter.registerDataSetObserver(new DataSetObserver()
                    {
                        @Override
                        public void onChanged()
                        {
                            //block message and kick out user if banned from current room
                            //TODO:add a method delay to prevent spamming this method call on every message
                            if(isUserBannedFromCurrentRoom()) {
                                Toast.makeText(getApplicationContext(), "You have been banned from the room!", Toast.LENGTH_SHORT).show();
                                startActivity(banUserIntent);
                                finish();
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                doLeaveRoom();
                break;
            case R.id.room_users:
                Intent userIntent = new Intent(this, RoomUserListActivity.class);
                startActivity(userIntent);
                break;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("caller", ChatActivity.class.getName());
                startActivity(settingsIntent);
                break;
            case R.id.menu_sign_out:
                doSignOut();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION_ACCESS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                }
                break;
            default:
                break;
        }
    }

    private void doLeaveRoom() {
        Database.setUserRoom(null);
        Database.removeRoomMessagesListener();
        shouldGetNumMessages = true;
        startActivity(new Intent(this, SelectActivity.class));
    }

    private void doSignOut() {
        Database.signOutUser();
        Database.removeRoomMessagesListener();
        shouldGetNumMessages = true;
        startActivity(new Intent(this, MainActivity.class));
    }

    private String formatTimestamp(long timeMillis) {
        String format = "h:mma";

        long timeDiff = System.currentTimeMillis() - timeMillis;
        if (timeDiff >= ONE_YEAR_IN_MILLIS) {
            format = "M/D/YYYY " + format;
        } else if (timeDiff >= ONE_WEEK_IN_MILLIS) {
            format = "D MMM " + format;
        } else if (timeDiff >= ONE_DAY_IN_MILLIS) {
            format = "EEE " + format;
        }

        if (android.text.format.DateFormat.is24HourFormat(this)) {
            format = format.replace("h", "H").replace("a", "");
        }

        Date messageDate = new Date(timeMillis);
        String dateFormatted = (String) android.text.format.DateFormat.format(format, messageDate);
        dateFormatted = dateFormatted.replace("AM", "am").replace("PM", "pm");

        return dateFormatted;
    }
    private GoogleApiClient mGoogleApiClient;

    private void createGoogleApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void buildGeofence() {
        mGeofence = createGeofence();
        GeofencingRequest geofenceRequest = createGeofenceRequest(mGeofence);
        addGeofence(geofenceRequest);
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofence(geofence)
                .build();
    }

    private Geofence createGeofence() {
        String roomId = Database.getCurrentRoomID();
        double lat = Database.getRoomLat(roomId);
        double lng = Database.getRoomLng(roomId);
        int radius = Database.getRoomRadius(roomId);
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(lat, lng, radius)
                .setExpirationDuration(ONE_DAY_IN_MILLIS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofence(GeofencingRequest request) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    getGeofencePendingIntent()
            );
        } else {
            requestFineLocationPermission();
        }
    }

    private void removeGeofence(){
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        );
    }

    private boolean isUserBannedFromCurrentRoom() {
        return BanController.isCurrentUserBanned(Database.getCurrentRoomID());
    }

    private static void trace(String message){
        System.out.println("ChatActivity >> " + message); //todo android logger
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        trace("Connected to location api");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0)
                .setFastestInterval(0)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            trace("Has permissions after location api connection");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        } else {
            trace("No permissions after location api connection");
            requestFineLocationPermission();
        }
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION_ACCESS);
    }
    @Override
    public void onConnectionSuspended(int i) {
        trace("Location api connection suspended");
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        trace("Failed to connect to location api");
    }

    @Override
    public void onLocationChanged(Location location) {}
}
