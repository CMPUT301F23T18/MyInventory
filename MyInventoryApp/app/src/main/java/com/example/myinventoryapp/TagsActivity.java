package com.example.myinventoryapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TagsActivity extends AppCompatActivity {
    Button back_button, add_button, save_button;
    EditText tagEditText;
    ListView tagList;
    ArrayAdapter<String> tagAdaptor;
    ArrayList<Item> items;
    ArrayList<String> tags;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

        items = new ArrayList<>();
        items = getIntent().getParcelableArrayListExtra("items");

        tagList = findViewById(R.id.tags_list);

        // TODO: Get tags from firebase
        String[] tags_lst = {"Lorem", "Ipsum", "Dolor"};

        this.tags = new ArrayList<>();
        this.tags.addAll(Arrays.asList(tags_lst));

        tagAdaptor = new ArrayAdapter<>(this, R.layout.tags_content, this.tags);
        tagList.setAdapter(tagAdaptor);

        add_button = findViewById(R.id.create_tag);
        back_button = findViewById(R.id.backButton2);
        tagEditText = findViewById(R.id.tagEditText);
        save_button = findViewById(R.id.save_tag);

        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tagList.setItemChecked(position, true);
                view.setBackgroundColor(getResources().getColor(R.color.orange));
                tagAdaptor.notifyDataSetChanged();
                Log.i("Clicked", "Item clicked");
            }
        });
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

                if (TagsActivity.this.tags.contains(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag already created", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag cannot be empty", Toast.LENGTH_SHORT).show();
                } else if("None".equals(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag cannot be called None", Toast.LENGTH_SHORT).show();
                    tagEditText.setText("");
                } else {
                    tagEditText.setText("");
                    TagsActivity.this.tags.add(userInput);

                    // TODO: Add tag to Firebase

                    tagAdaptor.notifyDataSetChanged();
                }
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CollectionReference coll = ((Global)getApplication()).getFBTagsRef();
//                Map<String, Object> tags_hash = new HashMap<String, Object>();
//                tags_hash.put("all_tags", tagList);
//
//                coll.add(tags_hash)
//                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                            @Override
//                            public void onSuccess(DocumentReference documentReference) {
//                                // The item was successfully added, you can get the document ID if needed
//                                String documentId = documentReference.getId();
//                                Log.d("Firestore", "Item added with ID: " + documentId);
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Handle the failure
//                                Log.e("Firestore", "Error adding item", e);
//                            }
//                        });

                for (int i = 0; i < items.size(); i++){
                    Item item = items.get(i);
                    DocumentReference ref = ((Global)getApplication()).DocumentRef(item.getID());
                    Map<String, Object> item_hash = new HashMap<String, Object>();
                    item_hash.put("ID", item.getID());
                    item_hash.put("serial", item.getSerial_num());
                    item_hash.put("date", item.getDate());
                    item_hash.put("make", item.getMake());
                    item_hash.put("model", item.getModel());
                    item_hash.put("price", item.getEst_value());
                    item_hash.put("desc", item.getDescription());
                    item_hash.put("tags", getSelectedTags());
                    ref.set(item_hash).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Firestore","tag saved");
                            } else {
                                Log.w("Firestore","failed:",task.getException());
                            }
                        }
                    });
                }
                finish();
            }
        });
    }

    private ArrayList<String>getSelectedTags(){
        ArrayList<String> selectedTags = new ArrayList<>();
        for (int i = 0; i < tagList.getCount(); i++) {
            if (tagList.isItemChecked(i)) {
                selectedTags.add(tags.get(i));
            }
        }

        return selectedTags;
    }
}
