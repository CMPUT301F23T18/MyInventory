package com.example.myinventoryapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class ViewItemActivity extends AppCompatActivity implements DeletePopUp.OnFragmentInteractionListener {
    EditText serialField;
    EditText dateField;
    EditText makeField;
    EditText priceField;
    EditText descField;
    EditText modelField;

    ImageView left_btn, right_btn, imageView;
    DocumentReference fb_view_item;
    long id;
    Item item;
    int img_index = 0;

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

        // set photos
        imageView = findViewById(R.id.imagePreview);
        left_btn = findViewById(R.id.imageLeft); left_btn.setOnClickListener(left_right_listener);
        right_btn = findViewById(R.id.imageRight); right_btn.setOnClickListener(left_right_listener);

        // access the database of default user make and model
        fb_view_item = ((Global) getApplication()).DocumentRef(id);

        // get keys and set values to appropriate text fields
        fb_view_item.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            String date = (String) data.get("date");
            String desc = (String) data.get("desc");
            String make = (String) data.get("make");
            String model = (String) data.get("model");
            String serial = (String) data.get("serial");
            String value = (String) data.get("price");

            serialField.setText(serial);
            dateField.setText(date);
            makeField.setText(make);
            priceField.setText(value);
            descField.setText(desc);
            modelField.setText(model);

            item = new Item(date,desc,make,model,serial,value);
            StorageReference photosRef = ((Global) getApplication()).getPhotoStorageRef();
            item.generatePhotoArray(photosRef, String.valueOf(id), task -> DisplayImage());
        });

        // Back Button
        final Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // Edit Button
        final Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ViewItemActivity.this, EditActivity.class);
                intent2.putExtra("item_id",id);
                startActivity(intent2);
            }
        });

        final Button deleteButton = findViewById(R.id.delete_btn);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                DeletePopUp del_fragment = new DeletePopUp();
                bundle.putString("confirm_text", "Delete item?");
                del_fragment.setArguments(bundle);
                del_fragment.show(getSupportFragmentManager(), "delete_item");
            }
        });

        ImageView camera_btn = findViewById(R.id.cameraButton);
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), GalleryActivity.class);
                // put ID in the intent
                i.putExtra("ID",id);
                i.putExtra("Edit", true);
                startActivity(i);
            }
        });
    }

    @Override
    public void onYESPressed() {
        CollectionReference fb_items = ((Global) getApplication()).getFbItemsRef();
        fb_items.document(Long.toString(id)).delete();
        finish();
        Toast.makeText(ViewItemActivity.this,"Item was deleted" ,Toast.LENGTH_SHORT).show();

    }

    /**
     * Displays the image based on given index
     */
    private void DisplayImage() {
        Bitmap photo = item.getImage(img_index);
        if (photo != null) {
            imageView.setImageBitmap(photo);
        } else {
            imageView.setImageResource(R.drawable.bg_colored_image);
        }
    }

    View.OnClickListener left_right_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.imageLeft) {
                img_index -= 1;
                // handle looping
                if (img_index == -1) {
                    img_index = item.getPhotosSize() -1;
                }

                DisplayImage();
            } else if (v.getId() == R.id.imageRight) {
                img_index += 1;
                // handle looping
                if (img_index == item.getPhotosSize()) {
                    img_index = 0;
                }

                DisplayImage();
            }
        }
    };
}
