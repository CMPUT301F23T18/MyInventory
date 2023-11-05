package com.example.myinventoryapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class CapturePopUp {
    Button capture_button;
    Button gallery_button;
    PopupWindow popupWindow;
    View view;

    /**
     * generates a popup for user to select if they want to take a photo or add a photo
     * from their phone gallery
     * @param view view from calling activity
     */
    public void showWindow(final View view) {
        this.view = view;
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.capture_popup, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize buttons
        capture_button = popupView.findViewById(R.id.choiceCapture);
        capture_button.setOnClickListener(captureListener);
        gallery_button = popupView.findViewById(R.id.choiceGallery);
        gallery_button.setOnClickListener(galleryListener);

        popupView.setOnTouchListener(touchListener);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Close the window when clicked
            popupWindow.dismiss();
            return true;
        }
    };

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // open the camera
            GalleryActivity.handleCamera(view);
            popupWindow.dismiss();
        }
    };

    View.OnClickListener galleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // open the gallery
            GalleryActivity.handleGallery(view);
            popupWindow.dismiss();
        }
    };
}
