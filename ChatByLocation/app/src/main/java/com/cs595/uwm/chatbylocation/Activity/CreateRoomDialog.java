package com.cs595.uwm.chatbylocation.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.Model.RoomIdentity;
import com.cs595.uwm.chatbylocation.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nathan on 3/15/17.
 */

public class CreateRoomDialog extends DialogFragment {

    private static final int MIN_RADIUS = 100;
    private static final int MAX_RADIUS = 1000;
    private static final int RADIUS_INCREMENT = 10;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.create_room_dialog_layout, null);
        builder.setView(dialogView);

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
                int radius = MIN_RADIUS + seekBar.getProgress() * RADIUS_INCREMENT;

                radiusValue.setText(Integer.toString(radius));
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
                EditText roomPassword = (EditText) dialogView.findViewById(R.id.roomPassword);
                if (isChecked) {
                    roomPassword.setEnabled(true);
                }
                else {
                    roomPassword.setEnabled(false);
                }
            }
        });

        // "Create" button action
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = roomName.getText().toString();

                int radius = MIN_RADIUS + roomRadius.getProgress() * RADIUS_INCREMENT;

                String password = null;
                if (roomIsPrivate.isChecked()) {
                    password = roomPassword.getText().toString();
                }

                // TODO: Graham: Get current device location
                Location location = null;

                System.out.println(name + ", " + radius + ", " + location + ", " + password);

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference roomIDRef = dbRef.child("roomIdentity");
                DatabaseReference roomUsersRef = dbRef.child("roomUsers");

                String roomID = roomIDRef.push().getKey();
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference roomIDInst = roomIDRef.child(roomID);
                roomIDInst.setValue(new RoomIdentity(name, "40", "40", radius, password));
//                roomIDInst.child("name").setValue(name);
//                roomIDInst.child("longg").setValue(40); //todo
//                roomIDInst.child("lat").setValue(40); //todo
//                roomIDInst.child("rad").setValue(radius);
//                if(password != null) roomIDInst.child("password").setValue(password);

                DatabaseReference roomUsersInst = roomUsersRef.child(roomID);
                roomUsersInst.child("ownerID").setValue(userID);
                roomUsersInst.child("users").child(userID).setValue(true);

            }
        });

        // "Cancel" button action
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //AlertDialog dialog = builder.create();

        return builder.create();
    }
}
