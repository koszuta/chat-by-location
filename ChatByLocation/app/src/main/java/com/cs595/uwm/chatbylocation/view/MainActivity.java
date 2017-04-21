package com.cs595.uwm.chatbylocation.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.objModel.UserIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.Registration;

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

        // If you get a weird ClassCastException you may need to uncomment
        // this and run it once to reset the preference
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("user_icon", UserIcon.NONE).apply();
        //*/

        Database.setUsers(new ArrayList<UserIdentity>());
        Database.initUsersListener();

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
