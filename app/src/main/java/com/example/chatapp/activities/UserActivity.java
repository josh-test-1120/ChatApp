package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.databinding.ActivityUserBinding;

import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the User activity view, which extended the UserListener
 */
public class UserActivity extends AppCompatActivity implements UserListener {
    // These are the private variables
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * This is the override for the onCreate method
     * @param savedInstanceState this is the bundle of saved configuration
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Attach the binding
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        // Bind the preference manager
        preferenceManager = new PreferenceManager(getApplicationContext());
        // Set the content view
        setContentView(binding.getRoot());
        // Set the listeners
        setListeners();
        // Get list of users
        getUsers();
    }

    /**
     * Sets the listeners in this view
     */
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Gets the users to display in the view
     */
    private void getUsers() {
        // Show the progress bar
        loading(true);
        // Initialize the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // Get the users from the database
        database.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    // Remove progress bar
                    loading(false);
                    // Get the current userID
                    String currentUserID = preferenceManager.getString(Constants.KEY_USERID);
                    // If they results are successful
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Initialize a new list of users
                        List<User> users = new ArrayList<>();
                        // Iterate through the results
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            // If the user is the same as the logged in one, skip
                            if (currentUserID.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            // Otherwise, create a user object
                            User user = new User();
                            // Add the information from the database
                            user.firstName = queryDocumentSnapshot.getString(Constants.KEY_FIRSTNAME);
                            user.lastName = queryDocumentSnapshot.getString(Constants.KEY_LASTNAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.userid = queryDocumentSnapshot.getId();
                            // Add the user to the list
                            users.add(user);
                        }
                        // If the users is greater than zero add the adaptor
                        if (users.size() > 0) {
                            UserAdapter usersAdapter = new UserAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        // Otherwise show error message
                        else {
                            showErrorMessage();
                        }
                    }
                    // Otherwise show error message if failure to get data from database
                    else {
                        showErrorMessage();
                    }
                });
    }

    /**
     * This will show the message that no other users are in the database
     */
    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * This will handle the state of the progress bar
     * @param isLoading a boolean that defines the state of the progress bar
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This method implements the UserListener method
     * and handles loading a chat view with them
     * @param user This is the user that was clicked
     */
    @Override
    public void onUserClicked(User user) {
        // Create a new intent for the chat view
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        // Put the user into the intent
        intent.putExtra(Constants.KEY_USER,user);
        // Start the new activity
        startActivity(intent);
        // Free up the resources and close this activity
        finish();
    }
}