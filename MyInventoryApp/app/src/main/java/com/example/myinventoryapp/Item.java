package com.example.myinventoryapp;

public class Item {
    private String date;
    private String description;
    private String make;
    private String model;
    private String serial_num;
    private double est_value;
    private String est_value_str;
    private String comment;

    public Item(String date, String description, String make, String model, String serial_num, double est_value, String comment) {
        this.date = date;
        this.description = description;
        this.make = make;
        this.model = model;
        this.serial_num = serial_num;
        this.est_value = est_value;
        this.comment = comment;

        est_value_str = String.valueOf(est_value);
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

    public double getEst_value() {
        return est_value;
    }

    public void setEst_value(double est_value) {
        this.est_value = est_value;
    }

    public String getEst_value_str() {
        return est_value_str;
    }

    public void setEst_value_str(String est_value_str) {
        this.est_value_str = est_value_str;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
