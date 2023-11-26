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
    ImageView nextButton;
    ImageView backButton;
    Button scanButton;
    EditText serialField;
    EditText dateField;
    EditText makeField;
    EditText priceField;
    EditText descField;
    EditText modelField;
    DocumentReference fb_new_item;
    EditText commentField;
    ArrayList<String> new_item;


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
        scanButton = findViewById(R.id.scanButtonAdd);
        commentField = findViewById(R.id.comments);

        nextButton = findViewById(R.id.forwardButtonAdd);
        nextButton.setOnClickListener(nextListener);

        // set a listener for the dateField
        dateField.addTextChangedListener(dateListener);

        backButton = findViewById(R.id.backButton1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
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
            }

//            // map all inputs to a Hashmap
//            Map<String, Object> item_hash = new HashMap<String, Object>();
//            item_hash.put("serial",serial);
//            item_hash.put("date",date);
//            item_hash.put("make",make);
//            item_hash.put("model",model);
//            item_hash.put("price",price);
//            item_hash.put("desc",desc);
//            item_hash.put("comment",comment);
////
             long ID = System.currentTimeMillis();
//            item_hash.put("ID",ID);
//
//            // create a document for firebase using the make and model as the name
//            fb_new_item = ((Global) getApplication()).DocumentRef(ID);
//            // add the item to firebase
//            fb_new_item.set(item_hash).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if (task.isSuccessful()) {
//                        Log.d("Firestore","document saved");
//                    } else {
//                        Log.w("Firestore","failed:",task.getException());
//                    }
//                }
//            });


            // go to gallery activity
            nextActivity(ID,v, serial, date, make, model, price, desc, comment);
            //nextActivity(ID,v);
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
    //TODO: scan function -> scan barcode or scan serial number
}