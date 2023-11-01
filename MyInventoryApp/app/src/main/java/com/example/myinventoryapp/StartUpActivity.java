package com.example.myinventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class StartUpActivity extends AppCompatActivity {
    // NOTE: in AndroidManifest.xml the name ".StartUpActivity" and ".ListActivity" must be switched
    //       or else the app won't open to the startup screen
    //       - Riley
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);
    }
}