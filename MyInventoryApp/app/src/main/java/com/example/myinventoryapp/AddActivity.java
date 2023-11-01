package com.example.myinventoryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AddActivity extends AppCompatActivity {
    ImageView nextButton;
    Button scanButton;
    EditText serialField;
    EditText dateField;
    EditText makeField;
    EditText priceField;
    EditText descField;
    EditText modelField;
    DocumentReference fb_new_item;
    Context context;

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

        nextButton = findViewById(R.id.forwardButtonAdd);
        nextButton.setOnClickListener(nextListener);

        // set a listener for the dateField
        dateField.addTextChangedListener(dateListener);
    }

    private final TextWatcher dateListener = new TextWatcher() {
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
            if (s.length() == 4 || s.length() == 7) {
                //adds "/" to date input so user doesn't have to
                s.append("-");
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

            //TODO: check for valid inputs for date, make sure required fields have data

            // map all inputs to a Hashmap
            Map<String, Object> item_new = new HashMap<String, Object>();
            item_new.put("serial",serial);
            item_new.put("date",date);
            item_new.put("make",make);
            item_new.put("model",model);
            item_new.put("price",price);
            item_new.put("desc",desc);

            Item actual_item = new Item(date,desc,make,model,serial,price);
            item_new.put("item",actual_item);

            // create a document for firebase using the make and model as the name
            fb_new_item = ((Global) getApplication()).DocumentRef(make,model);
            // add the item to firebase
            fb_new_item
                    .set(item_new)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("Firestore","document saved");
                    } else {
                        Log.w("Firestore","failed:",task.getException());
                    }
                }
            });

            //TODO: next activity -> compile data into a item then move on to photos
            //      currently goes back to listActivity

            // go to photo activity
            Intent i = new Intent(v.getContext(), ListActivity.class);
            startActivity(i);
        }
    };

    //Toast.makeText(this,"some text",Toast.LENGTH_SHORT).show()

    //TODO: back to list -> needs a button first...
    //TODO: scan function -> scan barcode or scan serial number
}
