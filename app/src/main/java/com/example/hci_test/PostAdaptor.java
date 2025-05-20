package com.example.hci_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdaptor extends RecyclerView.Adapter<PostViewHolder> {

    List<Post> postList;
    Context context;

    public PostAdaptor(List<Post> postList, MainActivity context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        Glide.with(context).load(post.getUrl())
                .placeholder(R.drawable.img)
                .centerCrop()
                .into(holder.imageView);
        holder.textViewUsername.setText(post.getPhotographer());
        holder.textViewLikes.setText("3");
        holder.textViewDescription.setText(post.getDescription());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
