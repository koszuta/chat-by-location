package com.cs595.uwm.chatbylocation.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.cs595.uwm.chatbylocation.R;

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
        EditText input = (EditText) findViewById(R.id.chatname);
        String name = input.getText().toString();
//        System.out.println("register chat name@");
//
//        Database.createUser();
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(name)
//                .build();
//        user.updateProfile(profileUpdates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            startActivity(new Intent(ChatNameSelectionActivity.this, SelectActivity.class));
//                        }
//                    }
//                });


    }
}
