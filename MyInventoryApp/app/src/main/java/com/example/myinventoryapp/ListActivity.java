package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ListActivity extends AppCompatActivity {

    ImageView addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);

        addButton = findViewById(R.id.add_button);
        //TODO: activity_edit needs to have inputType changed for applicable entries
        //TODO: make listview and adapter that shows the items of the list
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), AddActivity.class);
                startActivity(i);
            }
        });
        //TODO: clicking on an item takes it to the activity_review layout
        //TODO: within the layout, it should let you click on the delete button and delete the item.
        // once the item is deleted, return to the listview activity. make sure it deletes from the
        // listview and from firebase.
        //TODO: make activity to go to activity_review

    }
}
