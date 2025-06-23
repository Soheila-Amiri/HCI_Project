package com.example.hci_test.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hci_test.R;
import com.example.hci_test.model.Collection;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<Collection> collections;
    private final OnCollectionClickListener listener;

    public interface OnCollectionClickListener {
        void onCollectionLongPressed(String name);
    }


    public CollectionAdapter(List<Collection> collections, OnCollectionClickListener listener) {
        this.collections = collections;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Collection> newCollections) {
        this.collections = newCollections;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_collection, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);
        holder.collectionName.setText(collection.getName());
        holder.collectionCount.setText(collection.getPosts().size() + " posts");

        String thumbnailUrl = collection.getThumbnailUrl();

        if ("placeholder".equals(thumbnailUrl)) {
            holder.collectionImage.setImageResource(R.drawable.placeholder_collection);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder_collection)
                    .into(holder.collectionImage);
        }

        holder.itemView.setOnLongClickListener(v -> {
            listener.onCollectionLongPressed(collection.getName());
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.example.hci_test.activities.SingleCollectionActivity.class);
            intent.putExtra("collection_name", collection.getName());
            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return collections.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView collectionName, collectionCount;
        ImageView collectionImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            collectionName = itemView.findViewById(R.id.textViewCollectionName);
            collectionCount = itemView.findViewById(R.id.textViewCollectionCount);
            collectionImage = itemView.findViewById(R.id.imageViewCollection);
        }
    }
}
