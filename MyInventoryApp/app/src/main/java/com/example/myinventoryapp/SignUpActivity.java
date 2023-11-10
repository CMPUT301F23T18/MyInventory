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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
                signUp(emailText,passwordText);
            }
        });

    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, user.getUid(),
                                    Toast.LENGTH_SHORT).show();
                            ((Global) getApplication()).setUSER_PATH(user.getUid());
                            Intent intent = new Intent(SignUpActivity.this, ListActivity.class);
                            startActivity(intent);
                        } else {
                            // Sign up failed
                            Log.e(TAG, "createUserWithEmail:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // User with the same email already exists
                                handleExistingAccount(email);
                            } else {
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

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

                                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                    // Account is associated with email/password, prompt user to sign in or use a different email
                                    Toast.makeText(SignUpActivity.this, "An account with this email already exists. Please sign in or use a different email.",
                                            Toast.LENGTH_SHORT).show();
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
