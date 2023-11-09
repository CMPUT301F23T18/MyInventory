package com.example.myinventoryapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import javax.annotation.Nullable;

public class DeletePopUp extends DialogFragment {
    TextView confirm_text;
    Button yes_btn, no_btn;
    OnFragmentInteractionListener listener;

    public interface OnFragmentInteractionListener {
        void onYESPressed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_fragment, container, false);
        confirm_text = view.findViewById(R.id.confirm_textview);
        yes_btn = view.findViewById(R.id.yes_confirm);
        no_btn = view.findViewById(R.id.no_confirm);

        String strtext = getArguments().getString("confirm_text");
        confirm_text.setText(strtext);

        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onYESPressed();
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }
}
