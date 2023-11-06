package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;

import java.util.Map;

public class ViewItemActivity extends AppCompatActivity {
    EditText serialField;
    EditText dateField;
    EditText makeField;
    EditText priceField;
    EditText descField;
    EditText modelField;
    DocumentReference fb_view_item;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // get id of the item that was clicked:
        this.id = getIntent().getLongExtra("ID",0);

        // find the IDs of all the list of information when viewing an item, but get it from firebase
        serialField = findViewById(R.id.serialNumEdit);
        dateField = findViewById(R.id.acqDateEdit);
        makeField = findViewById(R.id.makeEdit);
        priceField = findViewById(R.id.estPriceEdit);
        descField = findViewById(R.id.descEdit);
        modelField = findViewById(R.id.modelEdit);

        // access the database of default user make and model
        fb_view_item = ((Global) getApplication()).DocumentRef(id);

        // get keys and set values to appropriate text fields
        fb_view_item.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            serialField.setText((String) data.get("serial"));
            dateField.setText((String) data.get("date"));
            makeField.setText((String) data.get("make"));
            priceField.setText((String) data.get("price"));
            descField.setText((String) data.get("desc"));
            modelField.setText((String) data.get("model"));
        });

        // Back Button
        final Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // Edit Button
        final Button editButton = findViewById(R.id.saveButtonGallery);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ViewItemActivity.this, EditActivity.class);
                intent2.putExtra("item_id",id);
                startActivity(intent2);
            }
        });

    }


}
