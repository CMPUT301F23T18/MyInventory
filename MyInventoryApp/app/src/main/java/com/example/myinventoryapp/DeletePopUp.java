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
/**
 * This is a DialogFragment that shows an alert dialog popup asking the user to confirm or cancel
 * when attempting to delete item(s) by clicking the yes or no button respectively.
 */
public class DeletePopUp extends DialogFragment {
    TextView confirm_text;
    Button yes_btn, no_btn;
    OnFragmentInteractionListener listener;

    /**
     * Interface DeletePopUp dialog fragment for the method to be implemented in
     * DeleteActivity class.
     */
    public interface OnFragmentInteractionListener {
        /**
         * Called when YES is pressed in the confirmation dialog fragment.
         */
        void onYESPressed();
    }

    /**
     * Called to create and return the view of the dialog fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
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

    /**
     * Fragment is attached to context (activity that is hosting the fragment).
     * @param context is the context the fragment attaches itself to
     */
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
