package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity handles the creation of items and their upload to firebase
 */
public class AddActivity extends AppCompatActivity {
    ImageView nextButton, backButton;
    Button barcodeButton;
    EditText serialField,dateField,makeField,priceField,descField,modelField,commentField;
    DocumentReference fb_new_item;
    ArrayList<String> new_item;
    ActivityResultLauncher<Intent> barcodeGrab;


    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_item_activity);

        // find all text fields
        serialField = findViewById(R.id.serial_numb);
        dateField = findViewById(R.id.acquired_da);
        makeField = findViewById(R.id.make);
        modelField = findViewById(R.id.model);
        priceField = findViewById(R.id.estimated_p);
        descField = findViewById(R.id.description);
        commentField = findViewById(R.id.comments);

        nextButton = findViewById(R.id.forwardButtonAdd); nextButton.setOnClickListener(nextListener);
        barcodeButton = findViewById(R.id.scanBarcodeAdd); barcodeButton.setOnClickListener(barcodeListener);

        // set a listener for the dateField
        dateField.addTextChangedListener(dateListener);

        backButton = findViewById(R.id.backButton1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        barcodeGrab = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Log.i("BARCODE","Result received");
                            Intent data = result.getData();
                            assert data != null;
                            makeField.setText(data.getStringExtra("make"));
                            modelField.setText(data.getStringExtra("model"));
                            descField.setText(data.getStringExtra("desc"));
                        } else {
                            Log.i("BARCODE","Result failed to return or no result sent");
                        }
                    }
                });
    }

    private final TextWatcher dateListener = new TextWatcher() {
        int first = 0;
        int second;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //When empty
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //when a character gets replaced with another character... I think

        }

        @Override
        public void afterTextChanged(Editable s) {
            // everytime text is edited
            // below is from stack overflow
            second = first;
            first = s.length();
            if (s.length() == 4 || s.length() == 7) {
                //check whether a character was added or deleted
                if (first > second) {
                    // a character was added rather than deleted
                    s.append("-");
                }
            } else if (s.length() == 5) {
                if (s.charAt(4) != '-') {
                    // the '-' was deleted and must be replaced
                    s.insert(4,"-");
                }
            } else if (s.length() == 8) {
                if (s.charAt(7) != '-') {
                    s.insert(7,"-");
                }
            }
        }
    };

    View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get all finished strings from user's input
            String serial = serialField.getText().toString();
            String date = dateField.getText().toString();
            String make = makeField.getText().toString();
            String model = modelField.getText().toString();
            String price = priceField.getText().toString();
            String desc = descField.getText().toString();
            String comment = commentField.getText().toString();
            //NOTE: Make is the brand, model is the product

            // check validity of fields
            //check date
            if (!FieldValidator.checkDate(date,getApplicationContext())) {
                return;
            }
            // check make and model
            if (!FieldValidator.checkFieldSize(make)) {
                Toast.makeText(getApplicationContext(),"make is required to proceed",Toast.LENGTH_SHORT).show();
                return;
            }

            if (!FieldValidator.checkFieldSize(model)) {
                Toast.makeText(getApplicationContext(),"model is required to proceed",Toast.LENGTH_SHORT).show();
                return;
            }
            if (!FieldValidator.checkFieldSize(price)) {
                Toast.makeText(getApplicationContext(),"price is required to proceed",Toast.LENGTH_SHORT).show();
                return;
            }

            long ID = System.currentTimeMillis();

            // go to gallery activity
            nextActivity(ID,v, serial, date, make, model, price, desc, comment);
        }
    };

    /**
     * Send user to the GalleryActivity once they finish making an item
     * @param ID ID of the item that was made, needed to get item in Gallery
     * @param v view of the activity
     */
    private void nextActivity(long ID, View v, String serial, String date, String make, String model, String price, String desc, String comment) {
        //Intent i = new Intent(v.getContext(), ListActivity.class);
        Intent i = new Intent(v.getContext(), GalleryActivity.class);
        // put ID in the intent
        i.putExtra("ID",ID);
        i.putExtra("serial", serial);
        i.putExtra("date",date);
        i.putExtra("make",make);
        i.putExtra("model",model);
        i.putExtra("price",price);
        i.putExtra("desc",desc);
        i.putExtra("comment",comment);
        startActivity(i);
    }
    /**
     * Send user to the ListActivity once they finish making an item
     */
    private void backActivity() {
        finish();
    }
    //TODO: scan function -> scan serial number

    View.OnClickListener barcodeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("BARCODE","CLICKED");
            Intent i = new Intent(AddActivity.this, BarcodeActivity.class);
            barcodeGrab.launch(i);
        }
    };

}