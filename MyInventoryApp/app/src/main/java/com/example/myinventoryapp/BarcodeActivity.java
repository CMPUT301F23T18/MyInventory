package com.example.myinventoryapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class BarcodeActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, EasyPermissions.PermissionCallbacks, View.OnClickListener {
    PreviewView camView;
    ImageView icon;
    Button done_btn, scan_btn, scanAgain_btn;
    EditText makeField,modelField,descField;
    ConstraintLayout camera_layout;
    ImageView footer;
    private static final int CAMERA_PERMISSION_CODE = 1111;
    BarcodeScannerOptions options;
    BarcodeScanner scanner;
    ProcessCameraProvider cameraProvider;
    Barcode PrevCode = null;
    boolean camera_hidden = false;
    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);
        Log.i("BARCODE", "made it to start of onCreate");

        // get views
        icon = findViewById(R.id.barcodeIcon);
        camView = findViewById(R.id.barcodeView);
        footer = findViewById(R.id.footerBarcode);
        done_btn = findViewById(R.id.doneButton); done_btn.setOnClickListener(this);
        scan_btn = findViewById(R.id.scanButton); scan_btn.setOnClickListener(this);
        scanAgain_btn = findViewById(R.id.scanAgain); scanAgain_btn.setOnClickListener(this);
        camera_layout = findViewById(R.id.cameraLayout); camera_layout.setVisibility(View.VISIBLE);

        makeField = findViewById(R.id.MakeBarcode);
        modelField = findViewById(R.id.ModelBarcode);
        descField = findViewById(R.id.DescBarcode);

        // set up barcode scanner
        options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        scanner = BarcodeScanning.getClient(options);
    }

    /**
     * Get the barcode's information from firebase and populates the editText fields hidden
     * under the camera preview. Once complete, hides the camera
     * @param barcode the barcode scanned by the camera
     */
    private void handleBarcode(Barcode barcode) {
        String value = barcode.getRawValue();
        assert value != null;
        //ensure the barcode is read correctly
        //NOTE: check digit is ((sum of odd digits)*3 + (sum of even digits)) mod 10
        // If result is not 0 do 10 - ans
        if (!FieldValidator.validateBarcode(value)) {
            return;
        }
        //For correct barcode, retrieve from firebase
        DocumentReference item_ref = ((Global) getApplication()).getBarcodeItem(value);
        item_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.i("BARCODE", "document retrieved");
                        Map<String, Object> data = document.getData();
                        assert data != null;
                        String make = (String) data.get("Brand");
                        String model = (String) data.get("Name");
                        String desc = (String) data.get("Description");

                        // Populate text fields
                        makeField.setText(make);
                        modelField.setText(model);
                        descField.setText(desc);
                        
                        //camera_layout.setVisibility(View.GONE);
                        //camera_hidden = true;
                        animator(camera_layout,"close");

                    } else {
                        Log.i("BARCODE", "No such document");
                    }
                }
            }
        });


    }

    /**
     * @param v The view that was clicked. 
     */
    @Override
    public void onClick(View v) {
        int vID = v.getId();
        if (vID == R.id.scanButton) {
            // make the button disappear and start the camera, with overlay
            icon.setVisibility(View.VISIBLE);
            scan_btn.setVisibility(View.GONE);
            initializeCamera();
        } else if (vID == R.id.scanAgain) {
            // Reveal the camera so user may scan again
            animator(camera_layout,"open");
        } else if (vID == R.id.doneButton) {
            if (camera_hidden) {
                //TODO: send information back to AddActivity
                Intent data = new Intent();
                data.putExtra("make",makeField.getText().toString());
                data.putExtra("model",modelField.getText().toString());
                data.putExtra("desc",descField.getText().toString());
                setResult(RESULT_OK,data);
                Log.i("BARCODE","sending result back to AddActivity");
                finish();
            } else {
                Log.i("BARCODE","result cancelled");
                setResult(RESULT_CANCELED);
                finish();
            }

        }
    }

    /**
     * Handles various animations for the activity
     * @param focus the view to be animated
     * @param type "open","close" -> determines what happens to focus
     */
    private void animator(View focus, String type) {
        float value = 0;
        if (Objects.equals(type, "close")) {
            value = -800f;
        } else if (Objects.equals(type, "open")) {
            focus.setVisibility(View.VISIBLE);
            scanAgain_btn.setVisibility(View.INVISIBLE);
            camera_hidden = false;
            value = 0f;
        }

        ObjectAnimator animation = ObjectAnimator.ofFloat(focus,"translationX", value);
        animation.addListener(new AnimatorListenerAdapter() {
            /**
             * @param animation The animation which reached its end.
             */
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (Objects.equals(type, "close")) {
                    focus.setVisibility(View.GONE);
                    scanAgain_btn.setVisibility(View.VISIBLE);
                    camera_hidden = true;
                }
            }
        });
        animation.start();
    }

    /**
     * Handles permissions for the camera and initializes the cameraProvider before calling startCamera()
     */
    private void initializeCamera() {
        String permission = Manifest.permission.CAMERA;
        if (EasyPermissions.hasPermissions(this,permission)) {
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
        } else {
            EasyPermissions.requestPermissions(this, "Our App Requires permission to access your camera", CAMERA_PERMISSION_CODE, permission);
        }
    }

    /**
     * Turns on the camera, sets options
     * @param cameraProvider a provider used to bind the lifecycle of cameras
     */
    private void startCamera(ProcessCameraProvider cameraProvider) {
        if (isDestroyed()) {
            Log.i("BARCODE","the activity was destroyed");
            return;
        }
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(camView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(getMainExecutor(),this);

        try {
            cameraProvider.unbindAll();
            if (!isDestroyed()) {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * analyzes the image provided by the camera, new image every frame. Therefore only called
     * when the barcode appears for multiple frames at a time
     * @param imageProxy The image to analyze
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            // convert imageProxy into an InputImage then process it
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // For each barcode detected get its information
                            for (Barcode barcode : barcodes) {
                                if (barcode != PrevCode) {
                                    int valueType = barcode.getValueType();
                                    if (valueType == Barcode.TYPE_PRODUCT) {
                                        Log.i("BARCODE", String.valueOf(barcode.getDisplayValue()));
                                        handleBarcode(barcode);
                                    }
                                }
                                PrevCode = barcode;
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("BARCODE", "Failed to scan a barcode");
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            imageProxy.close();
                        }
                    });
        } else {
            Log.i("BARCODE","Failed to getImage");
        }
    }

    /**
     * Called when the activity is destroyed, unbinds the camera so it can be
     * used for other purposes
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Dealing with permissions for camera

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
