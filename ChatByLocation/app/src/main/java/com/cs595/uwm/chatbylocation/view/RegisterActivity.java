package com.cs595.uwm.chatbylocation.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.objModel.UserIdentity;
import com.cs595.uwm.chatbylocation.service.Database;
import com.cs595.uwm.chatbylocation.service.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Map;

/**
 * Created by Jason on 4/16/2017.
 */

public class RegisterActivity extends AppCompatActivity {
    Context activityContext;
    Toast currentToast;
    boolean isViewClicked = false;
    final long MIN_CLICK_INTERVAL = 1000;
    long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        activityContext = this;

        final EditText nameView = (EditText) findViewById(R.id.reg_name);
        final EditText emailView = (EditText) findViewById(R.id.reg_email);

        nameView.setText(Registration.getLastUsedName());
        emailView.setText(Registration.getLastEmail());
    }

    public void onRegisterButtonClick(final View v) {
        //avoid multiple register onclicks! http://stackoverflow.com/questions/32819477/how-to-prevent-rapid-double-click-on-a-button
        v.setEnabled(false);
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        mLastClickTime = currentClickTime;
        if (elapsedTime <= MIN_CLICK_INTERVAL)
            return;
        if (!isViewClicked) {
            isViewClicked = true;
            startTimer();
        } else {
            return;
        }

        final EditText nameView = (EditText) findViewById(R.id.reg_name);
        final EditText emailView = (EditText) findViewById(R.id.reg_email);
        final EditText passwordView = (EditText) findViewById(R.id.reg_password);
        final EditText confirmView = (EditText) findViewById(R.id.reg_reenter);

        final String name = nameView.getText().toString();
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();
        final String confirm = confirmView.getText().toString();

        Registration.setLastEmail(email);
        Registration.setLastUsedName(name);


        //require all fields filled in
        if (name.equals("") || email.equals("") || password.equals("") || confirm.equals("")) {
            updateToast("Please fill in all registration fields in order to sign up.");

        }
        //valid email address
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            updateToast("Please enter a valid email address.");

        }
        //matching passwords
        else if (!password.equals(confirm)) {
            updateToast("Matching passwords are needed to sign up");

        } else if (!isUniqueUsername(name)) {
            updateToast("That username is already taken. Please try a different name");

        }
        //all good! register user
        else {
            trace("Registering user...");
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Registration", "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                updateToast("Registration Failed. The account may already exist, or an unknown error occurred.");

                            } else {
                                FirebaseUser newUser = task.getResult().getUser();
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                newUser.updateProfile(profile);
                                Database.createUser(name);
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
        Map<String, UserIdentity> users = Database.getUsers();
        if (users != null) {
            for (Map.Entry<String, UserIdentity> entry : users.entrySet()) {
                UserIdentity user = entry.getValue();
                if (username != null && username.equals(user.getUsername())) {
                    Log.d("Register Activity >> ", "Username is NOT unique: " + username);
                    return false;
                }
            }
        }
        Log.d("Register Activity >> ", "Username is unique: " + username);
        return true;
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

    private void trace(String message){
        Log.d("Register Activity >>", message);
    }

}
