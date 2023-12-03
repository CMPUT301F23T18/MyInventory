package com.example.myinventoryapp;

import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity displays the current list of saved tags in firebase, allows user to
 * create new tags and set tags to selected items passed from another activity.
 */
public class TagsActivity extends AppCompatActivity {
    Button back_button, add_button, save_button;
    EditText tagEditText;
    ListView tagList;
    ArrayAdapter<String> tagAdaptor;
    ArrayList<Item> items;
    ArrayList<String> tags;
    Map<String, Boolean> selectedTagsMap;

    /**
     * This is called to initialize any UI components, and to also retrieve item data from the
     * intent.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

//        // Get transferred items.
        items = new ArrayList<>();
        items = getIntent().getParcelableArrayListExtra("items");

        // Set adapter
        tagList = findViewById(R.id.tags_list);
        tags = new ArrayList<>();
        tagAdaptor = new ArrayAdapter<>(this, R.layout.tags_content, this.tags);
        tagList.setAdapter(tagAdaptor);
        selectedTagsMap = new HashMap<>();

        // Get tags from firebase
        DocumentReference docRef = ((Global) getApplication()).getFBTagsRef().document("TAGS");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document found retrieve data
                    List<String> tags_list = (List<String>) document.get("all_tags");
                    if (tags_list != null){tags.addAll(tags_list);}
                    tagAdaptor.notifyDataSetChanged();
                    for (String tag: tags){
                        if (!selectedTagsMap.containsKey(tag)){
                            selectedTagsMap.put(tag, false);
                        }
                    }
                }
            } else {
                Log.d("TagsActivity", "Error getting document: " + task.getException());
            }
        });

        add_button = findViewById(R.id.create_tag);
        back_button = findViewById(R.id.backButton2);
        tagEditText = findViewById(R.id.tagEditText);
        save_button = findViewById(R.id.save_tag);

        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected_tag = (String) tagList.getItemAtPosition(position);
                // Unselect already selected tag
                if (selectedTagsMap.get(selected_tag)){
                    selectedTagsMap.put(selected_tag, false);
                    tagList.setItemChecked(position, false);
                    view.setBackgroundColor(getResources().getColor(R.color.bg_white));
                    Log.i("Clicked", "Unselect");
                } else {
                    selectedTagsMap.put(selected_tag, true);
                    tagList.setItemChecked(position, true);
                    view.setBackgroundColor(getResources().getColor(R.color.orange));
                    Log.i("Clicked", "Select");
                }

                tagAdaptor.notifyDataSetChanged();
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
                    tagEditText.setText("");
                }
                else if (TextUtils.isEmpty(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag cannot be empty", Toast.LENGTH_SHORT).show();
                } else if("None".equals(userInput)){
                    Toast.makeText(getApplicationContext(), "Tag cannot be called None", Toast.LENGTH_SHORT).show();
                    tagEditText.setText("");
                } else {
                    tagEditText.setText("");
                    TagsActivity.this.tags.add(userInput);
                    tagAdaptor.notifyDataSetChanged();

                    // Store all current tags to firebase.
                    CollectionReference coll = ((Global)getApplication()).getFBTagsRef();
                    Map<String, Object> tag_hash = new HashMap<String, Object>();
                    tag_hash.put("all_tags", tags);
                    coll.document("TAGS").set(tag_hash).addOnCompleteListener(new OnCompleteListener<Void>() {
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
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store all current tags to firebase.
                CollectionReference coll = ((Global)getApplication()).getFBTagsRef();
                Map<String, Object> tag_hash = new HashMap<String, Object>();
                tag_hash.put("all_tags", tags);
                coll.document("TAGS").set(tag_hash);

                // Store chosen tags to chosen items in firebase.
                for (int i = 0; i < items.size(); i++){
                    Item item = items.get(i);
                    DocumentReference ref = ((Global)getApplication()).DocumentRef(item.getID());
                    Map<String, Object> item_hash = new HashMap<String, Object>();
                    Log.d("Tags Id", String.valueOf(item.getID()));
                    item_hash.put("ID", item.getID());
                    item_hash.put("serial", item.getSerial_num());
                    item_hash.put("date", item.getDate());
                    item_hash.put("make", item.getMake());
                    item_hash.put("model", item.getModel());
                    item_hash.put("price", item.getEst_value());
                    item_hash.put("desc", item.getDescription());

                    // Don't change tags for items if the user didn't select any tags.
                    if (getSelectedTags().size() > 0){
                        item_hash.put("tags", getSelectedTags());
                    } else {
                        item_hash.put("tags", item.getTags());
                    }

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

    /**
     * Finds all the selected tags and returns them
     * @return selected tags
     */
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
