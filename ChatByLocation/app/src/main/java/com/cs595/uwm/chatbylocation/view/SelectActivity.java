package com.cs595.uwm.chatbylocation.view;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nathan on 3/13/17.
 */

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

//        if(Database.getUserCurrentRoomID() != null){
//            Intent intent = new Intent(SelectActivity.getContext(), ChatActivity.class);
//            startActivity(intent);
//        }


        Database.initListeners();
        displayRoomList();
    }

    public void joinRoomClick(View view) {
        String roomId = String.valueOf(view.getTag());
        System.out.println("roomId = " + roomId);

        DatabaseReference passwordRef = Database.getRoomIdentityReference().child(roomId).child("password");
        if (Database.getRoomPassword(roomId) != null) {
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


    public void createRoomClick(View view) {
        DialogFragment dialog = new CreateRoomDialog();
        dialog.show(getFragmentManager(), "create room");
    }

    private void displayRoomList() {
        ListView listOfRooms = (ListView) findViewById(R.id.roomList);

        final FirebaseListAdapter<RoomIdentity> adapter = new FirebaseListAdapter<RoomIdentity>(this,
                RoomIdentity.class,
                R.layout.room_list_item,
                FirebaseDatabase.getInstance().getReference().child("roomIdentity")) {
            @Override
            protected void populateView(View view, RoomIdentity roomIdentity, int position) {
                final TextView roomName = (TextView) view.findViewById(R.id.roomName);
                final TextView roomCoords = (TextView) view.findViewById(R.id.roomCoords);
                final TextView roomRadius = (TextView) view.findViewById(R.id.roomRadius);
                final ImageView roomIsPrivate = (ImageView) view.findViewById(R.id.roomIsPrivate);

                if (roomIdentity.getPassword() != null) {
                    roomIsPrivate.setVisibility(View.VISIBLE);
                }

                roomName.setText(roomIdentity.getName());
                roomCoords.setText(roomIdentity.getLongg() + ", " + roomIdentity.getLat());
                roomRadius.setText("Radius: " + roomIdentity.getRad() + "m");

                Button joinButton = (Button) view.findViewById(R.id.joinButton);
                joinButton.setTag(getRef(position).getKey());
            }
        };

        listOfRooms.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("caller", SelectActivity.class.getName());
                startActivity(settingsIntent);
                break;

            case R.id.menu_sign_out:
                AuthUI.getInstance()
                        .signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SelectActivity.this,
                                "You have been signed out.",
                                Toast.LENGTH_LONG)
                                .show();

                        // Close activity
                        finish();
                    }
                });
                break;

            case R.id.menu_bypass:
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
        return true;
    }
}
