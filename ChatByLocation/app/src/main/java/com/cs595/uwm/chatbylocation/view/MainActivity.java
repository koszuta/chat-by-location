package com.cs595.uwm.chatbylocation.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.controllers.MuteController;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;

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
        // these next lines and run it once to reset the preference
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().getString("user_icon", UserIcon.NONE).apply();
        //*/

        //clear disk cache of current user if not logged out so it doesn't populate in database;
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
        }

        MuteController.initMuteList(getApplicationContext());
        Database.initUsersListener();
        Database.initRoomsListener();
    }

    public void signIn(View v) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void register(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

}
