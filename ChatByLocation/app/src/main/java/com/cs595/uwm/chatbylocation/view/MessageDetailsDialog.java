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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Nathan on 3/29/17.
 */

public class MessageDetailsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.message_details_dialog_layout, null);
        builder.setView(dialogView);

        final Button banButton = (Button) dialogView.findViewById(R.id.banUser);
        final ImageView userImage = (ImageView) dialogView.findViewById(R.id.userImage);

        // Nathan TODO: Set ImageView to selected user's image
        // userImage.setImageResource(R.drawable.chat_logo_no_back);

        // Nathan TODO: Make 'Ban' button visible when user is admin of current room
        /*
        if (user == admin) {
            banButton.setVisibility(Button.VISIBLE);
        }
        //*/

        return builder.create();
    }
}
