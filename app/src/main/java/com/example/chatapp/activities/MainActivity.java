package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;
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

    private void setListeners() {
        binding.imageSignout.setOnClickListener(v -> signOut());

        binding.fabNewChat.setOnClickListener(v -> {
            Log.d("Button Click","New Chat Clicked");
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
        });
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager
                .getString(Constants.KEY_FIRSTNAME + " " + Constants.KEY_LASTNAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        Log.d("Image Info:",preferenceManager.getString(Constants.KEY_IMAGE));
        binding.imageProfile.setImageBitmap(bitmap);

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document((preferenceManager.getString(Constants.KEY_USERID)));

        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(result -> showToast("Token Updated Successfully"))
                .addOnFailureListener(exception -> showToast("Token Update Failed"));
    }

    private void signOut() {
        showToast("Signing out...");

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document((preferenceManager.getString(Constants.KEY_USERID)));

        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(result -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(exception -> {
                    showToast("Unable to sign out");
                });
    }
}