package com.cs595.uwm.chatbylocation.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class MapViewFragment extends FragmentActivity implements OnMapReadyCallback {

    public static final double MKE_LAT = 43.052222;
    public static final double MKE_LNG = -87.955833;
    public static final LatLng CENTER_OF_USA = new LatLng(39.83333, -98.58333);
    private GoogleMap mMap;
    private LatLng myLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        // Get location from activity
        double lat = getIntent().getDoubleExtra("myLat", MKE_LAT);
        double lng = getIntent().getDoubleExtra("myLng", MKE_LNG);
        myLatLng = new LatLng(lat, lng);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            trace("Null map fragment");
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map<String, RoomIdentity> rooms = Database.getRooms();
        mMap = googleMap;
        mMap.clear();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMinZoomPreference(3);
        mMap.setMaxZoomPreference(18);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER_OF_USA, mMap.getMinZoomLevel()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 10));

        for (Map.Entry<String, RoomIdentity> entry : rooms.entrySet()) {
            RoomIdentity room = entry.getValue();
            float lat = Float.valueOf(room.getLat());
            float lng = Float.valueOf(room.getLongg());
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(lat, lng))
                    .radius(room.getRad())
                    .fillColor(R.color.colorPrimaryLight)
            );
        }
    }

    private static void trace(String message) {
        System.out.println("MapViewFragment >> " + message);
    }
}
