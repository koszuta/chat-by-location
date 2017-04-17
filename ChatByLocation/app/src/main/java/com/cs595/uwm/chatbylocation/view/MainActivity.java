package com.cs595.uwm.chatbylocation.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.Registration;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static com.cs595.uwm.chatbylocation.controllers.MuteController.MUTE_FILENAME;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //must call before setContentView to hide activity's app title in actionbar
        //getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        Registration.setUsernames(new ArrayList<String>());
        Database.setRegisterUsernameListener();

//        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                Toast.makeText(MainActivity.this, "boop", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        });
        try {
            File file = new File(this.getCacheDir().toString() + MUTE_FILENAME);
            if(!file.exists()) {
                FileOutputStream oStream = this.openFileOutput(MUTE_FILENAME, Context.MODE_PRIVATE);
                String newline = "\n";
                oStream.write(newline.getBytes());
                oStream.close();
            }
        }
        catch (Exception e) {
            Log.d("MainActivity","Error initializing mute file");
        }
    }

    public void signIn(View v) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void register(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

}
