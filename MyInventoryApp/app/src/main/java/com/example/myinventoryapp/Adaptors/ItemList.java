package com.example.myinventoryapp.Adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myinventoryapp.ItemManagement.Item;
import com.example.myinventoryapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemList is a custom ArrayAdapter used for displaying a list of items in the UI.
 * It extends ArrayAdapter and provides a custom implementation for the getView method
 * to show item details including an image, estimated value and tags in the list.
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
        TextView make = view.findViewById(R.id.makeView);
        TextView model = view.findViewById(R.id.modelView);
        TextView tags = view.findViewById(R.id.tagsView);

        // Set values for item properties in textviews.
        if (item.getImage(0) != null) {
            photo.setImageBitmap(item.getImage(0));

        } else {
            photo.setImageResource(R.drawable.no_image);
        }
        value.setText("$" + item.getEst_value());

        // Show ... if string for make and model is too long
        int max_string = 25;
        if (item.getMake().length() <= max_string){
            make.setText(item.getMake());
        } else {
            make.setText(item.getMake().substring(0,max_string) + "...");
        }
        if (item.getModel().length() <= max_string){
            model.setText(item.getModel());
        } else {
            model.setText(item.getModel().substring(0,max_string) + "...");
        }

        if (item.getTags() != null && item.getTags().size() > 0){
            tags.setText("Tags: " + String.join(", ", item.getTags()));
        } else {
            tags.setText("Tags: None");
        }

        return view;
    }

}