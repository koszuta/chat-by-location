package com.cs595.uwm.chatbylocation.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Nathan on 3/15/17.
 */

public class CreateRoomDialog extends DialogFragment {

    private static final int MIN_RADIUS = 100;
    private static final int MAX_RADIUS = 5000;
    private static final int RADIUS_INCREMENT = 10;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.create_room_dialog_layout, null);
        builder.setView(dialogView);

        // "Create" button with null clickListener (created later)
        builder.setPositiveButton(R.string.create, null);

        // "Cancel" button action
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();

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
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = roomName.getText().toString();
                        System.out.println('<' + name + '>');
                        if ("".equals(name)) {
                            Toast.makeText(getActivity(), "Please enter a name", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int radius = MIN_RADIUS + roomRadius.getProgress() * RADIUS_INCREMENT;

                        String password = null;
                        if (roomIsPrivate.isChecked()) {
                            password = roomPassword.getText().toString();
                            System.out.println('<' + password + '>');
                            if ("".equals(password)) {
                                Toast.makeText(getActivity(), "Please enter a password", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        // TODO: Graham: Get current device location
                        //These can change to whatever kinds of values location actually uses:
                        String longg = "50";
                        String lat = "50";

                        Database.createRoom(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                name, longg, lat, radius, password);

                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;
    }
}
