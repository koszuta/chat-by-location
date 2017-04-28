package com.cs595.uwm.chatbylocation.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.GeofenceTransitionsIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Nathan on 3/15/17.
 */

public class CreateRoomDialog extends DialogFragment {

    public static final int MIN_RADIUS = 100;
    public static final int MAX_RADIUS = 5000;
    public static final int RADIUS_INCREMENT = 10;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.create_room_dialog_layout, null);
        builder.setView(dialogView);
        /*     createGoogleApi();*/
        // "Create" button with null clickListener (created later)
        builder.setPositiveButton(R.string.create, null);

        // "Cancel" button action
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Get layout components for later use
        final EditText roomName = (EditText) dialogView.findViewById(R.id.roomName);
        final SeekBar roomRadius = (SeekBar) dialogView.findViewById(R.id.roomRadius);
        final TextView radiusValue = (TextView) dialogView.findViewById(R.id.radiusValue);
        final CheckBox roomIsPrivate = (CheckBox) dialogView.findViewById(R.id.roomIsPrivate);
        final EditText roomPassword = (EditText) dialogView.findViewById(R.id.roomPassword);

        // Seekbar listener; updates radius text and sets minimum and increment
        roomRadius.setMax((MAX_RADIUS - MIN_RADIUS) / RADIUS_INCREMENT);

        roomRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = radiusFromProgress(seekBar.getProgress());

                radiusValue.setText("Radius: " + Integer.toString(radius) + " m");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Checkbox listener; enables password field
        roomIsPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                roomPassword.setEnabled(isChecked);
            }
        });

        // Create a custom button listener
        // Allows checks on input before closing dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = String.valueOf(roomName.getText());
                        if ("".equals(name)) {
                            Toast.makeText(getActivity(), "Please enter a name", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int radius = radiusFromProgress(roomRadius.getProgress());

                        String password = null;
                        if (roomIsPrivate.isChecked()) {
                            password = roomPassword.getText().toString();
                            if ("".equals(password)) {
                                Toast.makeText(getActivity(), "Please enter a password", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        Location location = null;
                        Activity activity = getActivity();
                        if (activity instanceof SelectActivity) {
                            location = ((SelectActivity) activity).getLastLocation();
                        }

                        // Nathan TODO: Figure out how to return input values to SelectActivity and join room from there
                        if (location != null) {
                            Database.setUserRoom(Database.createRoom(
                                    Database.getUserId(),
                                    name,
                                    String.valueOf(location.getLongitude()),
                                    String.valueOf(location.getLatitude()),
                                    radius,
                                    password));

                            startActivity(new Intent(getActivity(), ChatActivity.class));
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "Failed to get location data", Toast.LENGTH_LONG).show();
                        }

                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    private int radiusFromProgress(int progress) {
        return MIN_RADIUS + progress * RADIUS_INCREMENT;
    }



// TODO: Remove all of this once Graham's done
/*
    private static final String GEOFENCE_REQ_ID = "Geofence";
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    private GoogleApiClient mGoogleApiClient;

    private void createGoogleApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void startGeofence() {
        Geofence geofence = createGeofence();
        GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
        addGeofence(geofenceRequest);
    }

    private Geofence createGeofence() {
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(Double.parseDouble(lat), Double.parseDouble(lon), radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private void addGeofence(GeofencingRequest request) {
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                request,
                createGeofencePendingIntent()
        ).setResultCallback(this);
    }
//*/
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MainActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }
/*
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( getApplicationContext(), GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(
                getApplicationContext(), GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    public void getLocationData() {
        try {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lon = String.valueOf(location.getLongitude());
            lat = String.valueOf(location.getLatitude());
        } catch(SecurityException e) {
            Log.d("Create Room Location", "SecurityException - Permission not set for location service");
        }
        catch (Exception e) {
            Log.d("Create Room Location", "Could not get last known location");
        }

        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        } catch(SecurityException e) {
            Log.d("Create Room Location", "SecurityException - Permission not set for location service");
        }
        catch (Exception e) {
            Log.d("Create Room Location", "Could not set last known location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getLocationData();
                    finishRoomCreation();

                } else {
                }

                return;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }
//*/
}
