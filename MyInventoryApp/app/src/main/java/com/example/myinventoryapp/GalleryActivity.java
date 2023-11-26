package com.example.myinventoryapp;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.grpc.Context;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

// Note: This class is split into 3 sections as follows:
//          - General/Gallery populating
//          - Camera
//          - Permissions
//      I tried to seperate all of them but that didn't work so it is what it is

/**
 * This activity handles using the camera and the phone's photo gallery, additionally it also
 * handles permissions for the above.
 */
public class GalleryActivity extends AppCompatActivity implements CapturePopUp.OnFragmentInteractionListener, EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private static final int CAMERA_PERMISSION_CODE = 1111;
    int animationDuration;
    ImageView image1,image2,image3,image4,image5,image6, capture_btn;
    TextView image_total; int total = 0; int img_idx = 0;
    Button back_btn, save_btn, capture_cam_btn, close_capture;
    ConstraintLayout capture_layout, gallery_layout;
    ArrayList<ImageView> images;
    ArrayList<Bitmap> imageBits = new ArrayList<Bitmap>(6);;
    ActivityResultLauncher<String> galleryGrab;
    PreviewView cam_preview;
    long id;
    Boolean edit_activity;
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
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bit = Bitmap.createBitmap(1,1,conf);
        images.add(image1); imageBits.add(bit);
        images.add(image2); imageBits.add(bit);
        images.add(image3); imageBits.add(bit);
        images.add(image4); imageBits.add(bit);
        images.add(image5); imageBits.add(bit);
        images.add(image6); imageBits.add(bit);


        cam_preview = findViewById(R.id.camPreview);
        capture_layout = findViewById(R.id.captureConstraints);
        capture_layout.setVisibility(View.GONE);
        gallery_layout = findViewById(R.id.images_constraint);
        animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        back_btn = findViewById(R.id.backButton); back_btn.setOnClickListener(this);
        save_btn = findViewById(R.id.saveButtonGallery); save_btn.setOnClickListener(this);
        capture_btn = findViewById(R.id.cameraButton); capture_btn.setOnClickListener(this);
        capture_cam_btn = findViewById(R.id.captureButtonCam); capture_cam_btn.setOnClickListener(this);
        close_capture = findViewById(R.id.closeCaptureButton); close_capture.setOnClickListener(this);

        galleryGrab = registerForActivityResult(
                new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri o) {
                        try {
                            Bitmap image_bit = BitmapFactory.decodeStream(getApplicationContext()
                                    .getContentResolver().openInputStream(o));
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(image_bit, image_bit.getWidth(), image_bit.getHeight(), true);
                            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                            attachToItem(rotatedBitmap);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        if (edit_activity) {
            // This Activity was called as the edit version, populate the gallery right away
            Log.i("PHOTOS","Popluating gallery from id");
            populateFromItem();
        }
    }

    /**
     * Populates the gallery with the photos already assigned to the Item, only called
     * when the activity is accessed from the view item class
     */
    private void populateFromItem() {
        Item item = new Item();
        item.generatePhotoArray(((Global)getApplication()).getPhotoStorageRef(), String.valueOf(id), new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Log.i("FATAL", String.valueOf(item.getImages().size()));
                // set each image view
                imageBits = item.getImages();
                total = 0;
                for (int i = 0; i < imageBits.size(); ++i) {
                    Log.i("FATAL", "index " + i);
                    if (imageBits.get(i) != null) {
                        images.get(i).setImageBitmap(imageBits.get(i));
                        images.get(i).setVisibility(View.VISIBLE);
                        total += 1;
                    }

                }
                String text = total + "/6 Images";
                image_total.setText(text);
            }
        });
    }


    /**
     * Opens the phone's gallery for retrieving pictures
     */
    @Override
    public void onGalleryPressed() {
        galleryGrab.launch("image/*");
    }

    /**
     * Deletes the photo that has been selected
     */
    @Override
    public void onDeletePressed() {
        StorageReference photosRef = ((Global) getApplication()).getPhotoStorageRef();
        // update list on app and firebase
        for (int i = img_idx; i < images.size()-1;++i) {
            // loop for each photo past the deleted one
            if (images.get(i+1).getVisibility() == View.INVISIBLE) {
                images.get(i).setVisibility(View.INVISIBLE);
                photosRef.child(id+"/image"+(i)+".jpg").delete();
                break;
            } else {
                images.get(i).setImageBitmap(imageBits.get(i+1));
                imageBits.set(i, imageBits.get(i+1));
                photosRef.child(id + "/image"+i + ".jpg");
                ((Global)getApplication()).setPhoto(id,imageBits.get(i),"image"+i);
            }

        }
        // change total
        total -= 1;
        String text = total + "/6 Images";
        image_total.setText(text);
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
            cameraProvider.unbindAll();
            finish();
        } else if (vID == R.id.saveButtonGallery) {
            // return to list activity
            cameraProvider.unbindAll();
            Intent i = new Intent(this,ListActivity.class);
            startActivity(i);
        } else if (vID == R.id.captureButtonCam) {
            // The button that appears with the camera preview
            capturePhoto();
            capture_layout.setVisibility(View.GONE);
        } else if (vID == R.id.closeCaptureButton) {
            // The close button that appears with the camera preview
            //capture_layout.setVisibility(View.GONE);
            animateCamera(false);
        }
        if (v.getVisibility() != View.INVISIBLE) {
            if (vID == R.id.image1Edit) {
                img_idx = 0;
                openPopup();
            } else if (vID == R.id.image2Edit) {
                img_idx = 1;
                openPopup();
            } else if (vID == R.id.image3Edit) {
                img_idx = 2;
                openPopup();
            } else if (vID == R.id.image4Edit) {
                img_idx = 3;
                openPopup();
            } else if (vID == R.id.image5Edit) {
                img_idx = 4;
                openPopup();
            } else if (vID == R.id.image6Edit) {
                img_idx = 5;
                openPopup();
            }
        }
    }

    /**
     * opens the dialog popup to ask if they want to capture or select a photo
     */
    private void openPopup() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("onImage",true);
        CapturePopUp popUp = new CapturePopUp();
        popUp.setArguments(bundle);
        popUp.show(getSupportFragmentManager(), "CAP_CHOOSE");
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
            //capture_layout.setVisibility(View.VISIBLE);
            animateCamera(true);
            // Activate the camera
            // Camera set up

            ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderListenableFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        cameraProvider = cameraProviderListenableFuture.get();

                        startCamera(cameraProvider);
                    } catch (ExecutionException | InterruptedException e) {
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
                        attachToItem(rotatedBitmap);
                        animateCamera(false);
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
                        animateCamera(false);
                    }
                });

    }

    /**
     * Takes an image and attaches to a item, increments image total and sends image to firebase
     * @param image_bit the bit map of the image to be attached
     */
    private void attachToItem(Bitmap image_bit) {
        // img_idx is set on view click, either equal to the total or the index of the clicked ImageView
        ImageView image = images.get(img_idx);
        if (imageBits.size() < 6 && imageBits.size() == img_idx) {
            imageBits.add(image_bit);
        }
        imageBits.set(img_idx,image_bit);
        image.setImageBitmap(image_bit);
        image.setVisibility(View.VISIBLE);
        String name = "image" + img_idx;
        total += 1;
        String text = total + "/6 Images";
        image_total.setText(text);

        // send to firebase storage
        ((Global) getApplication()).setPhoto(id,image_bit,name);
    }

    /**
     * fades the one view in and the other view out
     */
    private void animateCamera(boolean opening) {
        View open;
        View close;
        if (opening){
            open = capture_layout;
            close = gallery_layout;
        } else {
            open = gallery_layout;
            close = capture_layout;
        }

        open.setAlpha(0f);
        open.setVisibility(View.VISIBLE);

        open.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);

        close.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    /**
                     * @param animation The animation which reached its end.
                     */
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        close.setVisibility(View.GONE);
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

