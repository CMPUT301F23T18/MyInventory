package com.example.myinventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class CapturePopUp extends DialogFragment {
    Button capture_button;
    Button gallery_button;
    View view;
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
    }

    /**
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.view = getLayoutInflater().inflate(R.layout.capture_popup,null);

        //Initialize buttons
        capture_button = view.findViewById(R.id.choiceCapture);
        gallery_button = view.findViewById(R.id.choiceGallery);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .setNeutralButton("cancel",null)
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
