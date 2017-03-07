package com.cs595.uwm.chatdemo2;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private static final int SIGN_IN_REQUEST_CODE = 1;

    public static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final long ONE_WEEK_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS;
    public static final long ONE_YEAR_IN_MILLIS = 365 * ONE_DAY_IN_MILLIS;

    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom_view);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Welcome " + user.getDisplayName(), Toast.LENGTH_LONG).show();

            // Load chat room contents
            displayChatMessages();
        }
    }

    public void sendButtonClick(View view) {
        EditText input = (EditText) findViewById(R.id.textInput);

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

        FirebaseDatabase.getInstance().getReference().push().setValue(
                new ChatMessage(input.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                        icon)
        );

        input.setText("");
    }

    public void userIconClick(View view) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.messageList);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get reference to the views of message.xml
                ImageView userIcon = (ImageView) v.findViewById(R.id.userIcon);
                userIcon.setImageResource(model.getMessageIcon());

                TextView messageText = (TextView) v.findViewById(R.id.messageText);

                // Set their text
                String timestamp = formatTimestamp(model.getMessageTime());
                if (timestamp == null) timestamp = "";
                String username = model.getMessageUser();
                if (username == null) username = "no name";

                SpannableString ss = new SpannableString(timestamp + ' ' + username + ": " + model.getMessageText());

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

        listOfMessages.setAdapter(adapter);
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
}
