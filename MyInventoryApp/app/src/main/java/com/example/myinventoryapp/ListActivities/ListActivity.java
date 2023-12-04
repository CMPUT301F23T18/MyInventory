package com.example.myinventoryapp.ListActivities;

import android.content.DialogInterface;
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
import java.util.List;
import java.util.Locale;

/**
 * This is a class that represents an activity that displays the list of items.
 * The user can add, select, view, and delete items in the list
 */
public class ListActivity extends AppCompatActivity implements FilterDialogFragment.OnFragmentInteractionListener{
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayAdapter<String> orderadapter, fieldadapter;
    ArrayList<Item> items, filtered_items, temp_list, filteredDesc;
    List<Integer> delete_items;
    double totalValue = 0;
    TextView totalCostView;
    Button filterbutton, sortbutton, deleteButton, tagButton;
    String fieldData, orderData, dateString = "";
    boolean filtered;

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

        // Reset user id in case it's necessary. Also if current user is null, performing tests
        // so set it to test user uid.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            ((DatabaseHandler) getApplication()).setUSER_PATH(mAuth.getCurrentUser().getUid());
        } else{
            ((DatabaseHandler) getApplication()).setUSER_PATH("test_user");
        }

        items = new ArrayList<>();
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
                    items.clear();
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

                        List<String> tags = new ArrayList<>();
                        if (doc.contains("tags")){
                            tags = (List<String>) doc.get("tags");
                        }
                        item.setTags(tags);

                        // set photos
                        StorageReference photosRef = ((DatabaseHandler) getApplication()).getPhotoStorageRef();
                        item.generatePhotoArray(photosRef,id,itemAdapter);

                        Log.d("Firestore", String.format("Item(%s, %s) fetched", item.getMake(), item.getModel()));
                        items.add(item);
                    }
                    // Calculate total value after resetting total.
                    totalValue = 0;
                    for (int i = 0; i < items.size(); i++) {
                        String est_value = items.get(i).getEst_value();
                        if (est_value != null) {
                            totalValue += Double.parseDouble(est_value);
                        }

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
                bundle.putString("dateString", dateString);
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

        String[] order = {"Select", "Ascending", "Descending"};
        String[] fields = {"Select", "Make", "Date", "Value", "Description", "Tags"};

        orderadapter = new ArrayAdapter<String>(ListActivity.this, android.R.layout.simple_spinner_item, order);
        fieldadapter = new ArrayAdapter<String>(ListActivity.this, android.R.layout.simple_spinner_item, fields);
        orderadapter.setDropDownViewResource(R.layout.spinner_view);
        fieldadapter.setDropDownViewResource(R.layout.spinner_view);

        final Spinner fieldSpinner = view.findViewById(R.id.fieldSpinner);
        final Spinner orderSpinner = view.findViewById(R.id.orderSpinner);


        fieldSpinner.setAdapter(fieldadapter);
        orderSpinner.setAdapter(orderadapter);

        fieldSpinner.setSelection(0);

        builder.setView(view)
                .setTitle("Apply Filters");
        Button possitiveButton = view.findViewById(R.id.applySortButton);
        Button negativeButton = view.findViewById(R.id.cancelSortButton);
        AlertDialog alertDialog = builder.create();

        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fieldSpinner.getSelectedItem().toString() != "Select") {
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
        if(!field.equals("Select") && order.equals("Select")){
            Toast.makeText(ListActivity.this, "Please select the order to sort "+field+" by" ,Toast.LENGTH_SHORT).show();
        } else{
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

    /**
     * Invoked when the "Apply" button is pressed in the filter fragment dialog.
     */
    @Override
    public void onApplyPressed(String selectedDateRange, List<Integer> fromDate, List<Integer> toDate, List<String> fMakes, List<String> fTags) {
        dateString = selectedDateRange;
        filtered_items = new ArrayList<>();
        if(fromDate.size()==0 && fMakes.size()==0 && fTags.size()==0) {
            filtered_items = items;
            filtered = false;
        }
        //TODO: filter by make
        //TODO: filter bt tags
        //TODO: filter by description
        if (fromDate.size() > 0) {
            filtered_items = filterDate(fromDate, toDate, filtered_items);
            filtered = true;
        }
        itemAdapter = new ItemList(this, filtered_items);
        itemAdapter.notifyDataSetChanged();
        itemList.setOnItemClickListener(itemClicker);
        itemList.setAdapter(itemAdapter);
    }

    private ArrayList<Item> filterDate(List<Integer> fromDate, List<Integer> toDate, ArrayList<Item> filtered_items){
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