package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * LoginActivity class is responsible for handling user login functionality.This activity provides
 * UI components for the user to enter their username and password, as well as a login method
 * utilizing Firebase Authentication. The user is brought to the ListActivity after successfully
 * logging in. In the event of an authentication failure, the user is redirected to the
 * StartUpActivity.
 */
public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * When the activity is created, this method is called. Sets up click listeners for the back and
     * login buttons, as well as initializes Firebase Authentication.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        db = FirebaseFirestore.getInstance();

        EditText username = findViewById(R.id.usernameButton);
        EditText password = findViewById(R.id.PasswordButton);

        mAuth = FirebaseAuth.getInstance();
        Button backButton = findViewById(R.id.doneButton);
        Button loginButton = findViewById(R.id.LoginButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainScreen = new Intent(LoginActivity.this, StartUpActivity.class);
                startActivity(mainScreen);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userText = username.getText().toString();
                String passwordText = password.getText().toString();
                if (userText.isEmpty() || passwordText.isEmpty()) {
                    // Display a Toast if any of the fields is empty
                    Toast.makeText(LoginActivity.this, "Both username and password are required", Toast.LENGTH_SHORT).show();
                } else if (passwordText.length() < 6) {
                    // Display a Toast if the password is less than 6 characters
                    Toast.makeText(LoginActivity.this, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with login if all conditions are met
                    logIn(userText, passwordText);
                }
            }
        });
    }

    /**
     * Starts the login process with Firebase Authentication.
     * @param email    The email address of the user.
     * @param password The password of the user.
     */
    private void logIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        ((Global) getApplication()).setUSER_PATH(user.getUid());
                        Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails
                        if (task.getException() != null) {
                            // Check the exception to determine the specific reason for failure
                            String errorMessage = task.getException().getMessage();

                            // Display a Toast if the email is not in the proper format
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException
                                    && "The email address is badly formatted.".equals(errorMessage)) {
                                Toast.makeText(LoginActivity.this, "Invalid email format. Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Display a generic authentication failed Toast
                                Toast.makeText(LoginActivity.this, "Authentication Failed, Try Google SignIn/Signing Up", Toast.LENGTH_SHORT).show();
                                // Redirect the user to the StartUpActivity
                                Intent intent = new Intent(LoginActivity.this, StartUpActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }
}
