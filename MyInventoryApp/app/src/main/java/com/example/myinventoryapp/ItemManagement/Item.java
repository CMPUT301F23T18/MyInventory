package com.example.myinventoryapp.ItemManagement;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Item is the object content item that implements parcelable interface whose instances can be
 * written to and restored from a Parcel - container for a message (data and object references).
 */
public class Item implements Parcelable {
    private String date;
    private String description;
    private String make;
    private String model;
    private String serial_num;
    private String est_value;
    private double est_value_num;
    private String comment;
    private long ID;
    private List<String> tags;
    private boolean selected = false;

    private ArrayList<Bitmap> images = new ArrayList<Bitmap>(6);
    final long ONE_MEGABYTE = 1024 * 1024;
    private String barcode;

    // photos

    /**
     * Constructor for creating an Item object from a Parcel.
     * @param in The Parcel containing the serialized data for reconstructing the Item object.
     */
    protected Item(Parcel in) {
        date = in.readString();
        description = in.readString();
        make = in.readString();
        model = in.readString();
        serial_num = in.readString();
        est_value = in.readString();
        est_value_num = in.readDouble();
        comment = in.readString();
        ID = in.readLong();
        tags = in.createStringArrayList();
    }

    /**
     * A Parcelable.Creator implementation used to create instances of Item from a Parcel.
     * Required for Parcelable objects to be reconstructed from a Parcel.
     */
    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable instance's marshaled representation.
     * This implementation returns 0 as there are no special objects contained in the Item.
     * @return 0 (zero) as there are no special objects contained in the Parcelable instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel. Writes the object's data to the provided Parcel.
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

        dest.writeString(date);
        dest.writeString(description);
        dest.writeString(make);
        dest.writeString(model);
        dest.writeString(serial_num);
        dest.writeString(est_value);
        dest.writeDouble(est_value_num);
        dest.writeString(comment);
        dest.writeLong(ID);
        dest.writeStringList(tags);
    }

    /**
     * Default constructor for creating an empty Item object.
     */
    public Item() {
    }

    /**
     * class generator
     * @param date date item was acquired
     * @param description description of the item
     * @param make make of the item -> brand
     * @param model model of the item
     * @param serial_num serial number for item, as a string
     * @param est_value the estimated value of the item

     */
    public Item(String date, String description, String make, String model, String serial_num, String est_value) {
        // NOTE: comment, tags and photos are NOT added on item creation
        this.date = date;
        this.description = description;
        this.make = make;
        this.model = model;
        this.serial_num = serial_num;
        this.est_value = est_value;
        this.tags = new ArrayList<>();

        if (est_value != null){
            est_value_num = Double.parseDouble(est_value);
        }
    }

    /**
     * Generates the photoArray from firebase for the purpose of populating the ListActivity
     * @param photosRef the reference to the photos storage in firebase
     * @param id the id of this item
     * @param itemAdapter the arrayAdapter for the items in listActivity
     */
    public void generatePhotoArray(StorageReference photosRef, String id, ArrayAdapter<Item> itemAdapter) {
        for (int i = 0; i < 6; ++i) {
            // set path for current image
            StorageReference photoRef = photosRef.child(id + "/image" + i + ".jpg");
            Log.i("FETCHING PHOTOS", "fetching image"+i + "for item " + id);
            photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // get the bitmap of the byte array and add it to the item's list
                    Bitmap img_bit = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    addImage(img_bit);
                    Log.i("FETCHING PHOTOS","photo fetched for " + id);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    // Note: there is an error when the image isn't
                    // found, so we leave this blank to avoid printing the message
                }
            }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    // Update the list when finished to load all the pictures
                    itemAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * Populates the item's photo array from firebase WITHOUT updating the listActivity
     * @param photosRef the reference to the photo storage on firebase
     * @param id the id of this item
     */
    public void generatePhotoArray(StorageReference photosRef, String id, OnCompleteListener completeListener) {
        for (int i = 0; i < 6; ++i) {
            // set path for current image
            StorageReference photoRef = photosRef.child(id + "/image" + i + ".jpg");
            Log.i("FETCHING PHOTOS", "fetching image"+i + "for item " + id);
            photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // get the bitmap of the byte array and add it to the item's list
                    Bitmap img_bit = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    addImage(img_bit);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    // Note: there is an error when the image isn't
                    // found, so we leave this blank to avoid printing the message
                }
            }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    // Calls the onCompleteListener on function call
                    // See ViewItemActivity for example
                    completeListener.onComplete(task);
                }
            });
        }
    }

    /**
     * Returns the images arraylist associated with the item.
     * @return arrayList of type Bitmap containing the images
     */
    public ArrayList<Bitmap> getImages() {
        return images;
    }

    /**
     * Returns the size of the images arraylist.
     * @return the number of images stored for the item
     * */
    public int getPhotosSize() {
        return images.size();
    }

    /**
     * Sets the images arraylist to the input list of images
     * @param images arraylist of type Bitmap that holds the images
     * */
    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }

    /**
     * Returns the image at the specified index from the images arraylist.
     * @param index the index of the image to retrieve
     * @return the image at the specified index
     */
    public Bitmap getImage(int index) {
        try {
            Log.i("GETTING PHOTOS", "Getting photo " + index);
            return images.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Adds a new image to the item's image arraylist.
     * @param image the image to be added
     */
    public void addImage(Bitmap image) {
        this.images.add(image);
    }

    /**
     * Getter method to retrieve the date of purchased or acquisition of the Item.
     * @return date
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter method to set the date of purchased or acquisition of the Item.
     * @param date String date to be associated with the date of purchase
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Getter method that retrieves a brief description of the Item.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter method to set the brief description of the Item.
     * @param description String description to be added
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter method to retrieve the make of the Item.
     * @return make
     */
    public String getMake() {
        return make;
    }

    /**
     * Setter method to set the make of the Item.
     * @param make String make to be added
     */
    public void setMake(String make) {
        this.make = make;
    }

    /**
     * Getter method to retrieve the model of the Item.
     * @return model
     */
    public String getModel() {
        return model;
    }

    /**
     * Setter method to set the model of the Item.
     * @param model String model to be added
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Getter method to retrieve the serial number of the Item
     * @return serial_num
     */
    public String getSerial_num() {
        return serial_num;
    }

    /**
     * Setter method to set a serial number of an Item
     * @param serial_num String serial number to be added
     */
    public void setSerial_num(String serial_num) {
        this.serial_num = serial_num;
    }

    /**
     * Getter method to retrieve the estimated value of the Item
     * @return a formatted string representing the estimated value to second decimal
     * place.
     */
    public String getEst_value() {
        if (est_value != null){
            return String.format("%.2f", Double.parseDouble(est_value));
        }

        return est_value;
    }

    /**
     * Setter method to set the Item's estimated value
     * @param est_value String estimated value to be added
     */
    public void setEst_value(String est_value) {
        this.est_value = est_value;
        if (est_value != null){
            est_value_num = Double.parseDouble(est_value);
        }
    }

    /**
     * Getter method to retrieve the Item's comment
     * @return comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter method to set a comment for the Item
     * @param comment String commment to be added
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Method to set tag(s) for the Item
     * @param tags List of tags for the Item
     */
    public void setTags(List<String> tags){
        this.tags = tags;
    }

    /**
     * Returns the list of tags associated with the item
     * @return tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Getter method that retrieves the ID of the Item
     * @return ID
     */
    public long getID() {
        return ID;
    }

    /**
     * Setter method that sets the ID of the Item
     * @param ID 8 byte stored whole number used as an ID for the Item
     */
    public void setID(long ID) {
        this.ID = ID;
    }

    /**
     * Setter method that sets the checked status of the Item.
     * @param sel Boolean value for Item checked status.
     */
    public void setChecked(boolean sel){
        selected = sel;
    }

    /**
     * Getter method that returns a boolean value of the checked status of the Item.
     * @return selected
     */
    public boolean getChecked(){
        return selected;
    }

    public int getTagSize() {
        if(tags == null) {
            return 0;
        } else {
            return tags.size();
        }
    }

}