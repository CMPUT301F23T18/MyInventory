package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    ImageView addButton;
    ListView itemList;
    ArrayAdapter<Item> itemAdapter;
    ArrayList<Item> items;
    double totalValue = 0;
    TextView totalCostView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);
        totalCostView = findViewById(R.id.totalCostView);
        itemList = findViewById(R.id.item_list);

        items = new ArrayList<>();
        itemAdapter = new ItemList(this, items);
        // Remove later.
        Item item1 = new Item("", "", "", "", "","2000");
        items.add(item1);

        Item item2 = new Item("", "", "", "", "","300");
        items.add(item2);

        Item item3 = new Item("", "", "", "", "","45.5");
        items.add(item3);

        for (int i = 0; i < items.size(); i++){
            totalValue += Double.parseDouble(items.get(i).getEst_value());
        }
        totalCostView.setText(String.format("Total Value = $%.2f", totalValue));

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
    }
}
