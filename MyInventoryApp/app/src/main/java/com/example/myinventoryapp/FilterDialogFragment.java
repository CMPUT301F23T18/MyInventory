package com.example.myinventoryapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FilterDialogFragment extends DialogFragment {
    private TextView makeTextView, tagTextView, dateTextView;
    private Button applyBtn, cancelBtn;
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
        dateTextView = view.findViewById(R.id.dateDropDown);
        applyBtn = view.findViewById(R.id.applyFilterButton);
        cancelBtn = view.findViewById(R.id.cancelFilterButton);

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

        //TODO: finish method for filtering by date range
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker.Builder<Pair<Long, Long>> date_builder = MaterialDatePicker.Builder.dateRangePicker();
                date_builder.setTitleText("Select a date range");

                MaterialDatePicker<Pair<Long, Long>> rangePicker = date_builder.build();
                rangePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                        String from_date = new Date();
                        format.format(from_date);

//                        String from_date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date(selection.first));
//                        String to_date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date(selection.second));
//                        String day1 = Integer.toString(Integer.parseInt(from_date.substring(3,5))+1);
//                        String day2 = Integer.toString(Integer.parseInt(to_date.substring(3,5))+1);
                        from_date = from_date.substring(0,3)+day1+from_date.substring(5,10);
                        to_date = to_date.substring(0,3)+day2+to_date.substring(5,10);
                        String date_range = from_date+" to "+to_date;
                        dateTextView.setText(date_range);
                    }
                });
                rangePicker.show(getActivity().getSupportFragmentManager(), "date_range");
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: finish method for applying filters
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
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
                makeTextView.setText(stringBuilder.toString());
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
                makeTextView.setText("");
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



