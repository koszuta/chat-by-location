package com.cs595.uwm.chatbylocation.view;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.BanController;
import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nathan on 3/13/17.
 */

public class SelectActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int REQUEST_FINE_LOCATION_ACCESS = 19;
    private boolean hasFineLocationAccess = false;

    private GoogleApiClient googleApiClient;
    public static Location location;
    
    private FirebaseListAdapter<RoomIdentity> roomListAdapter =
            new FirebaseListAdapter<RoomIdentity>(
                    SelectActivity.this,
                    RoomIdentity.class,
                    R.layout.room_list_item,
                    Database.getRoomIdentityReference()) {
                @Override
                protected void populateView(View view, RoomIdentity roomIdentity, int position) {
                    //trace("All items enabled: " + this.areAllItemsEnabled());

                    // Get all list item components
                    final TextView roomName = (TextView) view.findViewById(R.id.roomName);
                    final TextView roomCoords = (TextView) view.findViewById(R.id.roomCoords);
                    final TextView roomRadius = (TextView) view.findViewById(R.id.roomRadius);
                    final ImageView roomIsPrivate = (ImageView) view.findViewById(R.id.roomIsPrivate);
                    final ImageView enterSymbol = (ImageView) view.findViewById(R.id.roomEnterSymbol);
                    final Button joinButton = (Button) view.findViewById(R.id.joinButton);
                    final TextView divider = (TextView) view.findViewById(R.id.roomDivider);

                    // Pre-hide them before deciding whether to show room in list
                    roomName.setVisibility(View.GONE);
                    roomCoords.setVisibility(View.GONE);
                    roomRadius.setVisibility(View.GONE);
                    roomIsPrivate.setVisibility(View.GONE);
                    enterSymbol.setVisibility(View.GONE);
                    //joinButton.setVisibility(View.GONE);
                    divider.setVisibility(View.GONE);

                    // If room has bad values leave it hidden and return
                    if (roomIdentity.getLat() == null
                     || roomIdentity.getLongg() == null
                     || roomIdentity.getRad() < 0
                     || roomIdentity.getName() == null
                     || roomIdentity.getOwnerID() == null) {
                        return;
                    }
                    // Check if user is within room radius (with math)
                    float lat = Float.valueOf(roomIdentity.getLat());
                    float lng = Float.valueOf(roomIdentity.getLongg());
                    if (!withinRoomRadius(lat, lng, roomIdentity.getRad())) {
                        //trace("Room " + roomIdentity.getName() + " is out of range");

                        // If user is outside room radius keep it hidden in list
                        return;
                    }
                    // Otherwise show it in the room list
                    else {
                        roomName.setVisibility(View.VISIBLE);
                        roomCoords.setVisibility(View.VISIBLE);
                        roomRadius.setVisibility(View.VISIBLE);
                        enterSymbol.setVisibility(View.VISIBLE);
                        //joinButton.setVisibility(View.VISIBLE);
                        divider.setVisibility(View.VISIBLE);
                    }

                    // If room is private, show lock icon
                    if (roomIdentity.getPassword() != null) {
                        roomIsPrivate.setVisibility(View.VISIBLE);
                    } else {
                        roomIsPrivate.setVisibility(View.GONE);
                    }
                    //create a ban database listener for the room
                    BanController.addRoom(getRef(position).getKey());

                    roomName.setText(roomIdentity.getName());
                    roomCoords.setText(formatCoords(roomIdentity.getLat(), roomIdentity.getLongg()));
                    roomRadius.setText("Radius: " + roomIdentity.getRad() + "m");

                    joinButton.setTag(getRef(position).getKey());
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

        // Make new api client for location api
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Check for location permissions before connecting to location api
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            trace("Location permissions granted at start");
            googleApiClient.connect();
        } else {
            trace("Location permissions denied at start");
            requestFineLocationPermission();
        }

        ListView roomList = (ListView) this.findViewById(R.id.roomList);
        roomList.setItemsCanFocus(false);
        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                trace("Click item " + parent.getItemAtPosition(position));
                Button joinButton = (Button) view.findViewById(R.id.joinButton);
                joinRoomClick(joinButton);
            }
        });

        displayRoomList();
    }
    @Override
    protected void onResume() {
        googleApiClient.connect();
        trace("Reconnected to location api on resume");
        super.onResume();
    }
    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        trace("Disconnected to location api on stop");
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION_ACCESS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    googleApiClient.connect();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location pLocation) {
        trace("Location changed");
        location = pLocation;
        if(roomListAdapter != null) {
            roomListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        trace("Connected to location api");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0)
                .setFastestInterval(0)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(roomListAdapter != null) {
                roomListAdapter.notifyDataSetChanged();
            }
        } else {
            trace("No permissions after location api connection");
            requestFineLocationPermission();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        trace("Location api connection suspended");
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        trace("Failed to connect to location api");
    }

    public void useMockLocation(Location location) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Settings.Secure.ALLOW_MOCK_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.setMockMode(googleApiClient, true);
            LocationServices.FusedLocationApi.setMockLocation(googleApiClient, location);
        }
        else {
            trace("No permission to use mock location");
        }
    }

    public void useCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Settings.Secure.ALLOW_MOCK_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.setMockMode(googleApiClient, false);
        }
        else {
            trace("No permission to use mock location");
        }
    }

    public Location getLastLocation() {
        return location;
    }

    public void joinRoomClick(View view) {
        String roomId = String.valueOf(view.getTag());
        System.out.println("roomId = " + roomId);

        RoomIdentity room = Database.getRoomIdentity(roomId);
        if (room == null) return;

        String roomLat = room.getLat();
        String roomLng = room.getLongg();
        int roomRad = room.getRad();
        // Make sure room has good values first
        if (roomLat == null || roomLng == null || roomRad < 0
                // Check if user is within room radius
                || !withinRoomRadius(Float.valueOf(roomLat), Float.valueOf(roomLng), roomRad)) {
            Toast.makeText(this, "You must be within a room's radius to enter", Toast.LENGTH_LONG).show();
        }
        else if (BanController.isCurrentUserBanned(roomId)) {
            AlertDialog aD = new AlertDialog.Builder(view.getContext())
                    .setTitle("Cannot Join Room")
                    .setMessage("You are banned from this room!")
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

    private void displayRoomList() {
        final ListView listOfRooms = (ListView) findViewById(R.id.roomList);

        listOfRooms.setAdapter(roomListAdapter);

        Database.getRoomIdentityReference().child("roomIdentity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Notify list adapter to update when data changes
                roomListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.set_location:
                DialogFragment locationDialog = new MockLocationDialog();
                locationDialog.show(getFragmentManager(), "set location");
                break;

            case R.id.menu_create_chatroom:
                DialogFragment dialog = new CreateRoomDialog();
                dialog.show(getFragmentManager(), "create room");
                break;

            case R.id.menu_view_map:
                // TODO: Set map view visible here
                break;

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
            default:
                break;
        }
        return true;
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION_ACCESS);
    }

    private boolean withinRoomRadius(float oLat, float oLon, int radius) {
        if (location == null) {
            trace("Null location");
            return false;
        }
        double myLat = location.getLatitude();
        double myLng = location.getLongitude();
        double meanLat = Math.toRadians(myLat + oLat / 2);
        double kpdLat = 111.13209 - 0.56605*Math.cos(2*meanLat) + 0.00120*Math.cos(4*meanLat);
        double kpdLon = 111.41513*Math.cos(meanLat) - 0.09455*Math.cos(3*meanLat) + 0.00012*Math.cos(5*meanLat);
        double dNS = kpdLat*(myLat - oLat);
        double dEW = kpdLon*(myLng - oLon);
        double distance = 1000 * Math.sqrt(Math.pow(dNS, 2) + Math.pow(dEW, 2));
        //trace("Distance = " + distance + " m");
        return distance  <= radius;
    }

    private String formatCoords(String lat, String lon) {
        float remainder = 0;

        float latf = Math.abs(Float.valueOf(lat));
        int latDegree = (int) latf;
        remainder = 60 * (latf - latDegree);
        int latMinute = Math.abs((int) remainder);
        float latSecond = 60 * (remainder - latMinute);

        float lngf = Math.abs(Float.valueOf(lon));
        int lngDegree = (int) lngf;
        remainder = 60 * (lngf - lngDegree);
        int lngMinute = (int) remainder;
        float lngSecond = 60 * (remainder - lngMinute);

        String ns = (Float.valueOf(lat) >= 0) ? "N" : "S";
        String ew = (Float.valueOf(lon) >= 0) ? "E" : "W";

        return latDegree + "\u00b0 " + latMinute + "\' " + String.format(Locale.getDefault(), "%.0f", latSecond) + "\" " + ns + ", " +
                lngDegree + "\u00b0 " + lngMinute + "\' " + String.format(Locale.getDefault(), "%.0f", lngSecond) + "\" " + ew;
    }

    private static void trace(String message) {
        System.out.println("SelectActivity >> " + message);
    }
}
