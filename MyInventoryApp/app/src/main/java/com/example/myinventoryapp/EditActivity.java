package com.example.myinventoryapp;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore

import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {
    EditText editSerialField;
    EditText editDateField;
    EditText editMakeField;
    EditText editModelField;
    EditText editPriceField;
    EditText editDescField;
    Button updateButton;
    String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initialize UI elements
        editSerialField = findViewById(R.id.serialNumEdit);
        editDateField = findViewById(R.id.acqDateEdit);
        editMakeField = findViewById(R.id.makeEdit);
        editModelField = findViewById(R.id.modelEdit);
        editPriceField = findViewById(R.id.estPriceEdit);
        editDescField = findViewById(R.id.descEdit);
        updateButton = findViewById(R.id.saveButton);

        itemId = getIntent().getStringExtra("item_id");

        populateUIFromFirestore(itemId); // Call the function to populate ui from Firestore

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get edited data from UI elements
                String editedSerial = editSerialField.getText().toString();
                String editedDate = editDateField.getText().toString();
                String editedMake = editMakeField.getText().toString();
                String editedModel = editModelField.getText().toString();
                String editedPrice = editPriceField.getText().toString();
                String editedDesc = editDescField.getText().toString();

                // Update the Firestore document with the new data
                updateItemInFirestore(itemId, editedSerial, editedDate, editedMake, editedModel, editedPrice, editedDesc);

                // Optionally, navigate back to the previous activity or perform other actions
                finish(); // Close the EditActivity after saving
            }

            private void updateItemInFirestore(String itemId, String editedSerial, String editedDate, String editedMake, String editedModel, String editedPrice, String editedDesc) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference itemDocRef = db.collection("your_collection_name").document(itemId); // Replace "your_collection_name" with your Firestore collection name

                // Create a map to hold the updated item data
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("serial", editedSerial);
                updatedData.put("date", editedDate);
                updatedData.put("make", editedMake);
                updatedData.put("model", editedModel);
                updatedData.put("price", editedPrice);
                updatedData.put("desc", editedDesc);

                // Update the Firestore document with the edited data
                itemDocRef.update(updatedData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Data updated successfully
                                // You can add code here to handle the success case (e.g., show a confirmation message)
                                Log.d("EditActivity", "Item updated in Firestore");
                            } else {
                                // Handle the error
                                Log.e("EditActivity", "Error updating item in Firestore: " + task.getException());
                            }
                        });
            }

        });
    }


    // Function to populate ui elements from Firestore data
    private void populateUIFromFirestore(String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Initialize Firestore
        DocumentReference itemDocRef = db.collection("Users/test_user/Items").document(itemId); // Replace "your_collection_name" with your Firestore collection name

        itemDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document found retrieve data
                    String serial = document.getString("serial");
                    String date = document.getString("date");
                    String make = document.getString("make");
                    String model = document.getString("model");
                    String price = document.getString("price");
                    String desc = document.getString("desc");

                    // Populate the ui elements with the retrieved data
                    editSerialField.setText(serial);
                    editDateField.setText(date);
                    editMakeField.setText(make);
                    editModelField.setText(model);
                    editPriceField.setText(price);
                    editDescField.setText(desc);
                }
            } else {
                Log.d("EditActivity", "Error getting document: " + task.getException());
            }
        });

    }
}
