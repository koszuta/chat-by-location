package com.cs595.uwm.chatdemo2;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nathan on 3/13/17.
 */

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

        displayRoomList();
    }

    public void joinRoomButton(View view) {

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
                String coords = location.getLatitude() + ", " + location.getLongitude();
                roomCoords.setText(coords);
                roomRadius.setText("Radius: " + chatroom.getRadius() + "m");
            }
        };

        listOfRooms.setAdapter(adapter);
    }
}
