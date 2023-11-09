package com.example.myinventoryapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListActivity extends AppCompatActivity implements DeleteFragment.OnFragmentInteractionListener{
    final long ONE_MEGABYTE = 1024 * 1024;
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayList<Item> items;
    List<Integer> delete_items;
    double totalValue = 0;
    TextView totalCostView;
    Button filterbutton, sortbutton, deleteButton, yes_button, no_button, tagButton, add_tags_button, cancel_tags_button;

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
                        String id = doc.getId();
                        item.setSerial_num(doc.getString("serial"));
                        item.setDate(doc.getString("date"));
                        item.setMake(doc.getString("make"));
                        item.setModel(doc.getString("model"));
                        item.setEst_value(doc.getString("price"));
                        item.setDescription(doc.getString("desc"));
                        item.setID(Long.parseLong(id));


                        // set photos
                        StorageReference photosRef = ((Global) getApplication()).getPhotoStorageRef();
                        for (int i = 0; i < 6; ++i) {
                            // set path for current image
                            StorageReference photoRef = photosRef.child(id + "/image" + i + ".jpg");
                            photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // get the bitmap of the byte array and add it to the item's list
                                    Bitmap img_bit = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                    item.addImage(img_bit);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    // Note: there is an error when the image isn't
                                    // found, so we leave this blank to avoid printing the message
                                }
                            }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                @Override
                                public void onComplete(@NonNull Task<byte[]> task) {
                                    // Update the list when finished to load all the pictures
                                    itemAdapter.notifyDataSetChanged();
                                }
                            });
                        }

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
        yes_button = findViewById(R.id.yes_delete);
        no_button = findViewById(R.id.no_delete);
        add_tags_button = findViewById(R.id.add_tag);
        cancel_tags_button = findViewById(R.id.no_tag);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set the visibility of buttons and checkboxes
                deleteButton.setVisibility(View.INVISIBLE);
                filterbutton.setVisibility(View.GONE);
                sortbutton.setVisibility(View.GONE);
                addButton.setVisibility(View.INVISIBLE);
                totalCostView.setVisibility(View.INVISIBLE);
                tagButton.setVisibility(View.INVISIBLE);
                yes_button.setVisibility(View.VISIBLE);
                no_button.setVisibility(View.VISIBLE);
                for (int i = 0; i < items.size(); i++) {
                    CheckBox cBox = (CheckBox) itemList.getChildAt(i).findViewById(R.id.check);
                    cBox.setVisibility(View.VISIBLE);
                }
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

        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });
        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteFragment().show(getSupportFragmentManager(), "Delete_item");
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
                startActivity(new Intent(ListActivity.this, TagsActivity.class));
            }
        });

        //TODO: add fragment layout
    }
        private void Reset(){
            deleteButton.setVisibility(View.VISIBLE);
            filterbutton.setVisibility(View.VISIBLE);
            sortbutton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            totalCostView.setVisibility(View.VISIBLE);
            yes_button.setVisibility(View.GONE);
            no_button.setVisibility(View.GONE);
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

        private void DeleteItems() {
            List<Integer> temp_list = CheckedItems();
            CollectionReference fb_items = ((Global) getApplication()).getFbItemsRef();
            for (int i = 0; i < temp_list.size(); i++) {
                long id = items.get(temp_list.get(i)).getID();
                int position = temp_list.get(i);
                items.remove(position);
                fb_items.document(Long.toString(id)).delete();
                itemAdapter.notifyDataSetChanged();
            }
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

    @Override
    public void onYESPressed() {
        DeleteItems();
        Reset();
    }
}