package com.cs595.uwm.chatbylocation.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.MuteController;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.objModel.UserIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class RoomUserListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_layout);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayUsers();
    }

    /**
     * TODO: reimplement populating scrollList using database (currently using mock data)
     */
    private void displayUsers() {

        ListView lV = (ListView) findViewById(R.id.user_list_view);
        final ArrayList<UserIdentity> users = new ArrayList<>();
        //users.add(new UserIdentity("Mock User 1", 0));
        //TODO: order alphabetically by user name

        ArrayAdapter<UserIdentity> itemsAdapter = new ArrayAdapter<UserIdentity>(this, R.layout.user_list_item, users) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                UserIdentity user = getItem(position);
                //create new view if not yet created
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
                }

                TextView userName = (TextView) convertView.findViewById(R.id.user_name_in_list);
                userName.setText(user.getUsername());
                ImageView iV = (ImageView) convertView.findViewById(R.id.icon_in_user_list);
                iV.setImageResource(R.drawable.ic_dragon);
                return convertView;
            }

        };

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //UserIdentity user = dataSnapshot.getValue(UserIdentity.class);
                trace("ds key " + dataSnapshot.getKey());
                trace("ds val " + dataSnapshot.getValue());
                HashMap<String, Object> roomUsers = (HashMap<String, Object>) dataSnapshot.getValue();

                for(String userID : roomUsers.keySet()){
                    users.add(Database.getUserByID(userID));
                }

//                Iterator<String> iterator = roomUsers.keySet().iterator();
//                while (iterator.hasNext()){
//                    String userID = iterator.next();
//                    users.add(new UserIdentity(userID, UserIcon.NONE));
//                }
                //users.add(new UserIdentity((String) dataSnapshot.getValue(), 0));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        Database.getRoomUsersReference().child(Database.getCurrentRoomID())
                .addListenerForSingleValueEvent(usersListener);
        lV.setAdapter(itemsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent roomIntent = new Intent(this, ChatActivity.class);
                startActivity(roomIntent);
                break;
            case R.id.menu_settings:
                Intent n = new Intent(this, SettingsActivity.class);
                startActivity(n);
                break;
            case R.id.menu_sign_out:
                Database.signOutUser();
                //return to sign in
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    public void onMuteClick(View v) {
        String name = Database.getUserUsername();
        Context context = v.getContext();

        if (MuteController.isMuted(v.getContext(), name)) {
            MuteController.removeUserFromMuteList(context, name);
        } else {
            MuteController.addUserToMuteList(context, name);
        }
    }

    private static void trace(String message){
        System.out.println("RoomUserListActivity >> " + message);
    }

}
