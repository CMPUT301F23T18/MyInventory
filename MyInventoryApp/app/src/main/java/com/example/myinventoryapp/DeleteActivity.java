package com.example.myinventoryapp;

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

public class DeleteActivity extends AppCompatActivity implements DeletePopUp.OnFragmentInteractionListener{
    RecyclerView itemList;
    SelectListAdaptor itemAdapter;
    ArrayList<Item> items;
    TextView delete_btn;
    Button selectAll_btn, unselectAll_btn;
    ImageView exit_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        delete_btn = findViewById(R.id.deleteButton);
        selectAll_btn = findViewById(R.id.selectallButton);
        unselectAll_btn = findViewById(R.id.unselectallButton);
        exit_btn = findViewById(R.id.exitButton);
        itemList = findViewById(R.id.delete_list);

        items = new ArrayList<>();
        // Get photos from firebase
        items = getIntent().getParcelableArrayListExtra("list", Item.class);
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
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemAdapter.getCheckedCount()>0){
                    Bundle bundle = new Bundle();
                    DeletePopUp del_fragment = new DeletePopUp();
                    if(itemAdapter.getCheckedCount()==1){
                        bundle.putString("confirm_text", "Delete 1 item?");
                    }
                    else {
                        String strtext = "Delete "+itemAdapter.getCheckedCount()+" items?";
                        bundle.putString("confirm_text", strtext);
                    }
                    del_fragment.setArguments(bundle);
                    del_fragment.show(getSupportFragmentManager(), "delete_item");
                }
                else{
                    Toast.makeText(DeleteActivity.this, "Please select item(s) to delete.",Toast.LENGTH_SHORT).show();
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

    public List<Long> CheckedItems(){
        List<Long> delete_items = new ArrayList<>();
        for(int i = 0; i < items.size();i++){
            if (items.get(i).getChecked()){
                delete_items.add(items.get(i).getID());
            }
        }
        return delete_items;
    }

    private void DeleteItems(List<Long> delete_list) {
        String str = "";
        if(delete_list.size() == 1) {
            str = "1 item deleted";
        }else{
            str = delete_list.size()+" items deleted";
        }
        CollectionReference fb_items = ((Global) getApplication()).getFbItemsRef();
        for(int i = 0; i < delete_list.size();i++){
            fb_items.document(Long.toString(delete_list.get(i))).delete();
        }
        finish();
        Toast.makeText(DeleteActivity.this, str ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onYESPressed() {
        List<Long> list = CheckedItems();
        DeleteItems(list);
    }
}
