package com.example.myinventoryapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class CapturePopUp extends DialogFragment {
    View view;
    View root;
    private OnFragmentInteractionListener listener;


    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        }
    }

    public interface OnFragmentInteractionListener {
        void onCapturePressed();
        void onGalleryPressed();

        void onDeletePressed();
    }

    /**
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.view = getLayoutInflater().inflate(R.layout.capture_popup,null);
        boolean on_image = getArguments().getBoolean("onImage"); //checks if click was from image

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // If called by clicking on an image provide a delete button, else provide cancel button
        if (!on_image) {
            return builder
                    .setView(view)
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton("Capture", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            listener.onCapturePressed();
                        }
                    })
                    .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // delete button gets clicked
                            listener.onGalleryPressed();
                        }
                    })
                    .create();
        } else {
            return builder
                    .setView(view)
                    .setCancelable(true)
                    .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            listener.onDeletePressed();
                        }
                    })
                    .setPositiveButton("Capture", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            listener.onCapturePressed();
                        }
                    })
                    .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // delete button gets clicked
                            listener.onGalleryPressed();
                        }
                    })
                    .create();
        }
    }

}
