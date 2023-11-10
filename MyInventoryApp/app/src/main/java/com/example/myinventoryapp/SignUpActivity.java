package com.example.myinventoryapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Class for handling Sign Up
 */
public class SignUpActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private FirebaseFirestore db;
    /**
     * Initializes the activity, sets up UI elements, and initializes Firebase components.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        db = FirebaseFirestore.getInstance();

        EditText username = findViewById(R.id.username);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
        Button backButton = findViewById(R.id.backButton);
        Button signUpButton = findViewById(R.id.LoginButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivity = new Intent(SignUpActivity.this, StartUpActivity.class);
                startActivity(startActivity);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userText = username.getText().toString();
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                if (userText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                    // Display a Toast if any of the fields is empty
                    Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (passwordText.length() < 6) {
                    // Display a Toast if the password is less than 6 characters
                    Toast.makeText(SignUpActivity.this, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with sign up if all conditions are met
                    signUp(emailText, passwordText);
                }
            }
        });

    }
    /**
     * Handles the user registration process using the provided email and password.
     *
     * @param email The user's email for registration.
     * @param password The user's password for registration.
     */
    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            ((Global) getApplication()).setUSER_PATH(user.getUid());
                            Intent intent = new Intent(SignUpActivity.this, ListActivity.class);
                            startActivity(intent);
                        } else {
                            // Sign up failed
                            Log.e(TAG, "createUserWithEmail:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException
                                    && "The email address is badly formatted.".equals(task.getException().getMessage())) {
                                // Display a Toast if the email is not in the proper format
                                Toast.makeText(SignUpActivity.this, "Invalid email format. Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // User with the same email already exists
                                handleExistingAccount(email);
                            } else {
                                // Display a generic authentication failed Toast
                                Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    /**
     * Handles the scenario where a user with the provided email already has an existing account.
     *
     * @param email The email associated with the existing account.
     */
    private void handleExistingAccount(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null) {
                                List<String> signInMethods = result.getSignInMethods();
                                Log.d(TAG, "Sign-in methods: " + signInMethods.toString());
                                Toast.makeText(SignUpActivity.this, "You are already Signed up.Please use google SignIn or log in using your password.", Toast.LENGTH_LONG).show();
                                // Redirect the user to the general sign-in activity or handle other scenarios
                                Intent intent1 = new Intent(SignUpActivity.this, StartUpActivity.class);
                                startActivity(intent1);
                                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                    // Account is associated with email/password, prompt user to sign in or use a different email
                                    Toast.makeText(SignUpActivity.this, "An account with this email already exists. Please sign in or use a different email.",
                                            Toast.LENGTH_SHORT).show();
                                    Toast.makeText(SignUpActivity.this, "We failed2.", Toast.LENGTH_SHORT).show();
                                    // Redirect the user to the general sign-in activity or handle other scenarios
                                    Intent intent = new Intent(SignUpActivity.this, StartUpActivity.class);
                                    startActivity(intent);
                                } else if (signInMethods.contains("google.com")) {
                                    // Account is associated with Google, handle accordingly
                                    Toast.makeText(SignUpActivity.this, "An account with this email already exists. Please sign in with Google.",
                                            Toast.LENGTH_SHORT).show();
                                    // Redirect the user to the Google sign-in activity or handle other Google-specific scenarios
                                    Intent intent = new Intent(SignUpActivity.this, StartUpActivity.class);
                                    startActivity(intent);
                                }
                            }
                        } else {
                            // Error while checking for existing accounts
                            Log.e(TAG, "Error checking for existing accounts: " + task.getException());
                            Toast.makeText(SignUpActivity.this, "Error checking for existing accounts.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
