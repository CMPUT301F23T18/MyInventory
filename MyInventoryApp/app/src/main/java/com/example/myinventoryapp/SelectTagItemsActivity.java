package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.List;

public class SelectTagItemsActivity  extends AppCompatActivity {
    RecyclerView itemList;
    SelectListAdaptor itemAdapter;
    ArrayList<Item> items;
    TextView add_tags_btn;
    Button selectAll_btn, unselectAll_btn;
    ImageView exit_btn;
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

    public ArrayList<Item> CheckedItems(){
        ArrayList<Item> tag_items = new ArrayList<>();
        for(int i = 0; i < items.size();i++){
            if (items.get(i).getChecked()){
                tag_items.add(items.get(i));
            }
        }
        return tag_items;
    }
}
