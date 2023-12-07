package com.example.myinventoryapp.ListActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myinventoryapp.Adaptors.ItemList;
import com.example.myinventoryapp.DatabaseHandler;
import com.example.myinventoryapp.FilterDialogFragment;
import com.example.myinventoryapp.ItemManagement.AddActivity;
import com.example.myinventoryapp.ItemManagement.Item;
import com.example.myinventoryapp.ItemManagement.ViewItemActivity;
import com.example.myinventoryapp.ProfileActivity;
import com.example.myinventoryapp.R;
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
import java.util.Comparator;
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
public class ListActivity extends AppCompatActivity implements FilterDialogFragment.FilterListener {
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayAdapter<String> orderadapter, fieldadapter;
    ArrayList<Item> items, filteredDesc, filtered_items;
    List<Integer> delete_items;
    double totalValue = 0;
    TextView totalCostView, banner;
    Button filterbutton, sortbutton, deleteButton, tagButton;
    String fieldData, orderData;
    boolean filtered;
    // Original list to store all items
    private ArrayList<Item> originalItems;
    private Set<String> appliedMakes = new HashSet<>();
    private Set<String> appliedTags = new HashSet<>();
    private Set<String> appliedDate = new HashSet<>();
    private String previousDate, fieldSelected = "", orderSelected = "";

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);
        totalCostView = findViewById(R.id.totalCostView);
        itemList = findViewById(R.id.item_list);

        banner = findViewById(R.id.nothingtoshowbanner);

        // Reset user id in case it's necessary.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        ((DatabaseHandler) getApplication()).setUSER_PATH(mAuth.getCurrentUser().getUid());

        items = new ArrayList<>();
        originalItems = new ArrayList<>();
        itemAdapter = new ItemList(this, items);

        CollectionReference fb_items = ((DatabaseHandler) getApplication()).getFbItemsRef();
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
                        StorageReference photosRef = ((DatabaseHandler)getApplication()).getPhotoStorageRef();
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
                    if(items.size() == 0) {
                        banner.setVisibility(View.VISIBLE);
                    }
                    else {
                        banner.setVisibility(View.INVISIBLE);
                    }
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
        ImageView profileButton = findViewById(R.id.profileMain);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ListActivity.this , ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        filterbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDistinctMakesAndTags();
            }
        });
        sortbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ListActivity.this, DeleteActivity.class);
                i.putParcelableArrayListExtra("list",items);
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
        filteredDesc = new ArrayList<>();

        // Set up the SearchView
        SearchView searchBar = findViewById(R.id.searchBar);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Call the filterList method to update the list based on the search query
                filterList(newText);
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save necessary data (e.g., applied filters) to the outState bundle
        outState.putStringArrayList("appliedMakes", new ArrayList<>(appliedMakes));
        outState.putStringArrayList("appliedTags", new ArrayList<>(appliedTags));

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore necessary data from the savedInstanceState bundle
        appliedMakes = new HashSet<>(Objects.requireNonNull(savedInstanceState.getStringArrayList("appliedMakes")));
        appliedTags = new HashSet<>(Objects.requireNonNull(savedInstanceState.getStringArrayList("appliedTags")));

    }



    private void fetchDistinctMakesAndTags() {
        CollectionReference fbItems = ((DatabaseHandler) getApplication()).getFbItemsRef();

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

                    // Create a bundle and put the required arguments
                    Bundle args = new Bundle();
                    args.putStringArrayList("originalMakes", new ArrayList<>(originalMakes));
                    args.putStringArrayList("originalTags", new ArrayList<>(originalTags));
                    args.putStringArrayList("appliedMakes", new ArrayList<>(appliedMakes));
                    args.putStringArrayList("appliedTags", new ArrayList<>(appliedTags));
                    args.putString("previousDateRange", previousDate);

                    // Create a new instance of FilterDialogFragment and set the arguments
                    FilterDialogFragment filterDialog = new FilterDialogFragment();
                    filterDialog.setArguments(args);

                    // Set the listener
                    filterDialog.setFilterListener(new FilterDialogFragment.FilterListener() {
                        @Override
                        public void onFilterApplied(Map<String, Set<String>> selectedFilters) {
                            // Call a method in your ListActivity to apply the filter
                            applyFilter(selectedFilters);
                        }
                    });

                    // Show the FilterDialogFragment
                    filterDialog.show(getSupportFragmentManager(), "FilterDialogFragment");
                } else {
                    Log.e("Firestore", "Error getting documents: ", task.getException());
                    // Handle error
                }
            }
        });
    }
    /**
     * Filters the list by the description keywords in the search bar.
     * @param query the keyword to filter the description of an item by.
     */
    private void filterList(String query) {
        filteredDesc.clear();

        if (query.isEmpty()) {
            filteredDesc.addAll(items); // If the query is empty, show all items
        } else {
            for (Item item : items) {
                // Check if the make, model, or description contains the query (case-insensitive)
                if (item.getMake() != null && item.getMake().toLowerCase().contains(query.toLowerCase())
                        || item.getModel() != null && item.getModel().toLowerCase().contains(query.toLowerCase())
                        || item.getDescription() != null && item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredDesc.add(item);
                }
            }
        }
        // Update the adapter and refresh the list
        itemAdapter = new ItemList(this, filteredDesc);
        itemAdapter.notifyDataSetChanged();
        itemList.setAdapter(itemAdapter);
    }

    @Override
    public void onFilterApplied(Map<String, Set<String>> selectedFilters) {
        // Call a method in your ListActivity to apply the filter
        applyFilter(selectedFilters);
    }

    public void applyFilter(Map<String, Set<String>> selectedFilters) {
        // Update the applied filters
        appliedMakes.clear();
        appliedTags.clear();
        appliedDate.clear();

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
                case "date":
                    appliedDate.addAll(values);
                    break;
            }
        }

        // Filter the items based on the selected filters from the original list
        List<Item> filteredItems = filterItems(originalItems, selectedFilters);

        // Update the adapter with the filtered items
        itemAdapter.clear();
        itemAdapter.addAll(filteredItems);
        itemAdapter.notifyDataSetChanged();

        if(items.size() == 0) {
            banner.setVisibility(View.VISIBLE);
        }
        else {
            banner.setVisibility(View.INVISIBLE);
        }

        // Recalculate and update total value
        updateTotalValue(filteredItems);
    }


    private List<Item> filterItems(List<Item> originalItems, Map<String, Set<String>> selectedFilters) {
        List<Item> filteredItems = new ArrayList<>();

        Set<String> dateRangeSet = selectedFilters.get("date");
        List<Integer> fromDate = new ArrayList<>();
        List<Integer> toDate = new ArrayList<>();

        if (dateRangeSet != null && !dateRangeSet.isEmpty()) {
            String dateRange = dateRangeSet.iterator().next();
            previousDate = dateRange;
            String[] dateParts = dateRange.split(" TO ");
            fromDate = parseDate(dateParts[0]);
            toDate = parseDate(dateParts[1]);
        }

        for (Item item : originalItems) {
            Set<String> itemMakes = new HashSet<>(Collections.singletonList(item.getMake()));
            Set<String> itemTags = item.getTags() != null ? new HashSet<>(item.getTags()) : Collections.emptySet();
            // Get other criteria as needed

            // Check if the item matches the selected filters for all criteria
            boolean makesMatch = selectedFilters.get("makes").isEmpty() || !Collections.disjoint(itemMakes, selectedFilters.get("makes"));
            boolean tagsMatch = selectedFilters.get("tags").isEmpty() || !Collections.disjoint(itemTags, selectedFilters.get("tags"));
            boolean dateMatch = filterDate(fromDate, toDate, item.getDate());

            if (makesMatch && tagsMatch && dateMatch) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    public void clearFilters() {
        if(items.size() == 0) {
            banner.setVisibility(View.VISIBLE);
        }
        else {
            banner.setVisibility(View.INVISIBLE);
        }

        // Clear the appliedMakes and appliedTags sets
        appliedMakes.clear();
        appliedTags.clear();
        clearPreviousFilterDate();

        // Reset the adapter with the original list
        itemAdapter.clear();
        itemAdapter.addAll(originalItems);
        itemAdapter.notifyDataSetChanged();

        // Recalculate and update total value for the original list
        updateTotalValue(originalItems);
    }

    public void clearPreviousFilterDate() {
        previousDate = "";
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

    /**
     * Shows the sort dialog fragment where the list can be sorted by make, date, value, description, or tags
     * in ascending or descending order.
     */
    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_sort, null);

        String[] order = {"Ascending", "Descending"};
        String[] fields = {"Default", "Make", "Date", "Value", "Description", "Tags"};

        orderadapter = new ArrayAdapter<String>(ListActivity.this, R.layout.spinner_view, order);
        fieldadapter = new ArrayAdapter<String>(ListActivity.this, R.layout.spinner_view, fields);
        orderadapter.setDropDownViewResource(R.layout.spinner_view);
        fieldadapter.setDropDownViewResource(R.layout.spinner_view);

        final Spinner fieldSpinner = view.findViewById(R.id.fieldSpinner);
        final Spinner orderSpinner = view.findViewById(R.id.orderSpinner);


        fieldSpinner.setAdapter(fieldadapter);
        orderSpinner.setAdapter(orderadapter);

        if(fieldSelected == "") {
            fieldSpinner.setSelection(0);
        } else if (fieldSelected == "Make") {
            fieldSpinner.setSelection(1);
        } else if (fieldSelected == "Date") {
            fieldSpinner.setSelection(2);
        } else if (fieldSelected == "Value") {
            fieldSpinner.setSelection(3);
        } else if (fieldSelected == "Description") {
            fieldSpinner.setSelection(4);
        } else if (fieldSelected == "Tags") {
            fieldSpinner.setSelection(5);
        }

        if(orderSelected == "") {
            orderSpinner.setSelection(0);
        } else if (orderSelected == "Ascending") {
            orderSpinner.setSelection(0);
        } else if (orderSelected == "Descending") {
            orderSpinner.setSelection(1);
        }

        builder.setView(view);
        Button possitiveButton = view.findViewById(R.id.applySortButton);
        Button negativeButton = view.findViewById(R.id.cancelSortButton);
        AlertDialog alertDialog = builder.create();

        possitiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fieldData = fieldSpinner.getSelectedItem().toString();
                fieldSelected =  fieldData;
                orderData = orderSpinner.getSelectedItem().toString();
                orderSelected = orderData;
                sortList(fieldData, orderData);
                alertDialog.dismiss();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    /**
     * Sorts the list based on the field and order selected from the sort dialog fragment.
     * @param field which field to sort by. Can pick default, make, date value, description, or tags.
     * @param order which order to sort the field by. Can either be ascending or descending.
     */
    private void sortList(String field, String order) {
        if(field.equals("Default")) {
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(Item::getID));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(Item::getID));
                Collections.reverse(items);
            }
        }
        if(field.equals("Make")){
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(Item::getMake));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(Item::getMake));
                Collections.reverse(items);
            }
        }
        if(field.equals("Date")){
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(Item::getDate));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(Item::getDate));
                Collections.reverse(items);
            }
        }
        if(field.equals("Value")){
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(Item::getEst_value));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(Item::getEst_value));
                Collections.reverse(items);
            }
        }
        if(field.equals("Description")){
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(Item::getDescription));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(Item::getDescription));
                Collections.reverse(items);
            }
        }
        if(field.equals("Tags")) {
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(Item::getTagSize));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(Item::getTagSize));
                Collections.reverse(items);
            }
        }
        itemAdapter.notifyDataSetChanged();
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


    private boolean filterDate(List<Integer> fromDate, List<Integer> toDate, String itemDate) {
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            return true; // No date range specified, so no filtering needed
        }

        String[] dateParts = itemDate.split("-");
        int day = Integer.parseInt(dateParts[2]);
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[0]);

        boolean withinFROMrange = false, withinTOrange = false;

        if (year > fromDate.get(0)) {
            withinFROMrange = true;
        } else if (year == fromDate.get(0)) {
            if (month > fromDate.get(1)) {
                withinFROMrange = true;
            } else if (month == fromDate.get(1)) {
                if (day >= fromDate.get(2)) {
                    withinFROMrange = true;
                }
            }
        }

        if (year < toDate.get(0)) {
            withinTOrange = true;
        } else if (year == toDate.get(0)) {
            if (month < toDate.get(1)) {
                withinTOrange = true;
            } else if (month == toDate.get(1)) {
                if (day <= toDate.get(2)) {
                    withinTOrange = true;
                }
            }
        }

        return withinFROMrange && withinTOrange;
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
}