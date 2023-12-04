package com.example.myinventoryapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.example.myinventoryapp.ListActivities.ListActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class FilterDialogFragment extends DialogFragment {

    private ChipGroup makeChipGroup, tagChipGroup;
    private Set<String> originalMakes, originalTags;
    private Set<String> appliedMakes, appliedTags;
    private List<Integer> fromDate = new ArrayList<>(), toDate = new ArrayList<>();
    private String selectedDateRange;

    private TextView dateTextView, cleardate;

    public interface FilterListener {
        void onFilterApplied(Map<String, Set<String>> selectedFilters);

        void applyFilter(Map<String, Set<String>> selectedFilters);
    }

    private OnFragmentInteractionListener fragmentInteractionListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Map<String, Set<String>> selectedFilters);
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        this.fragmentInteractionListener = listener;
    }


    private final FilterListener filterListener;

    public FilterDialogFragment(Set<String> originalMakes, Set<String> originalTags, Set<String> appliedMakes, Set<String> appliedTags, FilterListener filterListener) {
        this.originalMakes = originalMakes;
        this.filterListener = filterListener;
        this.originalTags = originalTags;
        this.appliedMakes = appliedMakes;
        this.appliedTags = appliedTags;

        // Retain the instance of the Fragment across configuration changes
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedDateRange = getArguments().getString("dateString");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_filter_dialog, null);

        makeChipGroup = view.findViewById(R.id.makeChipGroup);
        tagChipGroup = view.findViewById(R.id.tagChipGroup);
        dateTextView = view.findViewById(R.id.dateDropDown);
        cleardate = view.findViewById(R.id.cleardatebtn);

        // Populate chip groups with makes and tags
        populateChipGroups(originalMakes, originalTags, appliedMakes, appliedTags);

        // Set the selected date if available
        if (selectedDateRange != "") {
            dateTextView.setText(selectedDateRange);
        }

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to show the date dialog
                showDatesDialog();
            }
        });

        cleardate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the selected date and related values
                selectedDateRange = "";
                dateTextView.setText("");
                dateTextView.setHint("Date");
                fromDate.clear();
                toDate.clear();
            }
        });

        //Change title bar background to match fragment
        TextView titleTextView = new TextView(getActivity());
        titleTextView.setText("Filter Items");
        titleTextView.setTextSize(25);

        builder.setView(view)
                .setCustomTitle(titleTextView)
                .setPositiveButton("Filter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the selected filters and apply them
                        Map<String, Set<String>> selectedFilters = new HashMap<>();
                        selectedFilters.put("makes", getSelectedChips(makeChipGroup));
                        selectedFilters.put("tags", getSelectedChips(tagChipGroup));
                        selectedFilters.put("dates", getSelectedDates());

                        // Call the listener to notify the ListActivity about the filter
                        if (fragmentInteractionListener != null) {
                            fragmentInteractionListener.onFragmentInteraction(selectedFilters);
                        }

                        // Call the listener to notify the ListActivity about the filter
                        filterListener.onFilterApplied(selectedFilters);

                        // Call a method in your ListActivity to apply the filter
                        filterListener.applyFilter(selectedFilters);

                        // Disable and change color for selected chips
                        updateChipsState(selectedFilters.get("makes"), makeChipGroup);
                        updateChipsState(selectedFilters.get("tags"), tagChipGroup);
                        // Update for other criteria if needed
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setNeutralButton("Clear Filters", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear the filters
                        clearFilters();
                    }
                });

        return builder.create();
    }

    private void populateChipGroups(Set<String> makes, Set<String> tags, Set<String> appliedMakes, Set<String> appliedTags) {
        // Populate make chip group
        addChipsFromSet(makeChipGroup, makes, appliedMakes);

        // Populate tag chip group
        addChipsFromSet(tagChipGroup, tags, appliedTags);
    }

    private void addChipsFromSet(ChipGroup chipGroup, Set<String> options, Set<String> appliedOptions) {
        for (String option : options) {
            addChip(chipGroup, option, appliedOptions.contains(option));
        }
    }

    private void addChip(ChipGroup chipGroup, String label, boolean applied) {
        Chip chip = new Chip(chipGroup.getContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(!applied); // Set clickable property based on whether it's already applied
        chip.setChecked(applied);
        chipGroup.addView(chip);
    }

    private Set<String> getSelectedChips(ChipGroup chipGroup) {
        Set<String> selectedChips = new HashSet<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedChips.add(chip.getText().toString());
            }
        }
        return selectedChips;
    }

    private void clearChipGroup(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setChecked(false);
            enableChip(chip); // Re-enable all chips when clearing filters
        }

        appliedMakes.clear(); // Clear applied makes
        appliedTags.clear(); // Clear applied tags
    }

    private void disableChip(Chip chip) {
        // Ensure the chip is visually marked as checked
        chip.setChecked(true);
        chip.setEnabled(false);
        chip.setChipBackgroundColorResource(R.color.disabled_chip_color); // Change chip color when disabled
    }

    private void enableChip(Chip chip) {
        chip.setChecked(false);  // Ensure the chip is visually marked as unchecked
        chip.setEnabled(true);
        chip.setChipBackgroundColorResource(android.R.color.transparent); // Reset chip color when enabled
    }

    private void updateChipsState(Set<String> selectedChips, ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            String chipText = chip.getText().toString();

            if (selectedChips.contains(chipText)) {
                disableChip(chip); // Disable and change color for selected chips
                if (chipGroup.getId() == R.id.makeChipGroup) {
                    appliedMakes.add(chipText); // Keep track of applied makes
                } else if (chipGroup.getId() == R.id.tagChipGroup) {
                    appliedTags.add(chipText); // Keep track of applied tags
                }
            } else {
                enableChip(chip); // Re-enable other chips
                if (chipGroup.getId() == R.id.makeChipGroup) {
                    appliedMakes.remove(chipText); // Remove from applied makes
                } else if (chipGroup.getId() == R.id.tagChipGroup) {
                    appliedTags.remove(chipText); // Remove from applied tags
                }
            }
        }
    }

    private Set<String> getSelectedDates() {
        return selectedDateRange != null && !selectedDateRange.isEmpty() ? Collections.singleton(selectedDateRange) : Collections.emptySet();
    }

    private void clearFilters() {
        // Uncheck all chips in both chip groups
        clearChipGroup(makeChipGroup);
        clearChipGroup(tagChipGroup);

        // Re-enable all chips when clearing filters
        for (int i = 0; i < makeChipGroup.getChildCount(); i++) {
            enableChip((Chip) makeChipGroup.getChildAt(i));
        }
        for (int i = 0; i < tagChipGroup.getChildCount(); i++) {
            enableChip((Chip) tagChipGroup.getChildAt(i));
        }

        // Call a method in your ListActivity to clear the filters
        ((ListActivity) getActivity()).clearFilters();

        // Clear applied makes and tags
        appliedMakes.clear();
        appliedTags.clear();

        selectedDateRange = "";
        dateTextView.setText("");
        dateTextView.setHint("Date");
        fromDate.clear();
        toDate.clear();
    }

    private void showDatesDialog() {
        // Creating a MaterialDatePicker builder for selecting a date range
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a date range");

        // Building the date picker dialog
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                // Retrieving the selected start and end dates
                Long startDate = selection.first;
                Long endDate = selection.second;

                // Formatting the selected dates as strings
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Your_Timezone_ID"));
                String startDateString = sdf.format(new Date(startDate));
                String endDateString = sdf.format(new Date(endDate));

                // Creating the date range string
                selectedDateRange = startDateString + " TO " + endDateString;

                // Displaying the selected date range in the TextView
                dateTextView.setText(selectedDateRange);
                fromDate = parseDate(startDateString);
                toDate = parseDate(endDateString);
            }
        });

        // Showing the date picker dialog
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private List<Integer> parseDate(String date) {
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

}


