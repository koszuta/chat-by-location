package com.cs595.uwm.chatbylocation.view;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

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

public class RoomUserListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_layout);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        // Create dialog on item click which zooms user image
        ListView usersList = (ListView) findViewById(R.id.user_list_view);
        usersList.setItemsCanFocus(false);
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserIdentity user = (UserIdentity) parent.getItemAtPosition(position);
                trace("Clicked user item " + user.getUsername());
                String icon = user.getIcon();
                trace("User icon = " + user.getIcon());
                if (UserIcon.PHOTO.equals(icon)) {
                    String userId = Database.getUserId(user.getUsername());
                    Bundle args = new Bundle();
                    args.putString("userId", userId);
                    DialogFragment dialog = new ZoomImageDialog();
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "zoom image");
                }
            }
        });
        //*/

        displayUsers();
    }
    
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
                ToggleButton muteButton = (ToggleButton) convertView.findViewById(R.id.mute_button_user);

                if(MuteController.isMuted(getContext(), user.getUsername())) {
                    //have to swap text values for muted user - toggling it would trigger listener
                    muteButton.setTextOff("UNMUTE");
                    muteButton.setTextOn("MUTE");
                    muteButton.setText("UNMUTE");
                }
                TextView userName = (TextView) convertView.findViewById(R.id.user_name_in_list);
                if (user != null) {
                    userName.setText(user.getUsername());

                    // Set list item icon
                    ImageView imageView = (ImageView) convertView.findViewById(R.id.icon_in_user_list);
                    String userId = Database.getUserId(user.getUsername());
                    String icon = Database.getUserIcon(userId);
                    int iconRes = UserIcon.getIconResource(icon);
                    if (iconRes == 0) {
                        // Set custom image to ImageView
                        Bitmap image = Database.getUserImage(userId);
                        if (image != null) {
                            imageView.setImageBitmap(image);
                        } else {
                            // No custom image in Storage, use default icon
                            imageView.setImageResource(UserIcon.NONE_RESOURCE);
                        }
                    } else {
                        // Otherwise set icon to ImageView
                        imageView.setImageResource(iconRes);
                    }
                }

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
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("caller", RoomUserListActivity.class.getName());
                startActivity(intent);
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
