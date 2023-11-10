package com.example.myinventoryapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import java.util.List;

//TODO: add photos list

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
     * This constructs an Item object
     * @param date String of the Item's date of purchased or acquisition
     * @param description String of the appropriate description of an item
     * @param make String of the Item's make
     * @param model String of the Item's model
     * @param serial_num String of the Item's serial number
     * @param est_value String of the Item's estimated value
     */
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
     * Method to add tag(s) if not in the current list of tags.
     * @param tag String tag to be added to be associated with the Item
     */
    public void add_tag(String tag){
        if (!tags.contains(tag)){
            tags.add(tag);
        }
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

}