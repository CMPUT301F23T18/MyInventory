package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This is a class that represents an activity that displays the list of items.
 * The user can add, select, view, and delete items in the list
 */
public class ListActivity extends AppCompatActivity {
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayList<Item> items;
    List<Integer> delete_items;
    double totalValue = 0;
    TextView totalCostView;
    Button filterbutton, sortbutton, deleteButton, tagButton;
    // Original list to store all items
    private ArrayList<Item> originalItems;
    private Set<String> appliedMakes = new HashSet<>();
    private Set<String> appliedTags = new HashSet<>();

    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.item_list);
            totalCostView = findViewById(R.id.totalCostView);
            itemList = findViewById(R.id.item_list);

            // Reset user id in case it's necessary.
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            ((Global) getApplication()).setUSER_PATH(mAuth.getCurrentUser().getUid());

            items = new ArrayList<>();
            originalItems = new ArrayList<>();
            itemAdapter = new ItemList(this, items);

            CollectionReference fb_items = ((Global) getApplication()).getFbItemsRef();
            fb_items.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                        return;
                    }
                    if (querySnapshots != null) {
                        // Clear both original and filtered lists
                        originalItems.clear();
                        items.clear();

                        // Populate the original list
                        for (QueryDocumentSnapshot doc : querySnapshots) {
                            Item item = new Item();
                            String id = doc.getId();
                            item.setSerial_num(doc.getString("serial"));
                            item.setDate(doc.getString("date"));
                            item.setMake(doc.getString("make"));
                            item.setModel(doc.getString("model"));
                            item.setEst_value(doc.getString("price"));
                            item.setDescription(doc.getString("desc"));
                            item.setID(Long.parseLong(id));

                            if (doc.contains("tags")) {
                                List<String> tags = (List<String>) doc.get("tags");
                                item.setTags(tags);
                            }

                            // set photos
                            StorageReference photosRef = ((Global) getApplication()).getPhotoStorageRef();
                            item.generatePhotoArray(photosRef, id, itemAdapter);

                            Log.d("Firestore", String.format("Item(%s, %s) fetched", item.getMake(), item.getModel()));
                            originalItems.add(item); // Add to the original list
                            items.add(item); // Add to the filtered list initially
                        }
                        // Calculate total value after resetting total.
                        totalValue = 0;
                        for (int i = 0; i < items.size(); i++) {
                            String est_value = items.get(i).getEst_value();
                            if (est_value != null) {
                                totalValue += Double.parseDouble(est_value);
                            }

                        }
                        // Set the adapter to the filtered list
                        itemList.setAdapter(itemAdapter);

                        totalCostView.setText(String.format(Locale.CANADA, "Total Value = $%.2f", totalValue));
                        itemAdapter.notifyDataSetChanged();
                    }
                }
            });
            itemList.setOnItemClickListener(itemClicker);
            itemList.setAdapter(itemAdapter);

            addButton = findViewById(R.id.add_button);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), AddActivity.class);
                    startActivity(i);
                }
            });

            deleteButton = findViewById(R.id.delete_btn);
            tagButton = findViewById(R.id.tag_btn);
            filterbutton = findViewById(R.id.filterButton);
            sortbutton = findViewById(R.id.sortButton);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ListActivity.this, DeleteActivity.class);
                    i.putParcelableArrayListExtra("list", items);
                    startActivity(i);
                }
            });
            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ListActivity.this, SelectTagItemsActivity.class);
                    i.putParcelableArrayListExtra("list", items);
                    startActivity(i);
                }
            });

            filterbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchDistinctMakesAndTags();
                }
            });

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save necessary data (e.g., applied filters) to the outState bundle
        outState.putStringArrayList("appliedMakes", new ArrayList<>(appliedMakes));
        outState.putStringArrayList("appliedTags", new ArrayList<>(appliedTags));
        // Save other relevant data...
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore necessary data from the savedInstanceState bundle
        appliedMakes = new HashSet<>(Objects.requireNonNull(savedInstanceState.getStringArrayList("appliedMakes")));
        appliedTags = new HashSet<>(Objects.requireNonNull(savedInstanceState.getStringArrayList("appliedTags")));
        // Restore other relevant data...
    }


    private void showFilterDialog(Set<String> originalMakes, Set<String> originalTags) {
        // Create and show the filter dialog fragment
        FilterDialogFragment filterDialog = new FilterDialogFragment(originalMakes, originalTags, appliedMakes, appliedTags, new FilterDialogFragment.FilterListener() {
            @Override
            public void onFilterApplied(Map<String, Set<String>> selectedFilters) {
                // Call a method in your ListActivity to apply the filter
                applyFilter(selectedFilters);
            }
        });
        filterDialog.show(getSupportFragmentManager(), "FilterDialogFragment");
    }

    AdapterView.OnItemClickListener itemClicker = new AdapterView.OnItemClickListener() {
        /**
         * Handles item clicks in the list. The intent is to open the ViewItemActivity which allows
         * the user to view a single item.
         * @param parent The AdapterView where the click happened.
         * @param view The view within the AdapterView that was clicked (this
         *            will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent(view.getContext(), ViewItemActivity.class);
            long ID = items.get(position).getID();
            i.putExtra("ID",ID);
            i.putExtra("NumofImages",items.get(position).getPhotosSize());
            startActivity(i);
        }
    };

    private void fetchDistinctMakesAndTags() {
        CollectionReference fbItems = ((Global) getApplication()).getFbItemsRef();

        fbItems.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Set<String> makes = new HashSet<>();
                    Set<String> tags = new HashSet<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String make = document.getString("make");
                        if (make != null) {
                            makes.add(make);
                        }

                        List<String> documentTags = (List<String>) document.get("tags");
                        if (documentTags != null) {
                            tags.addAll(documentTags);
                        }
                    }

                    // Now you have the distinct makes and tags
                    Set<String> originalMakes = new HashSet<>(makes);
                    Set<String> originalTags = new HashSet<>(tags);

                    showFilterDialog(originalMakes, originalTags);
                } else {
                    Log.e("Firestore", "Error getting documents: ", task.getException());
                    // Handle error
                }
            }
        });
    }


    public void applyFilter(Map<String, Set<String>> selectedFilters) {
        // Update the applied filters
        appliedMakes.clear();
        appliedTags.clear();

        // Handle each criterion
        for (Map.Entry<String, Set<String>> entry : selectedFilters.entrySet()) {
            String criterion = entry.getKey();
            Set<String> values = entry.getValue();

            switch (criterion) {
                case "makes":
                    appliedMakes.addAll(values);
                    break;
                case "tags":
                    appliedTags.addAll(values);
                    break;
                // Add more cases for other criteria as needed
            }
        }

        // Filter the items based on the selected filters from the original list
        List<Item> filteredItems = filterItems(originalItems, selectedFilters);

        // Update the adapter with the filtered items
        itemAdapter.clear();
        itemAdapter.addAll(filteredItems);
        itemAdapter.notifyDataSetChanged();

        // Recalculate and update total value
        updateTotalValue(filteredItems);
    }

    private List<Item> filterItems(List<Item> originalItems, Map<String, Set<String>> selectedFilters) {
        List<Item> filteredItems = new ArrayList<>();

        for (Item item : originalItems) {
            Set<String> itemMakes = new HashSet<>(Collections.singletonList(item.getMake()));
            Set<String> itemTags = item.getTags() != null ? new HashSet<>(item.getTags()) : Collections.emptySet();
            // Get other criteria as needed

            // Check if the item matches the selected filters for all criteria
            boolean makesMatch = selectedFilters.get("makes").isEmpty() || !Collections.disjoint(itemMakes, selectedFilters.get("makes"));
            boolean tagsMatch = selectedFilters.get("tags").isEmpty() || !Collections.disjoint(itemTags, selectedFilters.get("tags"));
            // Check other criteria as needed

            if (makesMatch && tagsMatch /* && otherCriteriaMatch */) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    public void clearFilters() {
        // Clear the appliedMakes and appliedTags sets
        appliedMakes.clear();
        appliedTags.clear();

        // Reset the adapter with the original list
        itemAdapter.clear();
        itemAdapter.addAll(originalItems);
        itemAdapter.notifyDataSetChanged();

        // Recalculate and update total value for the original list
        updateTotalValue(originalItems);
    }

    private void updateTotalValue(List<Item> itemList) {
        double total = 0;

        for (Item item : itemList) {
            String estValue = item.getEst_value();
            if (estValue != null) {
                total += Double.parseDouble(estValue);
            }
        }

        totalCostView.setText(String.format(Locale.CANADA, "Total Value = $%.2f", total));
    }

}