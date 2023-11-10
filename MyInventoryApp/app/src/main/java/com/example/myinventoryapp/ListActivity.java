package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This is a class that represents an activity that displays the list of items.
 * The user can add, select, view, and delete items in the list
 */
public class ListActivity extends AppCompatActivity{
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayList<Item> items;
    List<Integer> delete_items;
    double totalValue = 0;
    TextView totalCostView;
    Button filterbutton, sortbutton, deleteButton, yes_button, no_button, tagButton, add_tags_button, cancel_tags_button;

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
                        item.setSerial_num(doc.getString("serial"));
                        item.setDate(doc.getString("date"));
                        item.setMake(doc.getString("make"));
                        item.setModel(doc.getString("model"));
                        item.setEst_value(doc.getString("price"));
                        item.setDescription(doc.getString("desc"));
                        item.setID(Long.parseLong(doc.getId()));

                        // TODO: Get photo
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
        //TODO: activity_edit needs to have inputType changed for applicable entries

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
        add_tags_button = findViewById(R.id.add_tag);
        cancel_tags_button = findViewById(R.id.no_tag);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Item> listToAdd = new ArrayList<>();
                Intent i = new Intent(ListActivity.this, DeleteActivity.class);
                i.putParcelableArrayListExtra("list",items);
                startActivity(i);
            }
        });
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set the visibility of buttons and checkboxes
                tagButton.setVisibility(View.INVISIBLE);
                filterbutton.setVisibility(View.GONE);
                sortbutton.setVisibility(View.GONE);
                addButton.setVisibility(View.INVISIBLE);
                totalCostView.setVisibility(View.INVISIBLE);
                add_tags_button.setVisibility(View.VISIBLE);
                cancel_tags_button.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.INVISIBLE);
                for (int i = 0; i < items.size(); i++) {
                    CheckBox cBox = (CheckBox) itemList.getChildAt(i).findViewById(R.id.check);
                    cBox.setVisibility(View.VISIBLE);
                }
            }
        });

        cancel_tags_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });

        add_tags_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckedItems().size()>0) {
                    startActivity(new Intent(ListActivity.this, TagsActivity.class));
                }
                else{
                    Toast.makeText(ListActivity.this, "Please select item(s) to add tags to.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Resets the UI components to their original visibility states and unchecks any checked
     * checkboxes.
     * Used after tagging items to revert the UI to its initial state.
     */
    private void Reset(){
            filterbutton.setVisibility(View.VISIBLE);
            sortbutton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            totalCostView.setVisibility(View.VISIBLE);
            tagButton.setVisibility(View.VISIBLE);
            add_tags_button.setVisibility(View.GONE);
            cancel_tags_button.setVisibility(View.GONE);
            for(int i = 0; i < items.size();i++){
                CheckBox cBox=(CheckBox)itemList.getChildAt(i).findViewById(R.id.check);
                if (cBox.isChecked()){
                    cBox.setChecked(false);
                }
                cBox.setVisibility(View.INVISIBLE);
            }
        }

    /**
     * Returns the list of checked item indices that are to be deleted
     * @return delete_items
     */
        public List<Integer> CheckedItems(){
            delete_items = new ArrayList<>();
            for(int i = 0; i < items.size();i++){
                CheckBox cBox=(CheckBox)itemList.getChildAt(i).findViewById(R.id.check);
                if (cBox.isChecked()){
                    delete_items.add(i);
                }
            }
            return delete_items;
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
            startActivity(i);
        }
    };
}