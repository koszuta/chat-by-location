package com.cs595.uwm.chatbylocation.view;



import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.MuteController;
import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.cs595.uwm.chatbylocation.service.Database;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Random;

/**
 * Created by Nathan on 3/13/17.
 */

public class ChatActivity extends AppCompatActivity {

    public static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final long ONE_WEEK_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS;
    public static final long ONE_YEAR_IN_MILLIS = 365 * ONE_DAY_IN_MILLIS;
    public static final String NAME_ARGUMENT = "usernameForBundle";
    public static final String ICON_ARGUMENT = "iconForBundle";

    DialogFragment messageDialog;
    ListView messageListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        //construct objects
        messageListView = (ListView) this.findViewById(R.id.messageList);
        messageListView.setItemsCanFocus(false);
        messageDialog = new MessageDetailsDialog();

        displayChatMessages();

        setTitle(Database.getCurrentRoomName());

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMessage message = (ChatMessage)parent.getItemAtPosition(position);
                String messageUser = message.getMessageUser();

                messageDialog = new MessageDetailsDialog();
                Bundle args = new Bundle();
                args.putString(NAME_ARGUMENT, messageUser);
                args.putInt(ICON_ARGUMENT, message.getMessageIcon());
                messageDialog.setArguments(args);
                messageDialog.show(getFragmentManager(), "message details");
            }
        });
    }

    // Nathan TODO: Add user to 'muted' blacklist
    public void onMuteClick(View v) {
        String name = messageDialog.getArguments().getString(NAME_ARGUMENT);
        Context context = v.getContext();

        if(MuteController.isMuted(v.getContext(), name)) {
            MuteController.removeUserFromMuteList(context, name);
        }
        else {
            MuteController.addUserToMuteList(context, name);
        }
        MuteController.printMuteList(context);
    }

    // Nathan TODO: Remove user from current room and put on blacklist
    public void banUserClick(View view) {

    }

    public void userImageClick(View view) {

    }

    public void toBottomClick(View view) {
        ListView list = (ListView) findViewById(R.id.messageList);
        list.smoothScrollToPosition(list.getCount());
    }

    public void sendMessageClick(View view) {

        EditText input = (EditText) findViewById(R.id.textInput);

        String roomID = Database.getCurrentRoomID();
        if (roomID != null) Database.sendChatMessage(new ChatMessage(input.getText().toString(),
                Database.getUserUsername(), getIcon()), roomID);

        input.setText("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                // TODO: Leave chatroom or just show list of rooms?
                Intent intent = new Intent(this, SelectActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_sign_out:
                AuthUI.getInstance()
                        .signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ChatActivity.this,
                                "You have been signed out.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                //return to sign in
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("caller", ChatActivity.class.getName());
                startActivity(settingsIntent);
                break;

            case R.id.leave_room:
                Database.setUserRoom(null);
                startActivity(new Intent(this, SelectActivity.class));
                break;

            case R.id.room_users:
                Intent userIntent = new Intent(this, RoomUserListActivity.class);
                startActivity(userIntent);

            default:
                break;
        }
        return true;
    }

    private void displayChatMessages() {

        ValueEventListener roomIDListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String roomID = String.valueOf(dataSnapshot.getValue());
                trace("roomIDListener sees roomid = " + roomID);

                FirebaseListAdapter<ChatMessage> chatMessageListener = new FirebaseListAdapter<ChatMessage>(
                        ChatActivity.this,
                        ChatMessage.class,
                        R.layout.message,
                        FirebaseDatabase.getInstance().getReference().child("roomMessages").child(roomID)) {
                    @Override
                    protected void populateView(View view, ChatMessage chatMessage, int position) {
                        String username = chatMessage.getMessageUser();

                        final TextView messageText = (TextView) view.findViewById(R.id.messageText);
                        /*
                        if (MuteController.isMuted(username)) {
                            // view.setVisibility(View.GONE);
                            messageText.setText("This user is muted");
                            return;
                        }
                        //*/

                        if (username == null) username = "no name";

                        // Get reference to the views of message.xml
                        ImageView userIcon = (ImageView) view.findViewById(R.id.userIcon);
                        userIcon.setImageResource(chatMessage.getMessageIcon());

                        // Set their text
                        String timestamp = formatTimestamp(chatMessage.getMessageTime());
                        if (timestamp == null) timestamp = "";

                        SpannableString ss;
                        if(MuteController.isMuted(view.getContext(), username)) {
                            ss = new SpannableString(timestamp + ' ' + username + ": " + "--message muted--");
                        }
                        else {
                            ss = new SpannableString(timestamp + ' ' + username + ": " + chatMessage.getMessageText());
                        }
                        StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
                        RelativeSizeSpan timeSize = new RelativeSizeSpan(0.8f);
                        ForegroundColorSpan timeColor = new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.timestamp, null));

                        int timeLength = timestamp.length() + 1;
                        ss.setSpan(timeSize, 0, timeLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        ss.setSpan(timeColor, 0, timeLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        ss.setSpan(bold, timeLength, timeLength + username.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                        // Nathan TODO: Change to message color rather than user color
                        ForegroundColorSpan textColor = new ForegroundColorSpan(Database.getTextColor());
                        ss.setSpan(textColor, timeLength + username.length() + 1, ss.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                        messageText.setText(ss);
                    }
                };

                ListView listOfMessages = (ListView) findViewById(R.id.messageList);
                if (roomID.equals("")) {
                    chatMessageListener.cleanup();
                    listOfMessages.setAdapter(null);
                    trace("roomIDListener removing adapter");
                } else {
                    listOfMessages.setAdapter(chatMessageListener);
                    trace("roomIDListener setting adapter");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        };

        Database.getCurrentUserReference().child("currentRoomID").addValueEventListener(roomIDListener);

    }

    private String formatTimestamp(long timeMillis) {
        String format = "h:mma";

        long timeDiff = System.currentTimeMillis() - timeMillis;
        if (timeDiff >= ONE_YEAR_IN_MILLIS) {
            format = "M/D/YYYY " + format;
        } else if (timeDiff >= ONE_WEEK_IN_MILLIS) {
            format = "D MMM " + format;
        } else if (timeDiff >= ONE_DAY_IN_MILLIS) {
            format = "EEE " + format;
        }

        if (android.text.format.DateFormat.is24HourFormat(this)) {
            format = format.replace("h", "H").replace("a", "");
        }

        Date messageDate = new Date(timeMillis);
        String dateFormatted = (String) android.text.format.DateFormat.format(format, messageDate);
        dateFormatted = dateFormatted.replace("AM", "am").replace("PM", "pm");

        return dateFormatted;
    }

    private int getIcon() {
        int icon = 0;
        switch (new Random().nextInt(5)) {
            case 0:
                icon = R.drawable.ic_bear;
                break;
            case 1:
                icon = R.drawable.ic_dragon;
                break;
            case 2:
                icon = R.drawable.ic_elephant;
                break;
            case 3:
                icon = R.drawable.ic_hippo;
                break;
            case 4:
                icon = R.drawable.ic_koala;
                break;
            default:
                icon = R.mipmap.ic_launcher;
                break;
        }

        return icon;
    }

    private static void trace(String message){
        System.out.println("ChatActivity >> " + message); //todo android logger

    }
}
