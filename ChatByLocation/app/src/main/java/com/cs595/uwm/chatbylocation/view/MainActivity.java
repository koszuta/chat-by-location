package com.cs595.uwm.chatbylocation.view;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context baseContext) {
        super.attachBaseContext(baseContext);
        MultiDex.install(this);
    }

    private static final int SIGN_IN_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //must call before setContentView to hide activity's app title in actionbar
        //getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
    }

    public void signIn(View v) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.AppTheme).build(),
                    SIGN_IN_REQUEST_CODE
            );

        } else {
            startNextCorrectActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {

                startNextCorrectActivity();

            } else {
                //remain in this activity
                //TODO: display sign in error
            }
        }
    }

    public void startNextCorrectActivity(){
        startActivity(Database.getUserUsername() == null ?
                new Intent(this, ChatNameSelectionActivity.class) :
                new Intent(this, SelectActivity.class));
    }


}
