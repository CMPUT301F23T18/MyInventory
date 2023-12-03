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

/**
 * Activity displays a checked list of items and provides options to user including select and
 * unselect all, and delete selected items. Additionally, the user can also cancel and or exit the
 * activity without deleting an item(s) from the list.
 */
public class DeleteActivity extends AppCompatActivity implements DeletePopUp.OnFragmentInteractionListener{
    RecyclerView itemList;
    SelectListAdaptor itemAdapter;
    ArrayList<Item> items;
    Button delete_btn;
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

    /**
     * Returns a list of selected items that are going to be deleted.
     * @return delete_items the list of item IDs
     */
    public List<Long> CheckedItems(){
        List<Long> delete_items = new ArrayList<>();
        for(int i = 0; i < items.size();i++){
            if (items.get(i).getChecked()){
                delete_items.add(items.get(i).getID());
            }
        }
        return delete_items;
    }

    /**
     * Deletes the selected Items associated with their appropriate ID that are on the delete_list.
     * @param delete_list list of IDs that were selected
     */
    private void DeleteItems(List<Long> delete_list) {
        String str = "";
        if(delete_list.size() == 1) {
            str = "1 item deleted";
        }else{
            str = delete_list.size()+" items deleted";
        }
        DeleteImages();
        CollectionReference fb_items = ((Global) getApplication()).getFbItemsRef();
        for(int i = 0; i < delete_list.size();i++){
            fb_items.document(Long.toString(delete_list.get(i))).delete();
        }
        finish();
        Toast.makeText(DeleteActivity.this, str ,Toast.LENGTH_SHORT).show();
    }
    /**
     * Deletes the images of the selected delete items from the Firebase storage, if there are any.
     */
    private void DeleteImages(){
        StorageReference photoRef = ((Global) getApplication()).getPhotoStorageRef();
        for(int i = 0; i < items.size();i++){
            //String str_id = String.valueOf(items.get(i).getID());
            if (items.get(i).getChecked() && (items.get(i).getPhotosSize() >0)){
                for(int j = 0; j < items.get(i).getPhotosSize();j++){
                    photoRef.child(items.get(i).getID()+"/image"+(j)+".jpg").delete();
                }
            }
        }
    }

    /**
     * Invoked when the "YES" button is pressed in the delete alert dialog popup.
     */
    @Override
    public void onYESPressed() {
        List<Long> list = CheckedItems();
        DeleteItems(list);
    }
}
