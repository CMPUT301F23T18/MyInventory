package com.example.myinventoryapp;

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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
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
    ArrayAdapter<String> spinneradapter;
    ArrayList<Item> items, filtered_items, temp_list;
    List<Integer> delete_items;
    double totalValue = 0;
    TextView totalCostView;
    Button filterbutton, sortbutton, deleteButton, tagButton;
    String makeData, tagData, dateData, descData, valData, dateString = "";

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

        // Reset user id in case it's necessary.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        ((Global) getApplication()).setUSER_PATH(mAuth.getCurrentUser().getUid());

        items = new ArrayList<>();
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

                        if (doc.contains("tags")){
                            List<String> tags = (List<String>) doc.get("tags");
                            item.setTags(tags);
                        }

                        // set photos
                        StorageReference photosRef = ((Global) getApplication()).getPhotoStorageRef();
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

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_filter, null);

        builder.setView(view)
                .setTitle("Apply Filters");
        Button positiveButton = view.findViewById(R.id.positive);
        Button negativeButton = view.findViewById(R.id.negative);
        AlertDialog alertDialog = builder.create();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    private void showSortDialog() {
        //TODO
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        LayoutInflater inflater = getLayoutInflater();
//        View view = inflater.inflate(R.layout.fragment_sort, null);
//
//        String[] options = {"None", "Ascending", "Descending"};
//        spinneradapter = new ArrayAdapter<String>(ListActivity.this, android.R.layout.simple_spinner_item, options);
//        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        final Spinner makeSpinner = view.findViewById(R.id.makeSpinner);
//        final Spinner tagSpinner = view.findViewById(R.id.tagsSpinner);
//        final Spinner dateSpinner = view.findViewById(R.id.dateSpinner);
//        final Spinner descSpinner = view.findViewById(R.id.descSpinner);
//        final Spinner valSpinner = view.findViewById(R.id.valueSpinner);
//
//        makeSpinner.setAdapter(spinneradapter);
//        tagSpinner.setAdapter(spinneradapter);
//        dateSpinner.setAdapter(spinneradapter);
//        descSpinner.setAdapter(spinneradapter);
//        valSpinner.setAdapter(spinneradapter);
//
//        builder.setView(view)
//                .setTitle("Apply Filters");
//        Button possitiveButton = view.findViewById(R.id.applySortButton);
//        Button negativeButton = view.findViewById(R.id.cancelSortButton);
//        AlertDialog alertDialog = builder.create();
//
//        possitiveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                makeData = makeSpinner.getSelectedItem().toString();
//                tagData = tagSpinner.getSelectedItem().toString();
//                dateData = dateSpinner.getSelectedItem().toString();
//                descData = descSpinner.getSelectedItem().toString();
//                valData = valSpinner.getSelectedItem().toString();
//                sortList(makeData, tagData, dateData, descData, valData);
//            }
//        });
//        negativeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//            }
//        });
//        alertDialog.show();
    }

    private void sortList(String makeData, String tagData, String dateData, String descData, String valData) {
//        if (makeData.equals("Ascending")){
////            Collections.sort(items, new Comparator<Item>() {
////                @Override
////                public int compare(Item o1, Item o2) {
////                    return o1.getMake().compareTo(o2.getMake());
////                }
////            });
//            Log.d("spinner","make:ascending");
//        } else if (makeData.equals("Descending")) {
//            Log.d("spinner","make:descending");
//        }
//        if (tagData.equals("Ascending")) {
//            Log.d("spinner","tag:ascending");
//        } else if (tagData.equals("Descending")) {
//            Log.d("spinner","tag:descending");
//        }
//        if (dateData.equals("Ascending")) {
//            Log.d("spinner","date:ascending");
//        } else if (dateData.equals("Descending")) {
//            Log.d("spinner","date:descending");
//        }
//        if (descData.equals("Ascending")) {
//            Log.d("spinner","desc:ascending");
//        } else if (descData.equals("Descending")) {
//            Log.d("spinner","desc:descending");
//        }
//        if (valData.equals("Ascending")) {
//            Log.d("spinner","val:ascending");
//        } else if (valData.equals("Descending")) {
//            Log.d("spinner","val:descending");
//        }
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
        }
        //TODO: filter by make
        //TODO: filter bt tags
        //TODO: filter by description
        if (fromDate.size() > 0) {
            filtered_items = filterDate(fromDate, toDate, filtered_items);
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