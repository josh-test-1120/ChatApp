package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

/**
 * This is the Main Activity view that implements PosterListener
 */
public class MainActivity extends AppCompatActivity {
    // Private variables
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * This is an override of the onCreate method
     * @param savedInstanceState the current saved state of the instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager((getApplicationContext()));

        loadUserDetails();
        setListeners();
        getToken();

    }

    /**
     * This is the method that sets the click listeners
     */
    private void setListeners() {
        // Set the listener for the logout image
        binding.imageSignout.setOnClickListener(v -> signOut());
        // Set the listener for the New Chat FAB
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
        });
    }

    /**
     * This method will load the user details into the view header
     */
    private void loadUserDetails() {
        // Set the username
        binding.textName.setText(preferenceManager
                .getString(Constants.KEY_FIRSTNAME + " " + Constants.KEY_LASTNAME));
        // Load the user image profile
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        // Bind the image to the view
        binding.imageProfile.setImageBitmap(bitmap);

    }

    /**
     * This will show a toast message to the user
     * @param message this is the message to show the user
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    /**
     * This will get the FCM token for the user from the database
     */
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    /**
     * Update the user FCM token
     * @param token this is the new token for the user
     */
    private void updateToken(String token) {
        // Initialize the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // Get the database document for the user
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document((preferenceManager.getString(Constants.KEY_USERID)));
        // Update the document with the new token
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                // Toast handles based on result
                .addOnSuccessListener(result -> showToast("Token Updated Successfully"))
                .addOnFailureListener(exception -> showToast("Token Update Failed"));
    }

    /**
     * This method will sign out the user
     * and remove the FCM token
     */
    private void signOut() {
        // Let the user know we are logging them out
        showToast("Signing out...");
        // Intialize the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // Get the document for the user
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document((preferenceManager.getString(Constants.KEY_USERID)));
        // Create a new hash map for updates
        HashMap<String,Object> updates = new HashMap<>();
        // Put the token into the hashmap
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        // Update the document
        documentReference.update(updates)
                // Result handlers
                .addOnSuccessListener(result -> {
                    // Clear the preference manager
                    preferenceManager.clear();
                    // Redirect to the SignIn Activity
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    // Finish and close out the resources for this view
                    finish();
                })
                .addOnFailureListener(exception -> {
                    showToast("Unable to sign out");
                });
    }
}