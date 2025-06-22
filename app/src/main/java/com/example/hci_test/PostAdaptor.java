package com.example.hci_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hci_test.activities.MainActivity;
import com.example.hci_test.model.Post;

import java.util.List;
import java.util.Random;

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

        Random random = new Random();

        Post post = postList.get(position);
        Glide.with(context).load(post.getUrl())
                .placeholder(R.drawable.img)
                .centerCrop()
                .into(holder.imageView);
        holder.textViewUsername.setText(post.getPhotographer());
        holder.textViewLikes.setText("" + random.nextInt(100) + 1);
        holder.textViewDescription.setText(post.getDescription());

        int profileImageIndex = random.nextInt(10) + 1;
        String profileImagePath = "file:///android_asset/Profile_pictures/" + profileImageIndex + ".jpg";
        Glide.with(context).load(profileImagePath)
                .circleCrop()
                .into(holder.imageViewUser);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
