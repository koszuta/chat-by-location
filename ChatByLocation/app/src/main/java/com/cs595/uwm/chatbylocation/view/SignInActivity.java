package com.cs595.uwm.chatbylocation.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.service.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Jason on 4/16/2017.
 */

public class SignInActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        final EditText emailView = (EditText)findViewById(R.id.sign_email);
        emailView.setText(Registration.getLastEmail());
    }

    public void onSignInClick(final View v){
        final EditText emailView = (EditText)findViewById(R.id.sign_email);
        final EditText passwordView = (EditText)findViewById(R.id.sign_password);

        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        //require all fields filled in
        if(email.equals("") || password.equals("")) {
            Toast.makeText(getApplicationContext(), "Please fill in all fields in order to sign in.",
                    Toast.LENGTH_SHORT).show();
        }
        //valid email address
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(), "Please enter a valid email address.",
                    Toast.LENGTH_SHORT).show();
        }
        else {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "The password is incorrect, or the account does not exist.",
                                    Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("SignIn", "createUserWithEmail:onComplete:" + task.isSuccessful());

                                if (task.isSuccessful()) {
                                    startActivity(new Intent(v.getContext(), SelectActivity.class));
                                }
                            }
                        });
        }
    }

}
