package com.example.myinventoryapp;

import android.app.Application;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Class for managing connections to firebase
 */
// Example of call for getting CollectionReference for items: CollectionReference fb_items = ((Global) getApplication()).getFbItemsRef();
// Example of call for setting username: ((Global) getApplication()).setUSER_PATH(username);
public class Global extends Application {
    private String USER_PATH;
    private CollectionReference fbItemsRef; // firebase Items reference
    private CollectionReference fbTagsRef; // firebase Tags reference
    private StorageReference photoStorageRef; // firebase storage reference -> for photos

    /**
     * creates a default user path and doc reference
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // NOTE: the below will get overwritten when the user logs in
        setUSER_PATH("test_user");
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
        this.USER_PATH = "Users/" + username;
        this.fbItemsRef = FirebaseFirestore.getInstance().collection(USER_PATH + "/Items");
        this.fbTagsRef = FirebaseFirestore.getInstance().collection(USER_PATH + "/Tags");
        this.photoStorageRef = FirebaseStorage.getInstance().getReference().child(USER_PATH + "/Photos");
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
     * gets the collection of tags from firebase for a specific user
     * @return CollectionReference
     */
    public CollectionReference getFBTagsRef() {
        return fbTagsRef;
    }

    /**
     * creates a document of firebase for storing the information in an item
     * returns the document if it already exists
     * @param ID the name of the item on firebase, generated using UTC
     * @return DocumentReference
     */
    public DocumentReference DocumentRef(long ID) {
        String str_id = Long.toString(ID);
        return fbItemsRef.document(str_id);
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


    /**
     * gets the storage reference for photos
     * @return StorageReference
     */
    public StorageReference getPhotoStorageRef() {
        return photoStorageRef;
    }

    /**
     * Uploads a photo for a specific item to firebase, using the id, photo_id and the file
     * @param id id of the item to assign the photo to
     * @param photo the actual photo to be assigned
     * @param name the name for the photo, generated when it was captured/selected
     */
    public void setPhoto(long id, Bitmap photo, String name) {
        String str_id = String.valueOf(id);
        StorageReference photoRef = photoStorageRef.child(str_id + "/" + name + ".jpg");

        // compress photo and apply to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Send to firestore
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });
    }

    public DocumentReference getBarcodeItem(String value) {
        StringBuilder prefix_bld = new StringBuilder();
        StringBuilder item_ref_bld = new StringBuilder();
        for (int i = 0; i < value.length() - 1; i++) {
            char c = value.charAt(i);
            if (i < 6) {
                // First six digits are the Prefix
                prefix_bld.append(c);
            } else {
                item_ref_bld.append(c);
            }
        }
        String brand = prefix_bld.toString();
        String item = item_ref_bld.toString();
        return FirebaseFirestore.getInstance().document("Barcodes/Brands/" + brand + "/" + item);
    }
}