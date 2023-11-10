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

/**
 * ItemList is a custom ArrayAdapter used for displaying a list of items in the UI.
 * It extends ArrayAdapter and provides a custom implementation for the getView method
 * to show item details including an image and estimated value in the list.
 */
public class ItemList extends ArrayAdapter<Item> {

    private ArrayList<Item> items;
    private Context context;


    /**
     * This constructs an ItemList object
     * @param context The context in which the adapter is created.
     * @param items The list of items to be displayed.
     */
    public ItemList(Context context, ArrayList<Item> items){
        super(context, 0, items);
        this.items = items;
        this.context = context;

    }

    /**
     * This is to inflate item_list_content layout which is to show the list of items with its
     * associated price and photograph.
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return
     */
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
        TextView tags = view.findViewById(R.id.tagsView);

        if (item.getImage(0) != null) {
            photo.setImageBitmap(item.getImage(0));

        } else {
            photo.setImageResource(R.drawable.bg_colored_image);
        }
        value.setText("$ " + item.getEst_value());
        if (item.getTags() != null){
        tags.setText("Tags: " + String.join(", ", item.getTags()));
        } else {
            tags.setText("Tags: None");
        }

        return view;
    }
}