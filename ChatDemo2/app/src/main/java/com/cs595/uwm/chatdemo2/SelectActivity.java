package com.cs595.uwm.chatdemo2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nathan on 3/13/17.
 */

public class SelectActivity extends AppCompatActivity {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

        displayRoomList();

        context = getApplicationContext();
    }

    public void joinRoomClick(View view) {

    }

    public void createRoomClick(View view) {
        DialogFragment dialog = new CreateRoomDialog();
        dialog.show(getFragmentManager(), "create room");
    }

    private void displayRoomList() {
        ListView listOfRooms = (ListView) findViewById(R.id.roomList);

        FirebaseListAdapter<ChatRoom> adapter = new FirebaseListAdapter<ChatRoom>(this,
                ChatRoom.class,
                R.layout.room_list_item,
                FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View view, ChatRoom chatroom, int position) {
                TextView roomName = (TextView) view.findViewById(R.id.roomName);
                TextView roomCoords = (TextView) view.findViewById(R.id.roomCoords);
                TextView roomRadius = (TextView) view.findViewById(R.id.roomRadius);

                roomName.setText(chatroom.getName());

                Location location = chatroom.getLocation();
                String coords = "no coords";
                if (location != null) {
                    coords = location.getLatitude() + ", " + location.getLongitude();
                }
                roomCoords.setText(coords);
                roomRadius.setText("Radius: " + chatroom.getRadius() + "m");
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
