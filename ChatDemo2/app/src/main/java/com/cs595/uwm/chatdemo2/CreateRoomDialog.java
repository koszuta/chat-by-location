package com.cs595.uwm.chatdemo2;

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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nathan on 3/15/17.
 */

public class CreateRoomDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.create_room_dialog_layout, null);
        builder.setView(dialogView);

        // Add checkbox listener; enables password field
        CheckBox roomIsPrivate = (CheckBox) dialogView.findViewById(R.id.roomIsPrivate);
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

        // Set "Create" button action
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final EditText roomName = (EditText) dialogView.findViewById(R.id.roomName);
                String name = roomName.getText().toString();

                final SeekBar roomRadius = (SeekBar) dialogView.findViewById(R.id.roomRadius);
                int radius = roomRadius.getProgress();


                final CheckBox roomIsPrivate = (CheckBox) dialogView.findViewById(R.id.roomIsPrivate);
                String password = null;

                if (roomIsPrivate.isChecked()) {
                    final EditText roomPassword = (EditText) dialogView.findViewById(R.id.roomPassword);
                    password = roomPassword.getText().toString();
                }

                // TODO: Graham: Get current device location
                Location location = null;

                System.out.println(name + ", " + radius + ", " + location + ", " + password);

                // TODO: Lowell: Update putting in database
                FirebaseDatabase.getInstance().getReference().push().setValue(
                        new ChatRoom(name, null, radius, password)
                );
            }
        });

        // Set "Cancel" button action
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
