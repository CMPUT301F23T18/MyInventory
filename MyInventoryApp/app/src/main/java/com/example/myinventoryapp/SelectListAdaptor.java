package com.example.myinventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * SelectListAdaptor is a custom ArrayAdapter used for displaying a list of items in the UI.
 * It extends RecyclerView.Adapter.
 */
public class SelectListAdaptor extends RecyclerView.Adapter<SelectListAdaptor.ViewHolder> {

    private ArrayList<Item> items;
    private Context context;
    private boolean select = false, unselect = false;

    /**
     * Initializes SelectListAdaptor
     * @param context Context
     * @param expenses List of items to be displayed
     */
    public SelectListAdaptor(Context context, ArrayList<Item> expenses){
        this.items = expenses;
        this.context = context;

    }

    /**
     * Custom onCreateViewHolder
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public SelectListAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_list_content, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    /**
     * Custom onBindViewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull SelectListAdaptor.ViewHolder holder, int position) {
        final int i = position;
        Item item = items.get(i);
        if (item.getImage(0) != null) {
            holder.photo.setImageBitmap(item.getImage(0));

        } else {
            holder.photo.setImageResource(R.drawable.no_image);
        }

        holder.value.setText("$ " + items.get(i).getEst_value());
        if(select){
            holder.cBox.setChecked(true);
            items.get(i).setChecked(true);
        } else if (unselect) {
            holder.cBox.setChecked(false);
            items.get(i).setChecked(false);
        }
        else{
            holder.cBox.setChecked(items.get(i).getChecked());
        }
        holder.cBox.setTag(items.get(i));

        holder.cBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                Item item_selected = (Item) v.getTag();

                item_selected.setChecked(checkBox.isChecked());
                items.get(i).setChecked(checkBox.isChecked());
            }
        });
    }

    /**
     * Returns the total number of items.
     * @return total number of items
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Returns the number of checked items.
     * @return Number of checked items
     */
    public int getCheckedCount(){
        int count = 0;
        for(int i=0; i< items.size();i++){
            if(items.get(i).getChecked()){
                count++;
            }
        }
        return count;
    }

    /**
     * Selects all items.
     */
    public void selectAll(){
        select = true;
        unselect = false;
        notifyDataSetChanged();
    }

    /**
     * Unselects all items.
     */
    public void unselectAll(){
        unselect = true;
        select = false;
        notifyDataSetChanged();
    }

    /**
     * Custom ViewHolder.
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox cBox;
        ImageView photo;
        TextView value;
        public ViewHolder(@NonNull View view){
            super(view);
            photo = view.findViewById(R.id.itemImageView);
            value = view.findViewById(R.id.itemCostView);
            cBox = view.findViewById(R.id.check);
        }
    }
}