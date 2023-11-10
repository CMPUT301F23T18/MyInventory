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

public class SelectListAdaptor extends RecyclerView.Adapter<SelectListAdaptor.ViewHolder> {

    private ArrayList<Item> items;
    private Context context;
    private boolean select = false, unselect = false;

    public SelectListAdaptor(Context context, ArrayList<Item> expenses){
        this.items = expenses;
        this.context = context;

    }

    @NonNull
    @Override
    public SelectListAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_list_content, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectListAdaptor.ViewHolder holder, int position) {
        final int i = position;
        holder.photo.setImageResource(R.drawable.house_placeholder);
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
}