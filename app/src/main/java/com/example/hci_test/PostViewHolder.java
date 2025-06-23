package com.example.hci_test;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    ImageView imageViewUser;
    TextView textViewUsername;
    ImageView imageViewLikes;
    TextView textViewLikes;
    TextView textViewDescription;
    ImageView addToCollectionButton;
    ImageView deleteButton;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
        imageViewUser = itemView.findViewById(R.id.imageViewUser);
        textViewUsername = itemView.findViewById(R.id.textViewUsername);
        imageViewLikes = itemView.findViewById(R.id.imageViewLikes);
        textViewLikes = itemView.findViewById(R.id.textViewLikes);
        textViewDescription = itemView.findViewById(R.id.textViewDescription);
        addToCollectionButton = itemView.findViewById(R.id.addToCollectionButton);
        deleteButton = itemView.findViewById(R.id.deleteButton);
    }
}
