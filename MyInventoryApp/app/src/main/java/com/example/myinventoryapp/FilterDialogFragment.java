package com.example.myinventoryapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class FilterDialogFragment extends DialogFragment {
    private TextView makeTextView, tagTextView, dateTextView, cleardate;
    private Button applyBtn, cancelBtn, clearfiltersBtn;
    private boolean[] selectedMakes;
    private boolean[] selectedTags;
    private ArrayList<Integer> makeListIndex = new ArrayList<>();
    private List<String> filterMakes = new ArrayList<>(), filterTags = new ArrayList<>();
    private List<String> makesList = new ArrayList<>();
    private ArrayList<Integer> tagListIndex = new ArrayList<>();
    private List<String> tagsList = new ArrayList<>();
    private List<Integer> fromDate = new ArrayList<>(), toDate = new ArrayList<>();
    OnFragmentInteractionListener listener;

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
        cleardate = view.findViewById(R.id.cleardatebtn);
        clearfiltersBtn = view.findViewById(R.id.clearFilterButton);

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

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatesDialog();
            }
        });

        cleardate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTextView.setText("");
                dateTextView.setHint("Date");
                fromDate.clear();
                toDate.clear();
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: finish method for applying filters
                listener.onApplyPressed(fromDate, toDate, filterMakes, filterTags);
                getDialog().dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        clearfiltersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTextView.setText("");
                dateTextView.setHint("Date");
                fromDate.clear();
                toDate.clear();
                makeListIndex.clear();
                makeTextView.setText("");
                makeTextView.setHint("Makes");
                filterMakes.clear();
                tagListIndex.clear();
                tagTextView.setText("");
                tagTextView.setHint("Tags");
                filterTags.clear();
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
                    makeListIndex.add(i);
                    Collections.sort(makeListIndex);
                } else {
                    makeListIndex.remove(Integer.valueOf(i));
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filterMakes = new ArrayList<>();
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < makeListIndex.size(); j++) {
                    stringBuilder.append(makesList.get(makeListIndex.get(j)));
                    filterMakes.add(makesList.get(makeListIndex.get(j)));
                    if (j != makeListIndex.size() - 1) {
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
                makeListIndex.clear();
                makeTextView.setText("");
                makeTextView.setHint("Makes");
                filterMakes.clear();
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
                    tagListIndex.add(i);
                    Collections.sort(tagListIndex);
                } else {
                    tagListIndex.remove(Integer.valueOf(i));
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filterTags = new ArrayList<>();
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < tagListIndex.size(); j++) {
                    stringBuilder.append(tagsList.get(tagListIndex.get(j)));
                    filterTags.add(tagsList.get(tagListIndex.get(j)));
                    if (j != tagListIndex.size() - 1) {
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
                tagListIndex.clear();
                tagTextView.setText("");
                tagTextView.setHint("Tags");
                filterTags.clear();
            }
        });

        builder.show();
    }

    private void showDatesDialog(){
        // Creating a MaterialDatePicker builder for selecting a date range
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a date range");

        // Building the date picker dialog
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Retrieving the selected start and end dates
            Long startDate = selection.first;
            Long endDate = selection.second;

            // Formating the selected dates as strings
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Your_Timezone_ID"));
            String startDateString = sdf.format(new Date(startDate));
            String endDateString = sdf.format(new Date(endDate));

            // Creating the date range string
            String selectedDateRange = startDateString + " TO " + endDateString;

            // Displaying the selected date range in the TextView
            dateTextView.setText(selectedDateRange);
            fromDate = parseDate(startDateString);
            toDate = parseDate(endDateString);

        });

        // Showing the date picker dialog
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private List<Integer> parseDate(String date){
        String[] dateParts = date.split("/");
        int day = Integer.parseInt(dateParts[2]);
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[0]);

        List<Integer> parts = new ArrayList<>();
        parts.add(year);
        parts.add(month);
        parts.add(day);

        return parts;
    }

    /**
     * Interface FilterDialogFragment dialog fragment for the method to be implemented in
     * ListActivity class.
     */
    public interface OnFragmentInteractionListener {
        /**
         * Called when Apply is pressed in the filter dialog fragment.
         */
        void onApplyPressed(List<Integer> fromDate, List<Integer> toDate, List<String> filterMakes, List<String> filterTags);
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



