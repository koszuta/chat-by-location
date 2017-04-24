package com.cs595.uwm.chatbylocation.view;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.BanController;
import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.LocationService;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

/**
 * Created by Nathan on 3/13/17.
 */

public class SelectActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    public static Location location;

    private FirebaseListAdapter<RoomIdentity> roomListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Database.initCurrentUserListeners();
        displayRoomList();
    }
    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onLocationChanged(Location plocation) {
        trace("Location changed");
        location = plocation;
        roomListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        trace("Connected to location api");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        displayRoomList();
    }
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("caller", SelectActivity.class.getName());
                startActivity(settingsIntent);
                break;

            case R.id.menu_sign_out:
                Database.signOutUser();
                //return to sign in
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.menu_bypass:
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
        return true;
    }

    public void joinRoomClick(View view) {
        String roomId = String.valueOf(view.getTag());
        System.out.println("roomId = " + roomId);

        if (BanController.isCurrentUserBanned(roomId)) {
            AlertDialog aD = new AlertDialog.Builder(view.getContext())
                    .setTitle("Cannot Join Room")
                    .setMessage("You have been banned from this room!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
        else if (Database.getRoomPassword(roomId) != null) {
            DialogFragment dialog = new PasswordCheckDialog();
            Bundle args = new Bundle();
            args.putString("roomId", roomId);
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "check password");
        }
        else {
            Database.setUserRoom(roomId);
            startActivity(new Intent(this, ChatActivity.class));
        }
    }


    public void createRoomClick(View view) {
        DialogFragment dialog = new CreateRoomDialog();
        dialog.show(getFragmentManager(), "create room");
    }

    private void displayRoomList() {
        final ListView listOfRooms = (ListView) findViewById(R.id.roomList);
        roomListAdapter = new FirebaseListAdapter<RoomIdentity>(
                this,
                RoomIdentity.class,
                R.layout.room_list_item,
                Database.getRoomIdentityReference()) {
            @Override
            protected void populateView(View view, RoomIdentity roomIdentity, int position) {
                final TextView roomName = (TextView) view.findViewById(R.id.roomName);
                final TextView roomCoords = (TextView) view.findViewById(R.id.roomCoords);
                final TextView roomRadius = (TextView) view.findViewById(R.id.roomRadius);
                final ImageView roomIsPrivate = (ImageView) view.findViewById(R.id.roomIsPrivate);
                final Button joinButton = (Button) view.findViewById(R.id.joinButton);


                // Nathan TODO: Check if user is within room radius (with math)
                float lat = Float.valueOf(roomIdentity.getLat());
                float lng = Float.valueOf(roomIdentity.getLongg());

                if (!withinRoomRadius(lat, lng, roomIdentity.getRad())) {
                    trace("Room " + roomIdentity.getName() + " is out of range");
                    view.setPadding(0,0,0,0);
                    roomName.setVisibility(View.GONE);
                    roomCoords.setVisibility(View.GONE);
                    roomRadius.setVisibility(View.GONE);
                    roomIsPrivate.setVisibility(View.GONE);
                    joinButton.setVisibility(View.GONE);
                    return;
                } else {
                    roomName.setVisibility(View.VISIBLE);
                    roomCoords.setVisibility(View.VISIBLE);
                    roomRadius.setVisibility(View.VISIBLE);
                    joinButton.setVisibility(View.VISIBLE);
                }

                if (roomIdentity.getPassword() != null) {
                    roomIsPrivate.setVisibility(View.VISIBLE);
                }
                //create a ban database listener for the room
                BanController.addRoom(getRef(position).getKey());

                roomName.setText(roomIdentity.getName());
                roomCoords.setText(formatCoords(roomIdentity.getLat(), roomIdentity.getLongg()));
                roomRadius.setText("Radius: " + roomIdentity.getRad() + "m");

                joinButton.setTag(getRef(position).getKey());
            }
        };
        listOfRooms.setAdapter(roomListAdapter);
    }

    private boolean withinRoomRadius(float oLat, float oLng, int radius) {
        if (location == null) return true;
        double myLat = location.getLatitude();
        double myLng = location.getLongitude();
        double meanLat = Math.toRadians(myLat + oLat / 2);
        double kpdLat = 111.13209 - 0.56605*Math.cos(2*meanLat) + 0.00120*Math.cos(4*meanLat);
        double kpdLng = 111.41513*Math.cos(meanLat) - 0.09455*Math.cos(3*meanLat) + 0.00012*Math.cos(5*meanLat);
        double dNS = kpdLat*(myLat - oLat);
        double dEW = kpdLng*(myLng - oLng);
        double distance = 1000 * Math.sqrt(Math.pow(dNS, 2) + Math.pow(dEW, 2));
        //trace("Distance = " + distance + " m");
        return distance  <= radius;
    }

    private String formatCoords(String lat, String lng) {
        float remainder = 0;

        float latf = Math.abs(Float.valueOf(lat));
        int latDegree = (int) latf;
        remainder = 60 * (latf - latDegree);
        int latMinute = Math.abs((int) remainder);
        float latSecond = 60 * (remainder - latMinute);

        float lngf = Math.abs(Float.valueOf(lng));
        int lngDegree = (int) lngf;
        remainder = 60 * (lngf - lngDegree);
        int lngMinute = (int) remainder;
        float lngSecond = 60 * (remainder - lngMinute);

        String ns = (Float.valueOf(lat) >= 0) ? "N" : "S";
        String ew = (Float.valueOf(lng) >= 0) ? "E" : "W";

        return latDegree + "\u00b0 " + latMinute + "\' " + String.format(Locale.getDefault(), "%.0f", latSecond) + "\" " + ns + ", " +
                lngDegree + "\u00b0 " + lngMinute + "\' " + String.format(Locale.getDefault(), "%.0f", lngSecond) + "\" " + ew;
    }

    private static void trace(String message) {
        System.out.println("SelectActivity >> " + message);
    }
}
