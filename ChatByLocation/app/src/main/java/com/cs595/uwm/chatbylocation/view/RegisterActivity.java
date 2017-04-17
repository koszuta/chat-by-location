package com.cs595.uwm.chatbylocation.view;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Jason on 4/16/2017.
 */

public class RegisterActivity extends AppCompatActivity {
    Context activityContext;
    Toast currentToast;
    boolean isViewClicked = false;
    final long MIN_CLICK_INTERVAL=1000;
    long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        activityContext = this;
    }

    public void onRegisterButtonClick(final View v) {
        //avoid multiple register onclicks! http://stackoverflow.com/questions/32819477/how-to-prevent-rapid-double-click-on-a-button
        v.setEnabled(false);
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;
        mLastClickTime=currentClickTime;
        if(elapsedTime<=MIN_CLICK_INTERVAL)
            return;
        if(!isViewClicked){
            isViewClicked = true;
            startTimer();
        } else {
            return;
        }

        final EditText nameView = (EditText)findViewById(R.id.reg_name);
        final EditText emailView = (EditText)findViewById(R.id.reg_email);
        final EditText passwordView = (EditText)findViewById(R.id.reg_password);
        final EditText confirmView = (EditText)findViewById(R.id.reg_reenter);

        final String name = nameView.getText().toString();
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();
        final String confirm = confirmView.getText().toString();




        //require all fields filled in
        if(name.equals("") || email.equals("") || password.equals("") || confirm.equals("")) {
            updateToast("Please fill in all registration fields in order to sign up.");

        }
        //valid email address
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            updateToast("Please enter a valid email address.");

        }
        //matching passwords
        else if(!password.equals(confirm)) {
            updateToast("Matching passwords are needed to sign up");

        }
        else if(!isUniqueUsername(name)) {
            updateToast("That username is already taken. Please try a different name");

        }
        //all good! register user
        else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Registration", "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                updateToast("Registration Failed. The account may already exist, or an unknown error occurred.");
                                //remove extra username in registration database due to error
                                Database.removeExtraUsernameFromRegistration(name);

                            }
                            else {
                                FirebaseUser newUser = task.getResult().getUser();
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                newUser.updateProfile(profile);
                                FirebaseAuth.getInstance().signOut();
                                updateToast("Thank you for registering! Please sign in again to use Chat By Location");
                                startActivity(new Intent(v.getContext(), MainActivity.class));

                            }
                        }
                    });
        }
        v.setEnabled(true);
    }

    private boolean isUniqueUsername(String username) {
        Database.addUsernameToRegistrationList(username);
        Log.d("Registration", String.valueOf(Registration.getUsernames().size()));
        return Registration.getNameCount(username) <= 1;
    }

    private void updateToast(String text) {
        currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    /**
     * This method delays simultaneous touch events of multiple views.
     */
    private void startTimer() {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                isViewClicked = false;
            }
        }, 1000);

    }

}
