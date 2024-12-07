package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

import java.util.List;

/**
 * This is the User Adaptor for use with Recycle Views
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    // These are the private variables
    private List<User> users;
    private final UserListener userListener;

    /**
     * This is the default constructor
     * @param users this is the list of users
     * @param userListener this is the user Listener instance
     */
    public UserAdapter(List<User> users, UserListener userListener)
    {
        this.users = users;
        this.userListener = userListener;
    }

    /**
     * This is the override for the onCreateViewHolder method
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return an instance of the UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflat the binding
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding
                .inflate(LayoutInflater.from(parent.getContext()),parent,false);
        // Return the view holder with the binding
        return new UserViewHolder(itemContainerUserBinding);
    }

    /**
     * This is the override for the onBindViewHolder method
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    /**
     * This is the override for the getItemCount method
     * @return the size of the users list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * This is the UserViewHolder for Recycle Views
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        // This is the view binding
        ItemContainerUserBinding binding;

        /**
         * This is the default constructor
         * @param itemContainerUserBinding
         */
        public UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            // Attach the binding
            this.binding = itemContainerUserBinding;
        }

        /**
         * Set the user data into the view
         * @param user This is the user to display
         */
        private void setUserData(User user) {
            // Set the username
            binding.textName.setText(user.firstName + " " + user.lastName);
            // Set the email
            binding.textEmail.setText(user.email);
            // Set the user profile image
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            // Set the OnClickListener to the entire container view
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    /**
     * This will return a Bitmap object from a BASE64 encoded string
     * @param encodedImage the BASE64 encoded string of the image
     * @return a Bitmap instance form the encoded string
     */
    private Bitmap getUserImage(String encodedImage) {
        // Get the bytes from the string decode
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        // Create an image from the bytes array
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
