package com.example.myinventoryapp;

import android.content.Context;
import android.widget.Toast;

import java.time.Month;
import java.util.Calendar;

// I gave field validation its own class because it needs to work for editing items as well
// and I didn't want to retype everything. In other words, efficiency is key
// - Riley

public class FieldValidator {
    /**
     * This function checks the validity of date, whether its too large or too small
     * @param date the date entered by the user
     * @return boolean
     */
    public static boolean checkDate(String date, Context context) {
        /*
           the xml file ensures only numbers and "-" can be entered
           as well as length
           so we only need to check the actual date to ensure it is valid
         */
        if (date.length() == 0) {
            Toast.makeText(context,"date is required to proceed",Toast.LENGTH_SHORT).show();
            return false;
        }
        int current_year = Calendar.getInstance().get(Calendar.YEAR);
        String[] split;
        split = date.split("-"); //split = [year,month,day]

        int year = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[2]);

        Month month1 = Month.of(month);
        boolean leap_year = (year % 4) == 0;
        int num_days = month1.length(leap_year);

        //check year, month and day. using elseif so toast doesn't get overwritten if multiple errors
        if (year > current_year || year < 1900) {
            // month started is too far in the future or past
            Toast.makeText(context,"Acquired date: year is incorrect", Toast.LENGTH_SHORT).show();
            return false;
        } else if (month <= 0 || month > 12) {
            // month is too big for a calendar or its 0
            Toast.makeText(context,"Acquired date: Month is incorrect", Toast.LENGTH_SHORT).show();
            return false;
        } else if (day <= 0) {
            // day is too small
            Toast.makeText(context,"Acquired date: Day is too small", Toast.LENGTH_SHORT).show();
            return false;
        } else if (day > num_days) {
            // day is too large for the given month
            Toast.makeText(context,"Acquired date: Day is to0 large for given month", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    /**
     * takes a field and checks the size of it, if it doesn't have content
     * then return false, else true
     * @param field make or model or price
     * @return boolean
     */
    public static boolean checkFieldSize(String field) {
        return field.length() > 0;
    }

}