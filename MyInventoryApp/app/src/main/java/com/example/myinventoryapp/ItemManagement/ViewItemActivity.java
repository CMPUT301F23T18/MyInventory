package com.example.myinventoryapp.ItemManagement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myinventoryapp.DatabaseHandler;
import com.example.myinventoryapp.Popups.DeletePopUp;
import com.example.myinventoryapp.R;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is a class that allows you to view an item when the item is clicked from the list
 */

public class ViewItemActivity extends AppCompatActivity implements DeletePopUp.OnFragmentInteractionListener {
    EditText serialField,dateField,makeField,priceField,descField,modelField,commentField;
    ImageView left_btn, right_btn, imageView;
    DocumentReference fb_view_item;
    long id;
    Item item;
    int img_index = 0, num_of_imgs;

    /**
     * This is called to initialize UI components
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // get id of the item that was clicked:
        this.id = getIntent().getLongExtra("ID",0);
        this.num_of_imgs = getIntent().getIntExtra("NumofImages",0);

        // find the IDs of all the list of information when viewing an item, but get it from firebase
        serialField = findViewById(R.id.serialNumEdit);
        dateField = findViewById(R.id.acqDateEdit);
        makeField = findViewById(R.id.makeEdit);
        priceField = findViewById(R.id.estPriceEdit);
        descField = findViewById(R.id.descEdit);
        modelField = findViewById(R.id.modelEdit);
        commentField = findViewById(R.id.comEdit);

        // set photos
        imageView = findViewById(R.id.imagePreview);
        left_btn = findViewById(R.id.imageLeft); left_btn.setOnClickListener(left_right_listener);
        right_btn = findViewById(R.id.imageRight); right_btn.setOnClickListener(left_right_listener);

        // access the database of default user make and model
        fb_view_item = ((DatabaseHandler) getApplication()).DocumentRef(id);

        // get keys and set values to appropriate text fields
        fb_view_item.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            assert data != null;

            String serial = (String) data.get("serial");
            String make = (String) data.get("make");
            String model = (String) data.get("model");
            String date = (String) data.get("date");
            String value = (String) data.get("price");
            String desc = (String) data.get("desc");
            String comm = (String) data.get("comment");

            populateField(serialField,serial);
            dateField.setText((String) data.get("date"));
            makeField.setText((String) data.get("make"));
            modelField.setText((String) data.get("model"));
            priceField.setText((String) data.get("price"));
            populateField(descField,desc);
            populateField(commentField,comm);



            List<String> tags = new ArrayList<>();
            if (data.containsKey("tags")){
                tags = (List<String>)data.get("tags");
            }



            item = new Item(date,desc,make,model,serial,value);
            item.setTags(tags);
            StorageReference photosRef = ((DatabaseHandler) getApplication()).getPhotoStorageRef();
            item.generatePhotoArray(photosRef, String.valueOf(id), task -> DisplayImage());

        });

        // Back Button
        final Button backButton = findViewById(R.id.doneButton);
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

        // Delete Button
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

        // Tag button
        Button tag_button = findViewById(R.id.add_tag_button);
        tag_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Item> listToAdd = new ArrayList<>();
                item.setID(id);
                Log.d("Tags Id", String.valueOf(item.getID()));
                listToAdd.add(item);
                Intent i = new Intent(ViewItemActivity.this, TagsActivity.class);
                i.putParcelableArrayListExtra("items", listToAdd);
                startActivity(i);
                finish();
            }
        });
    }

    private void populateField(EditText field, String text) {
        if (text != null) {
            field.setText(text);
        }
    }

    /**
     * This deletes the item from the firestore cloud database.
     */
    @Override
    public void onYESPressed() {
        CollectionReference fb_items = ((DatabaseHandler) getApplication()).getFbItemsRef();
        StorageReference photoRef = ((DatabaseHandler) getApplication()).getPhotoStorageRef();
        if (num_of_imgs>0){
            for(int i = 0; i < num_of_imgs;i++){
                photoRef.child(id+"/image"+(i)+".jpg").delete();
            }
        }
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
            imageView.setImageResource(R.drawable.no_image);
        }
    }


    /**
     * Refreshes the activity to show the current data populated from the firestore database
     * everytime this activity is shown.
     */
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
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
