package com.cs595.uwm.chatbylocation.view;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.objModel.ChatMessage;
import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.objModel.RoomIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        // TODO: Nathan: Change to correct current room name method
        setTitle(Database.getUserCurrentRoomID());

        displayChatMessages();
    }

    public void messageClick(View view) {
        System.out.println("CLICK");
    }

    public void sendMessageButton(View view) {

        ValueEventListener roomChangeListener = new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomID = String.valueOf(dataSnapshot.getValue());

                EditText input = (EditText) findViewById(R.id.textInput);

                Database.sendChatMessage(new ChatMessage(input.getText().toString(),
                        Database.getUserUsername(), getIcon()), roomID);

                input.setText("");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        };

        DatabaseReference userRoomRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentRoomID");
        userRoomRef.addValueEventListener(roomChangeListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
                        Toast.makeText(ChatActivity.this,
                                "You have been signed out.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                //return to sign in
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                break;
            case R.id.leave_room:
//                Database.removeUserFromRoom();
//                Intent selectIntent = new Intent(this, MainActivity.class);
//                startActivity(selectIntent);
                break;
            default:
                break;
        }
        return true;
    }

    private void displayChatMessages() {

        ValueEventListener roomChangeListener = new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String roomID = String.valueOf(dataSnapshot.getValue());

                FirebaseListAdapter<ChatMessage> chatMessageListener = new FirebaseListAdapter<ChatMessage>(
                        ChatActivity.this,
                        ChatMessage.class,
                        R.layout.message,
                        FirebaseDatabase.getInstance().getReference().child("roomMessages").child(roomID)) {
                    @Override
                    protected void populateView(View view, ChatMessage chatMessage, int position) {
                        // Get reference to the views of message.xml
                        ImageView userIcon = (ImageView) view.findViewById(R.id.userIcon);
                        userIcon.setImageResource(chatMessage.getMessageIcon());

                        TextView messageText = (TextView) view.findViewById(R.id.messageText);

                        // Set their text
                        String timestamp = formatTimestamp(chatMessage.getMessageTime());
                        if (timestamp == null) timestamp = "";
                        String username = chatMessage.getMessageUser();
                        if (username == null) username = "no name";

                        SpannableString ss = new SpannableString(timestamp + ' ' + username + ": " + chatMessage.getMessageText());

                        StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
                        RelativeSizeSpan timeSize = new RelativeSizeSpan(0.8f);
                        ForegroundColorSpan timeColor = new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.timestamp, null));

                        int timeLength = timestamp.length() + 1;
                        ss.setSpan(timeSize, 0, timeLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        ss.setSpan(timeColor, 0, timeLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        ss.setSpan(bold, timeLength, timeLength + username.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                        messageText.setText(ss);
                    }
                };

                ListView listOfMessages = (ListView) findViewById(R.id.messageList);
                listOfMessages.setAdapter(chatMessageListener);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        };

        DatabaseReference userRoomRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentRoomID");
        userRoomRef.addValueEventListener(roomChangeListener);


    }

    private String formatTimestamp(long timeMillis) {
        String format = "h:mma";

        long timeDiff = System.currentTimeMillis() - timeMillis;
        if (timeDiff >= ONE_YEAR_IN_MILLIS) {
            format = "M/D/YYYY " + format;
        }
        else if (timeDiff >= ONE_WEEK_IN_MILLIS) {
            format = "D MMM " + format;
        }
        else if (timeDiff >= ONE_DAY_IN_MILLIS) {
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
        switch(new Random().nextInt(5)) {
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

}
