package com.example.myinventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ItemList extends ArrayAdapter<Item> {

    private ArrayList<Item> items;
    private Context context;

    public ItemList(Context context, ArrayList<Item> items){
        super(context, 0, items);
        this.items = items;
        this.context = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_content,parent, false);
        }
        Item item = items.get(position);
        // Get a item using position
        ImageView photo = view.findViewById(R.id.itemImageView);
        TextView value = view.findViewById(R.id.itemCostView);

        if (item.getImage(0) != null) {
            photo.setImageBitmap(item.getImage(0));

        } else {
            photo.setImageResource(R.drawable.bg_colored_image);
        }
        value.setText("$ " + item.getEst_value());

        return view;
    }
}