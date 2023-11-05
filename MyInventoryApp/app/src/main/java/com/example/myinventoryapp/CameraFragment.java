package com.example.myinventoryapp;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraFragment extends Fragment {

    Button capture_btn;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;
    View view;

    /**
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.view = getLayoutInflater().inflate(R.layout.fragment_camera,null);
        capture_btn = view.findViewById(R.id.captureButtonCam);
        capture_btn.setOnClickListener(captureListener);
        previewView = view.findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(view.getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        }, getExecutor());
        return null;
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(view.getContext());
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,preview,imageCapture);
    }

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            capturePhoto();
        }
    };

    private void capturePhoto() {
        String sdcard = Environment.getExternalStorageDirectory().getPath();
        File photoDir = new File(sdcard + "/Pictures/CameraXPhotos"); //Might need "pictures/CameraXPhotos" instead

        if (!photoDir.exists()) {
            photoDir.mkdir();
        }
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";

        File photoFile = new File(photoFilePath);
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CameraFragment.this.getContext(),"Photo has been saved",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraFragment.this.getContext(),"Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
