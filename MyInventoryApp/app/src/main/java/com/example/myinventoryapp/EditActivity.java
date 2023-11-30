package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity allows the user to edit the selected item contents as it updates in the firestore
 * database.
 */
public class EditActivity extends AppCompatActivity {
    EditText editSerialField;
    EditText editDateField;
    EditText editMakeField;
    EditText editModelField;
    EditText editPriceField;
    EditText editDescField;
    Button updateButton;
    Button exitButton;
    EditText editcomment;
    long itemId;

    /**
     * Called to initialize UI components. It will also populate the unique user's item data from
     * the firestore database. Once populated, the user is able to edit the text fields of the
     * item and save it. Saved edits will be updated in the database.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
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
        ImageView back = findViewById(R.id.backArrow); back.setOnClickListener(backListener);
        editcomment = findViewById(R.id.comEdit);
        exitButton = findViewById(R.id.backArrow);
        this.itemId = getIntent().getLongExtra("item_id",0);
        populateUIFromFirestore(itemId); // Call the function to populate ui from Firestore

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                String editcom = editcomment.getText().toString();

                // Update the Firestore document with the new data
                updateItemInFirestore(itemId, editedSerial, editedDate, editedMake, editedModel, editedPrice, editedDesc,editcom);

                // Optionally, navigate back to the previous activity or perform other actions
                finish(); // Close the EditActivity after saving
            }

            private void updateItemInFirestore(long itemId, String editedSerial, String editedDate, String editedMake, String editedModel, String editedPrice, String editedDesc, String editcom) {
                DocumentReference itemDocRef = ((Global) getApplication()).DocumentRef(itemId);

                // Create a map to hold the updated item data
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("serial", editedSerial);
                updatedData.put("date", editedDate);
                updatedData.put("make", editedMake);
                updatedData.put("model", editedModel);
                updatedData.put("price", editedPrice);
                updatedData.put("desc", editedDesc);
                updatedData.put("comment", editcom);

                // Update the Firestore document with the edited data
                itemDocRef.update(updatedData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Data updated successfully
                                // You can add code here to handle the success case (e.g., show a confirmation message)
                                Log.d("EditActivity", "Item updated in Firestore");

                                // Navigate back to the view activity
                                navigateBackToViewActivity();
                            } else {
                                // Handle the error
                                Log.e("EditActivity", "Error updating item in Firestore: " + task.getException());
                            }
                        });
            }

            private void navigateBackToViewActivity() {
                finish(); // Close the EditActivity after navigating back
            }


        });

    }

    View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };


    /**
     * Populate UI elements from Firestore data and set them into the appropriate text fields.
     * @param itemId Item ID for the associated item to be retrieved
     */
    // Function to populate ui elements from Firestore data
    private void populateUIFromFirestore(long itemId) {
        DocumentReference itemDocRef = ((Global) getApplication()).DocumentRef(itemId);

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
                    String comment = document.getString("comment");

                    // Populate the ui elements with the retrieved data
                    editSerialField.setText(serial);
                    editDateField.setText(date);
                    editMakeField.setText(make);
                    editModelField.setText(model);
                    editPriceField.setText(price);
                    editDescField.setText(desc);
                    editcomment.setText(comment);
                }
            } else {
                Log.d("EditActivity", "Error getting document: " + task.getException());
            }
        });

    }
}
