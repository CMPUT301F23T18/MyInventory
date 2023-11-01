package com.example.myinventoryapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;



public class AddActivity extends AppCompatActivity {
    ImageView nextButton;
    Button scanButton;
    EditText serialField;
    EditText dateField;
    EditText makeField;
    EditText priceField;
    EditText descField;

    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_item_activity);

        serialField = findViewById(R.id.serial_numb);
        dateField = findViewById(R.id.acquired_da);
        makeField = findViewById(R.id.make);
        priceField = findViewById(R.id.estimated_p);
        descField = findViewById(R.id.description);
        scanButton = findViewById(R.id.scanButtonAdd);
        nextButton = findViewById(R.id.forwardButtonAdd);

        FirebaseFirestore db = FirebaseFirestore.getInstance();



    }

    //TODO: scan function -> scan barcode or scan serial number
    //TODO: next activity -> compile data into a item then move on to tags
    //TODO: back to list -> needs a button first...
}
