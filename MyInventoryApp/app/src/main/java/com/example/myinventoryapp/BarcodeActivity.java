package com.example.myinventoryapp;

import android.Manifest;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class BarcodeActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, EasyPermissions.PermissionCallbacks {
    PreviewView camView;
    ImageView icon;
    Button back_btn, scan_btn;
    private static final int CAMERA_PERMISSION_CODE = 1111;
    BarcodeScannerOptions options;
    BarcodeScanner scanner;
    ProcessCameraProvider cameraProvider;
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
        back_btn = findViewById(R.id.backButton); back_btn.setOnClickListener(onBackCLicked());
        scan_btn = findViewById(R.id.scanButton); scan_btn.setOnClickListener(onScanClicked());

        // set up barcode scanner
        options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        scanner = BarcodeScanning.getClient(options);

        //TODO: set up camera

        //TODO: scan a barcode from a captured image
        //TODO: get information from the barcode
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
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(camView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(getMainExecutor(),this);

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageAnalysis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Begin the image processing on click
     * @return null
     */
    private View.OnClickListener onScanClicked() {
        // make the button disappear and start the camera, with overlay
        icon.setVisibility(View.VISIBLE);
        scan_btn.setVisibility(View.GONE);
        initializeCamera();
        return null;
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

            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // For each barcode detected get its information
                            for (Barcode barcode: barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

                                int valueType = barcode.getValueType();

                                if (valueType == Barcode.TYPE_URL) {
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    Log.i("BARCODE",url);
                                    //TODO: autofill description of addActivity upon return
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("BARCODE","Failed to scan a barcode");
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

    private View.OnClickListener onBackCLicked() {
        finish();
        return null;
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
