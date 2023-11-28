package com.example.myinventoryapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class FilterDialogFragment extends DialogFragment {
    private TextView makeTextView;
    private TextView tagTextView;
    private boolean[] selectedMakes;
    private boolean[] selectedTags;
    private ArrayList<Integer> makeList = new ArrayList<>();
    private List<String> makesList = new ArrayList<>();
    private ArrayList<Integer> tagList = new ArrayList<>();
    private List<String> tagsList = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        makesList = getArguments().getStringArrayList("makesList");
        tagsList = getArguments().getStringArrayList("tagsList");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_filter_dialog, null);

        makeTextView = view.findViewById(R.id.makeDropDown);
        tagTextView = view.findViewById(R.id.tagDropDown);

        selectedMakes = new boolean[makesList.size()];
        selectedTags = new boolean[tagsList.size()];

        makeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMakesDialog();
            }
        });

        tagTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTagsDialog();
            }
        });

        builder.setView(view);

        return builder.create();
    }

    public static FilterDialogFragment newInstance(ArrayList<String> makesList, ArrayList<String> tagsList) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("makesList", makesList);
        args.putStringArrayList("tagsList", tagsList);
        fragment.setArguments(args);
        return fragment;
    }

    private void showMakesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Makes");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(makesList.toArray(new CharSequence[0]), selectedMakes, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b) {
                    makeList.add(i);
                    Collections.sort(makeList);
                } else {
                    makeList.remove(Integer.valueOf(i));
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < makeList.size(); j++) {
                    stringBuilder.append(makesList.get(makeList.get(j)));
                    if (j != makeList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                tagTextView.setText(stringBuilder.toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < selectedMakes.length; j++) {
                    selectedMakes[j] = false;
                }
                makeList.clear();
                tagTextView.setText("");
            }
        });

        builder.show();
    }

    private void showTagsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Tags");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(tagsList.toArray(new CharSequence[0]), selectedTags, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b) {
                    tagList.add(i);
                    Collections.sort(tagList);
                } else {
                    tagList.remove(Integer.valueOf(i));
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < tagList.size(); j++) {
                    stringBuilder.append(tagList.get(tagList.get(j)));
                    if (j != tagList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                tagTextView.setText(stringBuilder.toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < selectedTags.length; j++) {
                    selectedTags[j] = false;
                }
                tagList.clear();
                tagTextView.setText("");
            }
        });

        builder.show();
    }
}



