package com.example.myinventoryapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import java.util.List;

//TODO: add photos list

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

    public Item(String date, String description, String make, String model, String serial_num, String est_value) {
        // NOTE: comment, tags and photos are NOT added on item creation
        this.date = date;
        this.description = description;
        this.make = make;
        this.model = model;
        this.serial_num = serial_num;
        this.est_value = est_value;

        est_value_num = Double.parseDouble(est_value);
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

    public void add_tag(String tag){
        if (!tags.contains(tag)){
            tags.add(tag);
        }
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