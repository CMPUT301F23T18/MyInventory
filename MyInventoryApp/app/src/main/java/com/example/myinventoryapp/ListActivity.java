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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class ListActivity extends AppCompatActivity {
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayList<Item> items;
    double totalValue = 0;
    TextView totalCostView;
    Button filterbutton, sortbutton, deleteButton, yes_button, no_button;
    CheckBox item_checkBox;
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
            public void onEvent(@Nullable QuerySnapshot querySnapshots,@Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    items.clear();
                    for (QueryDocumentSnapshot doc: querySnapshots) {
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
                    for (int i = 0; i < items.size(); i++){
                        String est_value =  items.get(i).getEst_value();
                        if (est_value != null){
                            totalValue += Double.parseDouble(est_value);
                        }

                    }
                    totalCostView.setText(String.format(Locale.CANADA,"Total Value = $%.2f", totalValue));
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
        filterbutton = findViewById(R.id.filterButton);
        sortbutton = findViewById(R.id.sortButton);
        yes_button = findViewById(R.id.yes_delete);
        no_button = findViewById(R.id.no_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set the visibility of buttons and checkboxes
                deleteButton.setVisibility(View.INVISIBLE);
                filterbutton.setVisibility(View.GONE);
                sortbutton.setVisibility(View.GONE);
                addButton.setVisibility(View.INVISIBLE);
                totalCostView.setVisibility(View.INVISIBLE);
                yes_button.setVisibility(View.VISIBLE);
                no_button.setVisibility(View.VISIBLE);
                for(int i = 0; i < items.size();i++){
                    CheckBox cBox=(CheckBox)itemList.getChildAt(i).findViewById(R.id.check);
                    cBox.setVisibility(View.VISIBLE);
                }
            }
        });
        //TODO: get data from checked boxes to delete

        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.VISIBLE);
                filterbutton.setVisibility(View.VISIBLE);
                sortbutton.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
                totalCostView.setVisibility(View.VISIBLE);
                yes_button.setVisibility(View.GONE);
                no_button.setVisibility(View.GONE);
                for(int i = 0; i < items.size();i++){
                    CheckBox cBox=(CheckBox)itemList.getChildAt(i).findViewById(R.id.check);
                    cBox.setVisibility(View.INVISIBLE);
                }
            }
        });

        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: show fragment asking to confirm deletion
            }
        });

        //TODO: add fragment layout
    }

    AdapterView.OnItemClickListener itemClicker = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent(view.getContext(), ViewItemActivity.class);
            long ID = items.get(position).getID();
            i.putExtra("ID",ID);
            startActivity(i);
        }
    };
}

