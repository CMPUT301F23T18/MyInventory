package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class TagsActivity extends AppCompatActivity {
    Button back_button, add_button;
    ListView tagList;
    ArrayAdapter<String> tagAdaptor;
    ArrayList<String> dataList;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

        tagList = findViewById(R.id.tags_list);

        // TODO: Get tags from firebase
        String[] tags = {"Lorem", "Ipsum", "Dolor", "Sit", "Amet", "Consectetur"};

        dataList = new ArrayList<>();
        dataList.addAll(Arrays.asList(tags));

        tagAdaptor = new ArrayAdapter<>(this, R.layout.tags_content, dataList);
        tagList.setAdapter(tagAdaptor);

        add_button = findViewById(R.id.create_tag);
        back_button = findViewById(R.id.backButton2);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
