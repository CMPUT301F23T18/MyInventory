package com.example.myinventoryapp;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import java.util.List;

import io.grpc.Context;

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

    private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    final long ONE_MEGABYTE = 1024 * 1024;

    // photos

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

    @Override
    public int describeContents() {
        return 0;
    }

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
                    // Calls the onCompleteListener on function call
                    // See ViewItemActivity for example
                    completeListener.onComplete(task);
                }
            });
        }
    }

    public ArrayList<Bitmap> getImages() {
        return images;
    }

    public int getPhotosSize() {
        return images.size();
    }

    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }
    public Bitmap getImage(int index) {
        try {
            Log.i("GETTING PHOTOS", "Getting photo " + index);
            return images.get(index);
        } catch (Exception exception) {
            return null;
        }
    }

    public void addImage(Bitmap image) {
        this.images.add(image);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerial_num() {
        return serial_num;
    }

    public void setSerial_num(String serial_num) {
        this.serial_num = serial_num;
    }

    public String getEst_value() {
        if (est_value != null){
            return String.format("%.2f", Double.parseDouble(est_value));
        }

        return est_value;
    }

    public void setEst_value(String est_value) {
        this.est_value = est_value;
        if (est_value != null){
            est_value_num = Double.parseDouble(est_value);
        }
    }

    public double getEst_value_num() {
        return est_value_num;
    }

    public void setEst_value_num(double est_value_num) {
        this.est_value_num = est_value_num;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTags(List<String> tags){
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setChecked(boolean sel){
        selected = sel;
    }

    public boolean getChecked(){
        return selected;
    }

}