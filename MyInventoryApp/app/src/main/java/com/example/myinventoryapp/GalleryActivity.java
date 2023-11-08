package com.example.myinventoryapp;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
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
    ArrayList<ImageView> images;
    TextView image_total; int total = 0; int img_idx = 0;
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
        image1 = findViewById(R.id.image1Edit); image1.setOnClickListener(this);
        image2 = findViewById(R.id.image2Edit); image2.setOnClickListener(this);
        image3 = findViewById(R.id.image3Edit); image3.setOnClickListener(this);
        image4 = findViewById(R.id.image4Edit); image4.setOnClickListener(this);
        image5 = findViewById(R.id.image5Edit); image5.setOnClickListener(this);
        image6 = findViewById(R.id.image6Edit); image6.setOnClickListener(this);
        image_total = findViewById(R.id.imageTotal);

        images = new ArrayList<ImageView>();
        images.add(image1);
        images.add(image2);
        images.add(image3);
        images.add(image4);
        images.add(image5);
        images.add(image6);


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
        //TODO: Populate Gallery -> onClickListener for table?
        //TODO: Get photo from phone gallery -> need permission
        //TODO: Tap on a photo to give pop up to delete or replace (CapturePopUp)
        //TODO: increment image total

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
     * Opens the phone's gallery for retrieving pictures
     */
    @Override
    public void onGalleryPressed() {

    }

    /**
     * Deletes the photo that has been selected
     */
    @Override
    public void onDeletePressed() {

    }

    /**
     * handles all onclick listeners for the activity
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int vID = v.getId();
        if (vID == R.id.cameraButton) {
            if (total == 6) {
                // no available image slots
                Toast.makeText(getApplicationContext(),"Total images reached, tap on an image to delete/replace it",Toast.LENGTH_LONG).show();
                return;
            }
            img_idx = total;
            Bundle bundle = new Bundle();
            bundle.putBoolean("onImage",false);
            CapturePopUp popUp = new CapturePopUp();
            popUp.setArguments(bundle);
            popUp.show(getSupportFragmentManager(), "CAP_CHOOSE");

        } else if (vID == R.id.backButton) {
            // Go back to add activity
            Intent i = new Intent(this,AddActivity.class);
            startActivity(i);
        } else if (vID == R.id.saveButtonGallery) {
            // return to list activity
            Intent i = new Intent(this,ListActivity.class);
            startActivity(i);
        } else if (vID == R.id.captureButtonCam) {
            // The button that appears with the camera preview
            capturePhoto();
            capture_layout.setVisibility(View.GONE);
        } else {
            // triggered by all image clicks
            //TODO: figure out how to set image index for each image
            Bundle bundle = new Bundle();
            bundle.putBoolean("onImage",true);
            CapturePopUp popUp = new CapturePopUp();
            popUp.setArguments(bundle);
            popUp.show(getSupportFragmentManager(), "CAP_CHOOSE");
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

        // Set the name of photo
        long name = System.currentTimeMillis();

        //TODO: save pictures to firebase

        imageCapture.takePicture(ContextCompat.getMainExecutor(getBaseContext()), new ImageCapture.OnImageCapturedCallback() {
            /**
             * called on successful image capture
             * from here we can take the picture from memory and upload it to firebase
             * @param image The captured image
             */
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        Toast.makeText(getApplicationContext(),"Capture successful",Toast.LENGTH_SHORT).show();
                        Bitmap image_bit = image.toBitmap();

                        // must rotate bitmap 90 degrees to get correct orientation
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image_bit, image.getWidth(), image.getHeight(), true);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        attachToItem(rotatedBitmap, name);
                    }

            /**
             * Called on failure
             * informs user there was an error
             * @param exception An {@link ImageCaptureException} that contains the type of error, the
             *                  error message and the throwable that caused it.
             */
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        exception.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Failed to capture image",Toast.LENGTH_SHORT).show();
                    }
                }

        );

    }

    /**
     * Takes an image and attaches to a item, increments image total and sends image to firebase
     * @param image_bit
     */
    private void attachToItem(Bitmap image_bit, long name) {
        // img_idx is set on view click, either equal to the total or the index of the clicked ImageView
        ImageView image = images.get(img_idx);
        image.setImageBitmap(image_bit);
        total += 1;

        ((Global) getApplication()).setPhoto(id,image_bit,name);
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

