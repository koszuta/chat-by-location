package com.cs595.uwm.chatbylocation.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.service.Database;

/**
 * Created by Nathan on 4/23/17.
 */

public class ZoomImageDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout and set to dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.zoom_image_dialog, null);
        builder.setView(dialogView);

        final ImageView userImage = (ImageView) dialogView.findViewById(R.id.userImage);
        final String userId = getArguments().getString("userId");

        Bitmap image = Database.getUserImage(userId);
        if (image != null) {
            userImage.setImageBitmap(image);
        } else {
            userImage.setImageResource(UserIcon.NONE_RESOURCE);
        }

        return builder.create();
    }
}
