package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displays a checked list of items and provides options to user including select and
 * unselect all, for choosing items they want to add tags to. Additionally, the user can also exit the activity.
 */
public class SelectTagItemsActivity  extends AppCompatActivity {
    RecyclerView itemList;
    SelectListAdaptor itemAdapter;
    ArrayList<Item> items;
    TextView add_tags_btn;
    Button selectAll_btn, unselectAll_btn;
    ImageView exit_btn;

    /**
     * This is called to initialize any UI components, and to also retrieve item data from the
     * intent.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tag_items);

        add_tags_btn = findViewById(R.id.add_tag_button);
        selectAll_btn = findViewById(R.id.selectallButton);
        unselectAll_btn = findViewById(R.id.unselectallButton);
        exit_btn = findViewById(R.id.exitButton);
        itemList = findViewById(R.id.delete_list);

        items = new ArrayList<>();
        items = getIntent().getParcelableArrayListExtra("list");
        StorageReference photoRef = ((Global) getApplication()).getPhotoStorageRef();
        for (Item item:items) {
            item.generatePhotoArray(photoRef, String.valueOf(item.getID()), new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    itemAdapter.notifyDataSetChanged();
                }
            });
        }

        itemList.setHasFixedSize(true);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        itemAdapter = new SelectListAdaptor(this, items);

        itemList.setAdapter(itemAdapter);


        add_tags_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemAdapter.getCheckedCount()>0){
                    // Pass checked items
                    ArrayList<Item> listToAdd = CheckedItems();
                    Intent i = new Intent(SelectTagItemsActivity.this, TagsActivity.class);
                    i.putParcelableArrayListExtra("items", listToAdd);
                    startActivity(i);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please select item(s) for adding tags.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        selectAll_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemAdapter.selectAll();
            }
        });

        unselectAll_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemAdapter.unselectAll();
            }
        });

    }

    /**
     * Finds all the selected items and returns them
     * @return selected items
     */
    public ArrayList<Item> CheckedItems(){
        ArrayList<Item> tag_items = new ArrayList<>();
        for(int i = 0; i < items.size();i++){
            if (items.get(i).getChecked()){
                tag_items.add(items.get(i));
                Log.d("Tags Id", String.valueOf(items.get(i).getID()));
            }
        }
        return tag_items;
    }
}
