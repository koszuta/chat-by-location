package com.cs595.uwm.chatbylocation.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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

/**
 * Created by Nathan on 4/27/17.
 */

public class MockLocationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.mock_location_dialog_layout, null);
        builder.setView(dialogView);

        final EditText latString = (EditText) dialogView.findViewById(R.id.mockLatitude);
        final EditText lngString = (EditText) dialogView.findViewById(R.id.mockLongitude);

        // "Cancel" button action
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strLat = String.valueOf(latString.getText());
                String strLng = String.valueOf(lngString.getText());
                if ("".equals(strLat) || "".equals(strLng)) return;

                final double mockLat = Double.valueOf(strLat);
                final double mockLng = Double.valueOf(strLng);

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(mockLat);
                location.setLongitude(mockLng);
                Activity activity = getActivity();
                if (activity instanceof ChatActivity) {
                    ((ChatActivity) activity).useMockLocation(location);
                }
                else if (activity instanceof SelectActivity) {
                    ((SelectActivity) activity).useMockLocation(location);
                }
            }
        });

        // "Cancel" button action
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity activity = getActivity();
                if (activity instanceof ChatActivity) {
                    ((ChatActivity) activity).useCurrentLocation();
                }
                else if (activity instanceof SelectActivity) {
                    ((SelectActivity) activity).useCurrentLocation();
                }
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
