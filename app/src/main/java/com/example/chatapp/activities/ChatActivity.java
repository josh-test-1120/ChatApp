package com.example.chatapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * This is the Chat Activity class for managing the chat views
 */
public class ChatActivity extends AppCompatActivity {
    // These are the private variables
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    /**
     * This is the override for the onCreate method
     * @param savedInstanceState this is a bundle with saved information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Attach the binding
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        // Set the content View
        setContentView(binding.getRoot());
        // Process methods to load receiver details
        loadReceiverDetails();
        // Set the listeners for events
        setListeners();
        // Initialize the variables
        init();
        // Listen for messages and add them to the view
        listenMessage();
    }

    /**
     * This method will initial the variables
     */
    private void init() {
        // Wire up the preference manager
        preferenceManager = new PreferenceManager(getApplicationContext());
        // Initialize the chat messages
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USERID));
        // Set the recycler view adaptor
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    /**
     * This is the send Message method that will send a new message
     */
    private void sendMessage() {
        // New object for the Firestore database
        HashMap<String, Object> message = new HashMap<>();
        // Put the information into the hash
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USERID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.userid);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        // Add the new has to the database
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        // Clear the input message set
        binding.inputMessage.setText(null);
    }

    /**
     * This is the listenMessage function to show messages
     */
    private void listenMessage() {
        // These are the messages that the logged in user has sent
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USERID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.userid)
                .addSnapshotListener(eventListener);
        // These are the messages that are sent to the logged in user
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.userid)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USERID))
                .addSnapshotListener(eventListener);
    }

    /**
     * This is the EventListener for the database changes
     */
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        // Exit if there is an error
        if (error != null) {
            return;
        }
        // Otherwise if the value is not null
        else if (value != null) {
            // Set the count of the current List size
            int count = chatMessages.size();
            // Iterate through the changed documents
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                // If the change type was ADDED
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    // Empty chat message
                    ChatMessage chatMessage = new ChatMessage();
                    // Add the information to the instance
                    chatMessage.senderID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime =
                            getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    // Add the instance to the List
                    chatMessages.add(chatMessage);
                }
            }
            // Sort the list with a custom compareTo
            Collections.sort(chatMessages, (first, second) -> first.dateObject.compareTo(second.dateObject));
            // If the messages are empty
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            }
            // Otherwise notify of the changes and scroll to end
            else {
                chatAdapter.notifyItemRangeChanged(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            // Ensure the recycle view is visible
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        // Remove the progress bar
        binding.progressBar.setVisibility(View.GONE);
    });

    /**
     * This will get the Bitmap from an encoded string
     * @param encodedImage this is the BASE64 string encoded image
     * @return a Bitmap object to represent the image
     */
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

    /**
     * Loads the Receiver Details
     */
    private void loadReceiverDetails() {
        // Get the user from the Intent and serialize it
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        // Set the user name in the view
        binding.textName.setText(receiverUser.firstName + " " + receiverUser.lastName);

    }

    /**
     * Sets the listeners in the view
     */
    private void setListeners() {
        // Sets the click listener for the back button
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        // Sets the click listener for the send button
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    /**
     * This is the method that creates a consistent String format
     * for date and time timestamps
     * @param date this is the date object to derive a string from
     * @return a string representation of the date and time
     */
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy - hh:mm a",
                Locale.getDefault()).format(date);
    }
}