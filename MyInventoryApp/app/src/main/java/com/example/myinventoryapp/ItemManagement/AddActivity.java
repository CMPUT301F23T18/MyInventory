package com.example.myinventoryapp.ItemManagement;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.myinventoryapp.FieldValidator;
import com.example.myinventoryapp.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * This activity handles the creation of items and their upload to firebase
 */
public class AddActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    ImageView nextButton, backButton;
    Button barcodeButton, captureButton, closeButton;
    EditText serialField,dateField,makeField,priceField,descField,modelField,commentField;
    DocumentReference fb_new_item;
    ArrayList<String> new_item;
    ConstraintLayout capture_layout , add_layout;
    ActivityResultLauncher<Intent> barcodeGrab;
    ImageCapture imageCapture;
    ProcessCameraProvider cameraProvider;


    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_item_activity);

        // find all text fields
        serialField = findViewById(R.id.serial_numb);
        dateField = findViewById(R.id.acquired_da);
        makeField = findViewById(R.id.make);
        modelField = findViewById(R.id.model);
        priceField = findViewById(R.id.estimated_p);
        descField = findViewById(R.id.description);
        commentField = findViewById(R.id.comments);

        add_layout = findViewById(R.id.addConstraints);
        capture_layout = findViewById(R.id.captureConstraints);

        nextButton = findViewById(R.id.forwardButtonAdd);
        nextButton.setOnClickListener(nextListener);

        nextButton = findViewById(R.id.forwardButtonAdd); nextButton.setOnClickListener(nextListener);
        barcodeButton = findViewById(R.id.scanBarcodeAdd); barcodeButton.setOnClickListener(barcodeListener);

        Button scanSerialButton = findViewById(R.id.serialScanButton);
        scanSerialButton.setOnClickListener(scanserial);
        captureButton = findViewById(R.id.captureButtonCam); captureButton.setOnClickListener(capListener);
        closeButton = findViewById(R.id.closeCaptureButton); closeButton.setOnClickListener(closeListener);


        // set a listener for the dateField
        dateField.addTextChangedListener(dateListener);

        backButton = findViewById(R.id.backButton1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        barcodeGrab = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Log.i("BARCODE","Result received");
                            Intent data = result.getData();
                            assert data != null;
                            makeField.setText(data.getStringExtra("make"));
                            modelField.setText(data.getStringExtra("model"));
                            descField.setText(data.getStringExtra("desc"));
                        } else {
                            Log.i("BARCODE","Result failed to return or no result sent");
                        }
                    }
                });
    }



    private final TextWatcher dateListener = new TextWatcher() {
        int first = 0;
        int second;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //When empty
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //when a character gets replaced with another character... I think

        }

        @Override
        public void afterTextChanged(Editable s) {
            // everytime text is edited
            // below is from stack overflow
            second = first;
            first = s.length();
            if (s.length() == 4 || s.length() == 7) {
                //check whether a character was added or deleted
                if (first > second) {
                    // a character was added rather than deleted
                    s.append("-");
                }
            } else if (s.length() == 5) {
                if (s.charAt(4) != '-') {
                    // the '-' was deleted and must be replaced
                    s.insert(4,"-");
                }
            } else if (s.length() == 8) {
                if (s.charAt(7) != '-') {
                    s.insert(7,"-");
                }
            }
        }
    };

    View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get all finished strings from user's input
            String serial = serialField.getText().toString();
            String date = dateField.getText().toString();
            String make = makeField.getText().toString();
            String model = modelField.getText().toString();
            String price = priceField.getText().toString();
            String desc = descField.getText().toString();
            String comment = commentField.getText().toString();
            //NOTE: Make is the brand, model is the product

            // check validity of fields
            //check date
            if (!FieldValidator.checkDate(date,getApplicationContext())) {
                return;
            }
            // check make and model
            if (!FieldValidator.checkFieldSize(make)) {
                Toast.makeText(getApplicationContext(),"make is required to proceed",Toast.LENGTH_SHORT).show();
                return;
            }

            if (!FieldValidator.checkFieldSize(model)) {
                Toast.makeText(getApplicationContext(),"model is required to proceed",Toast.LENGTH_SHORT).show();
                return;
            }
            if (!FieldValidator.checkFieldSize(price)) {
                Toast.makeText(getApplicationContext(),"price is required to proceed",Toast.LENGTH_SHORT).show();
                return;
            }
            String recognizedText = serialField.getText().toString();


            long ID = System.currentTimeMillis();

            // go to gallery activity
            nextActivity(ID,v, serial, date, make, model, price, desc, comment);
        }
    };

    /**
     * Send user to the GalleryActivity once they finish making an item
     * @param ID ID of the item that was made, needed to get item in Gallery
     * @param v view of the activity
     */
    private void nextActivity(long ID, View v, String serial, String date, String make, String model, String price, String desc, String comment) {
        //Intent i = new Intent(v.getContext(), ListActivity.class);
        Intent i = new Intent(v.getContext(), GalleryActivity.class);
        // put ID in the intent
        i.putExtra("ID",ID);
        i.putExtra("serial", serial);
        i.putExtra("date",date);
        i.putExtra("make",make);
        i.putExtra("model",model);
        i.putExtra("price",price);
        i.putExtra("desc",desc);
        i.putExtra("comment",comment);
        startActivity(i);
    }

    /**
     * Send user to the ListActivity once they finish making an item
     */
    private void backActivity() {
        finish();
    }
    //TODO: scan function -> scan serial number

    View.OnClickListener barcodeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("BARCODE","CLICKED");
            Intent i = new Intent(AddActivity.this, BarcodeActivity.class);
            barcodeGrab.launch(i);
        }
    };

    View.OnClickListener capListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            capturePhoto();
        }
    };

    View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            animateCamera(false);
        }
    };

    View.OnClickListener scanserial = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String permission = Manifest.permission.CAMERA;
            int CAMERA_PERMISSION_CODE = 1111;
            if (EasyPermissions.hasPermissions(AddActivity.this,permission)) {
                animateCamera(true);
                // Activate the camera
                // Camera set up

                ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(AddActivity.this);

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
                }, ContextCompat.getMainExecutor(AddActivity.this));

            }  else {
                EasyPermissions.requestPermissions(AddActivity.this, "Our App Requires permission to access your camera", CAMERA_PERMISSION_CODE, permission);
            }
        }
    };

    /**
     * fades the one view in and the other view out
     */
    private void animateCamera(boolean opening) {
        int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        View open;
        View close;
        if (opening){
            open = capture_layout;
            close = add_layout;
        } else {
            open = add_layout;
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

    /**
     * Turns on the camera, sets options
     * @param cameraProvider a provider used to bind the lifecycle of cameras
     */
    private void startCamera(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        PreviewView cam_preview ;
        cam_preview=findViewById(R.id.camPreview);

        preview.setSurfaceProvider(cam_preview.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();
        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(AddActivity.this,cameraSelector,preview,imageCapture);
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

                // This where an image cropper would be, if I had one

                recognizeText(rotatedBitmap);
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
     * sets the text in the serial field
     * @param resultText
     */
    private void onTextRecognized(String resultText) {
        // Your implementation for handling recognized text
        // This method should contain the logic you want to execute when text is recognized
        serialField.setText(resultText);
    }

    /**
     * recognize the text from the image
     * @param bitmap
     */
    private void recognizeText(Bitmap bitmap) {
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (bitmap != null) {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            textRecognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        String resultText = visionText.getText();
                        resultText = validation(resultText);
                        onTextRecognized(resultText);
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof MlKitException) {
                            // Handle ML Kit errors
                            MlKitException mlKitException = (MlKitException) e;
                            Toast.makeText(AddActivity.this, "ML Kit Error: " + mlKitException.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /**
     * validates the serial number
     * @param text
     * @return
     */
    public String validation(String text) {
        ArrayList<String> serial = new ArrayList<String>();
        serial.add("123456");
        serial.add("654321");
        serial.add("230802");
        serial.add("150903");
        serial.add("241100");
        serial.add("290102");
        serial.add("140403");
        serial.add("3000045581");

        for (int i = 0 ; i< serial.size() ;i++) {
            if (text.contains(serial.get(i))) {
                return serial.get(i);
            }
        }


        return text;
    }

    private void extractSerialNumber(String resultText) {
        // Pattern to match a sequence of digits or an uppercase letter followed by digits until a space is encountered
        String pattern = "(?:(?=[A-Z])[A-Z0-9]*\\d[A-Z0-9]*|\\d{6,})";
        Pattern serialNumberPattern = Pattern.compile(pattern);
        Matcher matcher = serialNumberPattern.matcher(resultText);

        if (matcher.find()) {
            String extractedSerialNumber = matcher.group();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("serialNumber", extractedSerialNumber);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(AddActivity.this, "Serial Number: " + extractedSerialNumber, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(AddActivity.this, "No valid serial number found in the text.", Toast.LENGTH_LONG).show();
        }
    }
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