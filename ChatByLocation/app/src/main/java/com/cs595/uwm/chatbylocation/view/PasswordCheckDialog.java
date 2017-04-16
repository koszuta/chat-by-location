package com.cs595.uwm.chatbylocation.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Nathan on 4/14/17.
 */

public class PasswordCheckDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.password_check_layout, null);
        builder.setView(dialogView);

        final String roomId = getArguments().getString("roomId");
        final TextView roomName = (TextView) dialogView.findViewById(R.id.passwordRoomName);
        final EditText passwordInput = (EditText) dialogView.findViewById(R.id.passwordInput);

        roomName.setText(Database.getRoomName(roomId));

        builder.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordInput.getText().toString();

                if (password.equals(Database.getRoomPassword(roomId))) {
                    Database.setUserRoom(roomId);
                    startActivity(new Intent(getActivity(), ChatActivity.class));
                }
                else {
                    Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
