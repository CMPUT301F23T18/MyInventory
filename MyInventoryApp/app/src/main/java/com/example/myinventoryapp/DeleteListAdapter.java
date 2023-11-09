package com.example.myinventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeleteListAdapter extends RecyclerView.Adapter<DeleteListAdapter.ViewHolder> {

    private ArrayList<Item> items;
    private Context context;
    private boolean select = false, unselect = false;

    public DeleteListAdapter(Context context, ArrayList<Item> expenses){
        this.items = expenses;
        this.context = context;

    }

    @NonNull
    @Override
    public DeleteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.delete_content, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteListAdapter.ViewHolder holder, int position) {
        final int i = position;
        holder.photo.setImageResource(R.drawable.house_placeholder);
        holder.value.setText("$ " + items.get(i).getEst_value());
        if(select){
            holder.cBox.setChecked(true);
            items.get(i).setChecked(true);
            holder.cBox.setTag(items.get(i));
        } else if (unselect) {
            holder.cBox.setChecked(false);
            items.get(i).setChecked(false);
            holder.cBox.setTag(items.get(i));
        }
        else{
            holder.cBox.setChecked(items.get(i).getChecked());
            holder.cBox.setTag(items.get(i));
        }
        //holder.cBox.setTag(items.get(i));

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int getCheckedCount(){
        int count = 0;
        for(int i=0; i< items.size();i++){
            if(items.get(i).getChecked()){
                count++;
            }
        }
        return count;
    }

    public void selectAll(){
        select = true;
        unselect = false;
        notifyDataSetChanged();
    }

    public void unselectAll(){
        unselect = true;
        select = false;
        notifyDataSetChanged();
    }

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

    public ArrayList<Item> getItems(){
        return items;
    }
}