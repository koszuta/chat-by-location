package com.cs595.uwm.chatbylocation.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.Singleton.Database;
import com.cs595.uwm.chatbylocation.Singleton.UserRegistrationInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Jason on 3/15/2017.
 */

public class ChatNameSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_name_select_layout);
    }

    public void registerChatName(View v) {
        boolean isRegistered = false;
        EditText input = (EditText) findViewById(R.id.chatname);
        String chatName = input.getText().toString();

        isRegistered = UserRegistrationInfo.getInstance().setChatName(chatName);

        if(isRegistered) {

            Database.createUser(chatName);

            Intent intent = new Intent(this, SelectActivity.class);
            startActivity(intent);
        }
        else {
            //TODO: display error message
        }


    }
}
