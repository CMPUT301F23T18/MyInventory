package com.example.myinventoryapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class TagsActivity extends AppCompatActivity {
    Button back_button, add_button, save_button;
    EditText tagEditText;
    ListView tagList;
    ArrayAdapter<String> tagAdaptor;
    ArrayList<Item> items;
    ArrayList<String> dataList;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

        items = new ArrayList<>();
        items = getIntent().getParcelableArrayListExtra("items");

        tagList = findViewById(R.id.tags_list);

        // TODO: Get tags from firebase
        String[] tags = {"Lorem", "Ipsum", "Dolor"};

        dataList = new ArrayList<>();
        dataList.addAll(Arrays.asList(tags));

        tagAdaptor = new ArrayAdapter<>(this, R.layout.tags_content, dataList);
        tagList.setAdapter(tagAdaptor);

        add_button = findViewById(R.id.create_tag);
        back_button = findViewById(R.id.backButton2);
        tagEditText = findViewById(R.id.tagEditText);
        save_button = findViewById(R.id.save_tag);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Standardize capitalization of tag.
                String userInput = StringUtils.capitalize(tagEditText.getText().toString());

                if (dataList.contains(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag already created", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag cannot be empty", Toast.LENGTH_SHORT).show();
                } else if("None".equals(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag cannot be called None", Toast.LENGTH_SHORT).show();
                    tagEditText.setText("");
                } else {
                    tagEditText.setText("");
                    dataList.add(userInput);

                    // TODO: Add tag to Firebase

                    tagAdaptor.notifyDataSetChanged();
                }
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Save tags for items in firebase
                finish();
            }
        });
    }

}
