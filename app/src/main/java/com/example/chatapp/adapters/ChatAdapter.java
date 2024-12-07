package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.chatapp.databinding.ItemContainerSentMessageBinding;
import com.example.chatapp.models.ChatMessage;

import java.util.List;

/**
 * This is the Chat Adaptor used for Recycle Views
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // These are the private variables
    private Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String sendID;
    // These are the view type constants
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    /**
     * This is the default constructor
     * @param chatMessages these are the chat messages
     * @param receiverProfileImage this is the receiver profile image bitmap
     * @param sendID this is the senders user ID
     */
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String sendID) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.sendID = sendID;
    }

    /**
     * This is the override for the onCreateViewHolder method
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // If the view type is sender
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(ItemContainerSentMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        // If the view type is receiver
        else {
            return new ReceiverMessageViewHolder(ItemContainerReceivedMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    /**
     * This is the override for the onBindViewHolder method
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // If the view type is sender
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        }
        // If the view type is receiver
        else {
            ((ReceiverMessageViewHolder)holder)
                    .setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    /**
     * This is the override for the getItemViewType method
     * @param position position to query
     * @return the integer value of the view type
     */
    @Override
    public int getItemViewType(int position) {
        // If the sender ID equal the logged in user it is send type
        if (chatMessages.get(position).senderID.equals(sendID)) {
            return VIEW_TYPE_SENT;
        }
        // Otherwise it is receiver type
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    /**
     * This is the override for the getItemCount method
     * @return this size of the chat messages list
     */
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    /**
     * This is the Send Message View Holder
     */
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        // This is the view binding
        private final ItemContainerSentMessageBinding binding;

        /**
         * This is the default constructor
         * @param itemContainerSentMessageBinding this is the view binding
         */
        public SentMessageViewHolder(@NonNull ItemContainerSentMessageBinding
                                             itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            // Attach the binding
            binding = itemContainerSentMessageBinding;
        }

        /**
         * This sets the data for the chat message
         * @param chatMessage this chat message to display
         */
        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

        }
    }

    /**
     * This is the Received Message View Holder
     */
    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        // This is the view binding
        private final ItemContainerReceivedMessageBinding binding;

        /**
         * This is the default constructor
         * @param itemContainerReceivedMessageBinding this is the view binding
         */
        public ReceiverMessageViewHolder(@NonNull ItemContainerReceivedMessageBinding
                                                 itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            // Attach the binding
            binding = itemContainerReceivedMessageBinding;
        }

        /**
         * This sets the data for the chat message
         * @param chatMessage this chat message to display
         * @param receiverProfileImage this is the receiver profile image
         */
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);

        }
    }
}
