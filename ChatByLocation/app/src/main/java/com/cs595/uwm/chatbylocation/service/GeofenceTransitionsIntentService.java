package com.cs595.uwm.chatbylocation.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.view.ChatActivity;
import com.cs595.uwm.chatbylocation.view.CreateRoomDialog;
import com.cs595.uwm.chatbylocation.view.SelectActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gnovak.sh on 4/16/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    private static Handler mHandler = new Handler();

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            trace("Geofence error: " + errorMsg);
            return;
        }

        Log.d("geofence", "handle intent");

        // If the user is not in the room, don't do anything
        if (Database.getCurrentRoomID() == null) {
            trace("Geofence triggered, but user is not in a room");
            return;
        }

        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        trace("Geofence transition: " + getGeofenceTransitionDetails(geofencingEvent.getGeofenceTransition(), triggeringGeofences));

        // Do somethine based on the transition type
        /*
        switch (geofencingEvent.getGeofenceTransition()) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                // Give a warning if they cross the chat room's radius
                if (ChatActivity.shouldWelcomeUser()) {
                    // Send a welcome message
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GeofenceTransitionsIntentService.this, "Welcome to the chat room!", Toast.LENGTH_LONG).show();
                            ChatActivity.setShouldWelcomeUser(false);
                        }
                    });
                }
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                for (Geofence g : triggeringGeofences) {
                    trace("Exit: request id = " + g.getRequestId());

                    // Give a warning if they cross the warning radius
                    if (ChatActivity.WARN_GEOFENCE.equals(g.getRequestId())) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GeofenceTransitionsIntentService.this, "Turn back! You're getting too far away!", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }

                    // Kick them if they leave the kick radius
                    if (ChatActivity.KICK_GEOFENCE.equals(g.getRequestId())) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GeofenceTransitionsIntentService.this, "You wandered astray and were kicked from the chat room!", Toast.LENGTH_LONG).show();
                            }
                        });

                        // Tell ChatActivity to kick the user next chance it gets
                        ChatActivity.kickUser();
                        break;
                    }
                }
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                break;
            default:
                break;
        }
        */
    }


    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    private void sendNotification( String msg ) {
        Log.i(TAG, "sendNotification: " + msg );

        // Intent to start the main Activity
        //Intent notificationIntent = CreateRoomDialog.makeNotificationIntent(
             //   getApplicationContext(), msg
        //);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SelectActivity.class);
        //stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));

    }

    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        //Todo find better Icon
        notificationBuilder
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    private static void trace(String message) {
        System.out.println("GeofenceTIS >> " + message);
    }
}