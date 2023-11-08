package com.example.myinventoryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {

    Button capture_btn;
    View view;

    ProcessCameraProvider cameraProvider;
    PreviewView pvView;

    /**
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.view = getLayoutInflater().inflate(R.layout.fragment_camera, null);
        capture_btn = view.findViewById(R.id.captureButtonCam);
        capture_btn.setOnClickListener(captureListener);
        pvView = view.findViewById(R.id.camPreview);

        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(view.getContext());
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
        }, ContextCompat.getMainExecutor(view.getContext()));
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(pvView.getSurfaceProvider());

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(this,cameraSelector,preview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}
