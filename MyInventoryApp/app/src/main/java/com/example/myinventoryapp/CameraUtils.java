package com.example.myinventoryapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.Manifest;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import pub.devrel.easypermissions.EasyPermissions;

public class CameraUtils {
    //    static String recognizedText;
    interface OnImageCapturedListener {
        void onImageCaptured(Bitmap bitmap);
        void onTextRecognized(String text);
    }
    private static final int CAMERA_PERMISSION_CODE = 123;
    private static final int CAMERA_REQUEST_CODE = 124;

    public static void openCameraDirectly(Activity activity, OnImageCapturedListener onImageCapturedListener) {
        // Check camera permission
        String permission = Manifest.permission.CAMERA;

        if (EasyPermissions.hasPermissions(activity, permission)) {
            // Camera permission granted, continue with camera initialization and launch
            initCamera(activity);
        } else {
            // Request camera permission
            EasyPermissions.requestPermissions(activity, "Our App Requires permission to access your camera", CAMERA_PERMISSION_CODE, permission);
        }
    }

    public static void initCamera(Activity activity) {
        // Initialize and launch the camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data, OnImageCapturedListener listener) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Get the captured image as a Bitmap
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");

            // Perform text recognition
            recognizeText(bitmap, listener);
        }
    }

    private static void recognizeText(Bitmap bitmap, OnImageCapturedListener listener) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        textRecognizer.processImage(image)
                .addOnSuccessListener(firebaseVisionText -> {
                    // Process the recognized text
                    String recognizedText = firebaseVisionText.getText();
                    listener.onTextRecognized(recognizedText);
                    listener.onImageCaptured(bitmap);
                })
                .addOnFailureListener(e -> {
                    // Handle text recognition failure
                    Log.e("TextRecognition", "Error: " + e.getMessage());
                    listener.onImageCaptured(bitmap);
                });
    }
}



