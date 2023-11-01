package com.example.myinventoryapp;

import android.app.Application;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;




// set
//      ((Global) this.getApplication()).setUSER_PATH("Users/<userID>/Items");

// get
//      CollectionReference fb_items = ((Global) this.getApplication()).getFbCollRef();






public class Global extends Application {

    /**
     * creates a default user path and doc reference
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // NOTE: the below will get overwrote when the user logs in
        USER_PATH = "Users/test_user/Items";
        fbCollRef = FirebaseFirestore.getInstance().collection(USER_PATH);
    }

    private String USER_PATH;
    private CollectionReference fbCollRef; // firebase collection reference

    /**
     * returns the user path for the current user, set when they login
     * @return String
     */
    public String getUSER_PATH() {
        return USER_PATH;
    }

    /**
     *
     * @param USER_PATH the string that tells firebase where to store information
     *                  for the current user.
     *                  Should be set everytime the user logs in to the app
     */
    public void setUSER_PATH(String USER_PATH) {
        this.USER_PATH = USER_PATH;
        this.fbCollRef = FirebaseFirestore.getInstance().collection(USER_PATH);
    }

    /**
     * gets the collection of items from firebase for a specific user, specified
     * by USER_PATH
     * @return CollectionReference
     */
    public CollectionReference getFbCollRef(){
        return fbCollRef;
    }

    /**
     * creates a document of firebase for storing the information in an item
     * The make of the item acts as a name for the item
     * @param make
     * @return DocumentReference
     */
    public DocumentReference makeDocumentRef(String make, String model) {
        String path = USER_PATH + "/" + make + model;
        return fbCollRef.document(path);
    }

    /**
     * an alternative method of making a document reference where the item is given
     * and the make and model are taken from there
     * + faster to code
     * @param item an item to get the firebase document for
     * @return DocumentReference
     */
    public DocumentReference makeDocumentRef(Item item) {
        String make = item.getMake();
        String model = item.getModel();

        return makeDocumentRef(make,model);
    }
}
