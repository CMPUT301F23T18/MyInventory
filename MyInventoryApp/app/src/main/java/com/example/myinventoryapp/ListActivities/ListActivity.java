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
import android.widget.Toast;

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
<<<<<<< HEAD
import java.util.Map;
import java.util.Objects;
=======
>>>>>>> 5084f201217c3f2746a0d849117186280ad896da
import java.util.Set;

/**
 * This is a class that represents an activity that displays the list of items.
 * The user can add, select, view, and delete items in the list
 */
public class ListActivity extends AppCompatActivity implements FilterDialogFragment.OnFragmentInteractionListener, FilterDialogFragment.FilterListener {
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
    private String dateRange;
    boolean filtered;
    // Original list to store all items
    private ArrayList<Item> originalItems;
    private Set<String> appliedMakes = new HashSet<>();
    private Set<String> appliedTags = new HashSet<>();


    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
<<<<<<< HEAD
        if (savedInstanceState == null) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.item_list);
            totalCostView = findViewById(R.id.totalCostView);
            itemList = findViewById(R.id.item_list);
=======
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);
        totalCostView = findViewById(R.id.totalCostView);
        itemList = findViewById(R.id.item_list);
        banner = findViewById(R.id.nothingtoshowbanner);
>>>>>>> 5084f201217c3f2746a0d849117186280ad896da

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
            ImageView profileButton = findViewById(R.id.profileMain);

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
        FilterDialogFragment filterDialog = new FilterDialogFragment(originalMakes, originalTags, appliedMakes, appliedTags, this);
        Bundle args = new Bundle();
        args.putString("dateString",""); // Provide a default date string if needed
        filterDialog.setArguments(args);
        filterDialog.show(getSupportFragmentManager(), "FilterDialogFragment");
    }

    @Override
    public void onFragmentInteraction(Map<String, Set<String>> selectedFilters) {
        // Handle fragment interaction (if needed)
        // This method is part of the OnFragmentInteractionListener interface
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
<<<<<<< HEAD

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
=======
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
               //showAlertDialog();
                Bundle bundle = new Bundle();
                FilterDialogFragment filter_fragment = new FilterDialogFragment();
                bundle.putStringArrayList("makesList",getMakesListFromItems());
                bundle.putStringArrayList("tagsList",getTagsListFromItems());
                bundle.putString("dateString", dateRange);
                filter_fragment.setArguments(bundle);
                filter_fragment.show(getSupportFragmentManager(), "filter_items");
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
>>>>>>> 5084f201217c3f2746a0d849117186280ad896da
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

    private ArrayList<String> getMakesListFromItems() {
        ArrayList<String> makesList = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            String make = items.get(i).getMake();
            if (make != null) {
                makesList.add(make);
            }
        }
        return makesList;
    }

    private ArrayList<String> getTagsListFromItems() {
        ArrayList<String> tagsList = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            List<String> tags = items.get(i).getTags();
            if (tags != null) {
                for (String tag : tags) {
                    // Check if tag is not null and not already in the list
                    if (tag != null && !tagsList.contains(tag)) {
                        tagsList.add(tag);
                    }
                }
            }
        }
        return tagsList;
    }
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

        fieldSpinner.setSelection(0);

        builder.setView(view);
        Button possitiveButton = view.findViewById(R.id.applySortButton);
        Button negativeButton = view.findViewById(R.id.cancelSortButton);
        AlertDialog alertDialog = builder.create();

        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fieldSpinner.getSelectedItem().toString() != "Default") {
                    orderSpinner.setVisibility(View.VISIBLE);
                    orderSpinner.setSelection(0);
                }
                else{
                    orderSpinner.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        possitiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fieldData = fieldSpinner.getSelectedItem().toString();
                orderData = orderSpinner.getSelectedItem().toString();
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
            if (order.equals("Ascending")) {
                Collections.sort(items, Comparator.comparing(obj -> obj.getTags().size()));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(obj -> obj.getTags().size()));
                Collections.reverse(items);
            }
        }
        if(field.equals("Tags")){
            if(order.equals("Ascending")){
                Collections.sort(items, Comparator.comparing(obj -> obj.getTags().size()));
            } else if (order.equals("Descending")) {
                Collections.sort(items, Comparator.comparing(obj -> obj.getTags().size()));
                Collections.reverse(items);
            }
        }
        itemAdapter.notifyDataSetChanged();
    }

<<<<<<< HEAD
    private ArrayList<Item> filterDate(List<Integer> fromDate, List<Integer> toDate, ArrayList<Item> filtered_items){
=======
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

    /**
     * Invoked when the "Apply" button is pressed in the filter fragment dialog.
     */
    @Override
    public void onApplyPressed(String selectedDateRange, List<Integer> fromDate, List<Integer> toDate, List<String> fMakes, List<String> fTags) {
        dateRange = selectedDateRange;
        filtered = true;

        if(fromDate.size()==0 && fMakes.size()==0 && fTags.size()==0) {
            filtered = false;
        }

        filtered_items = filterDate(fromDate, toDate);

        if(filtered) {
            itemAdapter = new ItemList(this, filtered_items);
            itemAdapter.notifyDataSetChanged();
            itemList.setAdapter(itemAdapter);
            updateTotalValue(filtered_items);
            if(filtered_items.size() == 0){
                banner.setVisibility(View.VISIBLE);
            } else {
                banner.setVisibility(View.INVISIBLE);
            }
        } else {
            itemAdapter = new ItemList(this, items);
            itemAdapter.notifyDataSetChanged();
            itemList.setAdapter(itemAdapter);
            updateTotalValue(items);
            if(items.size() == 0) {
                banner.setVisibility(View.VISIBLE);
            } else {
                banner.setVisibility(View.INVISIBLE);
            }
        }
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

    private ArrayList<Item> filterDate(List<Integer> fromDate, List<Integer> toDate){
        filtered_items = new ArrayList<>();
>>>>>>> 5084f201217c3f2746a0d849117186280ad896da
        if(fromDate.size()>0){
            int fromday = fromDate.get(2);int frommonth = fromDate.get(1);int fromyear = fromDate.get(0);
            int today = toDate.get(2);int tomonth = toDate.get(1);int toyear = toDate.get(0);
            for(int i = 0; i < items.size(); i++) {
                String[] dateParts = items.get(i).getDate().split("-");
                int day = Integer.parseInt(dateParts[2]);
                int month = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[0]);

                boolean withinFROMrange = false, withinTOrange = false;

                if (year > fromyear) {withinFROMrange = true;}

                if(year == fromyear) {
                    if (month == frommonth) {
                        if (day >= fromday) {withinFROMrange = true;}
                    }
                    else if (month > frommonth) {withinFROMrange = true;}
                }

                if(year < toyear) {withinTOrange = true;}

                if (year == toyear ) {
                    if (month < tomonth) {
                        withinTOrange = true;
                    } else if (month == tomonth) {
                        if (day <= today) {
                            withinTOrange = true;
                        }
                    }
                }
                if (withinFROMrange && withinTOrange) {
                    filtered_items.add(items.get(i));
                }
            }
        }
        return filtered_items;
    }
}