package com.example.myinventoryapp;

import android.app.Application;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;



// set
//      ((Global) getApplication()).setUSER_PATH(username);

// get
//      CollectionReference fb_items = ((Global) getApplication()).getFbCollRef();



public class Global extends Application {
    private String USER_PATH;
    private CollectionReference fbItemsRef; // firebase collection reference

    /**
     * creates a default user path and doc reference
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // NOTE: the below will get overwrote when the user logs in
        USER_PATH = "Users/test_user/Items";
        fbItemsRef = FirebaseFirestore.getInstance().collection(USER_PATH);
    }

    /**
     * returns the user path for the current user, set when they login
     * @return String
     */
    public String getUSER_PATH() {
        return USER_PATH;
    }

    /**
     * sets the USER_PATH so firebase directs to the correct list of items
     * @param username the string that tells firebase who the user is
     *                  Should be set everytime the user logs in to the app
     */
    public void setUSER_PATH(String username) {
        this.USER_PATH = "Users/" + username + "/Items";
        this.fbItemsRef = FirebaseFirestore.getInstance().collection(USER_PATH);
    }

    /**
     * gets the collection of items from firebase for a specific user, specified
     * by USER_PATH
     * @return CollectionReference
     */
    public CollectionReference getFbItemsRef(){
        return fbItemsRef;
    }

    /**
     * creates a document of firebase for storing the information in an item
     * returns the document if it already exists
     * @param ID the name of the item on firebase, generated using UTC
     * @return DocumentReference
     */
    public DocumentReference DocumentRef(long ID) {
        String str_id = Long.toString(ID);
        String path = "/" + str_id;
        return fbItemsRef.document(path);
    }

    /**
     * an alternative method of making a document reference where the item is given
     * and the Id is taken from there
     * + faster to code
     * @param item an item to get the firebase document for
     * @return DocumentReference
     */
    public DocumentReference DocumentRef(Item item) {
        long ID = item.getID();

        return DocumentRef(ID);
    }
}
