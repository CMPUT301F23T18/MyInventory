package com.example.myinventoryapp;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    View image1,image2,image3,image4,image5,image6;
    TextView image_total;
    Button back_btn, save_btn;
    ImageView capture_btn;
    long id;
    Boolean edit_activity;
    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        this.id = getIntent().getLongExtra("ID",0);
        this.edit_activity = getIntent().getBooleanExtra("Edit",false);

        //TODO: I would like to use this activity for both add and edit,
        //      therefore I need to differentiate between the two, probably with
        //      something coming from the intent

        if (id == 0) {
            //Just go back to the add activity because clearly something is wrong
            Intent i = new Intent(this,AddActivity.class);
            startActivity(i);
        }

        //Get all views
        image1 = findViewById(R.id.image1Edit);
        image2 = findViewById(R.id.image2Edit);
        image3 = findViewById(R.id.image3Edit);
        image4 = findViewById(R.id.image4Edit);
        image5 = findViewById(R.id.image5Edit);
        image6 = findViewById(R.id.image6Edit);
        image_total = findViewById(R.id.imageTotal);

        back_btn = findViewById(R.id.backButton);
        save_btn = findViewById(R.id.saveButtonGallery);
        capture_btn = findViewById(R.id.cameraButton);

        //Set capture button -> call popup window
        capture_btn.setOnClickListener(captureListener);
        //TODO: Open camera and save photo
        //TODO: Populate Gallery -> onClickListener for table?
        //TODO: Tap on a photo to give pop up to delete or replace (CapturePopUp)
        //TODO: increment image total
        //TODO: Set Back button
        //TODO: Set Save button

        if (edit_activity) {
            // This Activity was called as the edit version, populate the gallery right away
            populateFromItem();
        }
    }

    /**
     * Opens the phone camera for taking pictures
     */
    public static void handleCamera(View view) {
    }

    /**
     * Opens the phone's gallery for retrieving pictures
     */
    public static void handleGallery(View view) {

    }

    /**
     * Populates the gallery with the photos already assigned to the Item, only called
     * when the activity is accessed from the view item class
     */
    private void populateFromItem() {

    }

    /**
     * Opens the "capture or gallery" popup, called by capture button click or image click
     */
    private void openPopup(View view) {
        CapturePopUp popUp = new CapturePopUp();
        popUp.showWindow(view);
    }

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // opens the popup
            openPopup(v);
        }
    };

}

