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
import android.widget.ToggleButton;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.MuteController;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Nathan on 3/29/17.
 */

public class MessageDetailsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();

        String username = bundle.getString(ChatActivity.NAME_ARGUMENT);

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.message_details_dialog_layout, null);
        builder.setView(dialogView);

        final TextView user = (TextView) dialogView.findViewById(R.id.userName);
        user.setText(username);

        final ImageView userIcon = (ImageView) dialogView.findViewById(R.id.userImage);
        userIcon.setImageResource(bundle.getInt(ChatActivity.ICON_ARGUMENT));

        final Button banButton = (Button) dialogView.findViewById(R.id.banUser);
        final ImageView userImage = (ImageView) dialogView.findViewById(R.id.userImage);

        final ToggleButton muteButton = (ToggleButton) dialogView.findViewById(R.id.blockUser);
        if(MuteController.isMuted(dialogView.getContext(), username)) {
            //have to swap text values for muted user - toggling it would trigger listener
            muteButton.setTextOff("UNMUTE");
            muteButton.setTextOn("MUTE");
            muteButton.setText("UNMUTE");

        }

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
