package com.example.myinventoryapp;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    View image1,image2,image3,image4,image5,image6;
    ArrayList<View> images;
    TextView image_total;
    Button back_btn;
    ImageView capture_btn;
    Button save_btn;
    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        long id = getIntent().getLongExtra("ID",0);

        if (id == 0) {
            //Just go back to the add activity because clearly something is wrong
            Intent i = new Intent(this,AddActivity.class);
            startActivity(i);
        }

        //Get all views

        //TODO: Set capture button -> call popup window
        //TODO: Open camera and save photo
        //TODO: Populate Gallery
        //TODO: Tap on a photo to give pop up to delete or replace (CapturePopUp)
        //TODO: increment image total
        //TODO: Set Back button
        //TODO: Set Save button
    }

    /**
     * Opens the phone camera for taking pictures
     */
    public static void handleCamera() {

    }

    /**
     * Opens the phone's gallery for retrieving pictures
     */
    public static void handleGallery() {

    }
}
