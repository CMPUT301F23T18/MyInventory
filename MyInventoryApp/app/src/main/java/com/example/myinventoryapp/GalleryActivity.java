package com.example.myinventoryapp;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

// Note: This class is split into 3 sections as follows:
//          - General/Gallery populating
//          - Camera
//          - Permissions
//      I tried to seperate all of them but that didn't work so it is what it is


public class GalleryActivity extends AppCompatActivity implements CapturePopUp.OnFragmentInteractionListener, EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private static final int CAMERA_PERMISSION_CODE = 1111;
    private static final int GALLERY_PERMISSION_CODE = 2222;
    ImageView image1,image2,image3,image4,image5,image6;
    TextView image_total;
    Button back_btn, save_btn, capture_cam_btn;
    PreviewView cam_preview;
    ImageView capture_btn;
    long id;
    Boolean edit_activity;
    ConstraintLayout capture_layout;
    ProcessCameraProvider cameraProvider;
    ImageCapture imageCapture;
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

        cam_preview = findViewById(R.id.camPreview);
        capture_layout = findViewById(R.id.captureConstraints);
        capture_layout.setVisibility(View.GONE);

        back_btn = findViewById(R.id.backButton);
        save_btn = findViewById(R.id.saveButtonGallery);
        capture_btn = findViewById(R.id.cameraButton);
        capture_cam_btn = findViewById(R.id.captureButtonCam);


        //Set capture button -> call popup window
        capture_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        save_btn.setOnClickListener(this);
        capture_cam_btn.setOnClickListener(this);
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
     * Populates the gallery with the photos already assigned to the Item, only called
     * when the activity is accessed from the view item class
     */
    private void populateFromItem() {

    }

    /**
     * Opens the "capture or gallery" popup, called by capture button click or image click
     */
    private void openPopup() {
        new CapturePopUp().show(getSupportFragmentManager(), "CAP_CHOOSE");
    }


    /**
     * Opens the phone's gallery for retrieving pictures
     */
    @Override
    public void onGalleryPressed() {

    }

    /**
     * handles all onclick listeners for the activity
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int vID = v.getId();
        if (vID == R.id.cameraButton) {
            openPopup();
        } else if (vID == R.id.backButton) {
            // Go back to add activity
            Intent i = new Intent(this,AddActivity.class);
            startActivity(i);
        } else if (vID == R.id.saveButtonGallery) {
            // return to list activity
            //TODO: save pictures to firebase
            Intent i = new Intent(this,ListActivity.class);
            startActivity(i);
        } else if (vID == R.id.captureButtonCam) {
            // The button that appears with the camera preview
            capturePhoto();
            capture_layout.setVisibility(View.GONE);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Dealing with the camera

    /**
     * Opens the phone camera for taking pictures, called when dialog fragment is closed
     */
    @Override
    public void onCapturePressed() {
        // check if app has permission to use the camera, else ask for permission
        String permission = Manifest.permission.CAMERA;

        if (EasyPermissions.hasPermissions(this,permission)) {
            capture_layout.setVisibility(View.VISIBLE);
            // Activate the camera
            // Camera set up

            ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderListenableFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        cameraProvider = cameraProviderListenableFuture.get();

                        startCamera(cameraProvider);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, ContextCompat.getMainExecutor(this));

        }  else {
            EasyPermissions.requestPermissions(this, "Our App Requires permission to access your camera", CAMERA_PERMISSION_CODE, permission);
        }
    }

    /**
     * Turns on the camera, sets options
     * @param cameraProvider a provider used to bind the lifecycle of cameras
     */
    private void startCamera(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cam_preview.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses the camera to take a picture and upload it to the phone's storage, as well as firestore
     */
    private void capturePhoto() {
        if (imageCapture == null) return;

        // The name of the photo will the the id of its item
        String name = System.currentTimeMillis() + "";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(getApplicationContext(), "Image captured!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });

    }




//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Dealing with permissions for camera/Gallery

    /**
     * Prompts the user to giver permission to access the camera, and photo picker
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * @param requestCode The request code passed in
     * @param perms the required permissions
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    /**
     * @param requestCode The request code passed in
     * @param perms the required permissions
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){

            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}

