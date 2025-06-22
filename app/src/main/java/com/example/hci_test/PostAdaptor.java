package com.example.hci_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // ✅ Import corect
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hci_test.activities.MainActivity;
import com.example.hci_test.adapter.CollectionChoiceAdapter;
import com.example.hci_test.model.Collection;
import com.example.hci_test.model.CollectionManager;
import com.example.hci_test.model.Post;

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

        holder.addToCollectionButton.setOnClickListener(v -> showAddToCollectionDialog(post));
    }
    private void showAddToCollectionDialog(Post post) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_collection, null);
        ListView listView = dialogView.findViewById(R.id.listViewCollections);
        TextView newCollectionBtn = dialogView.findViewById(R.id.textViewNewCollection);

        List<Collection> allCollections = CollectionManager.getAllCollections();
        CollectionChoiceAdapter adapter = new CollectionChoiceAdapter(context, allCollections);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    for (String collectionName : adapter.getSelectedNames()) {
                        boolean added = CollectionManager.addPostToCollection(collectionName, post);
                        // optionally: show Toast for each addition
                    }
                    Toast.makeText(context, "Saved to selected collections", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        newCollectionBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (context instanceof MainActivity) {
                ((MainActivity) context).openNewCollectionDialog(post);
            }
        });
    }
    private void showCreateCollectionDialog(Post post) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.editTextCollectionName);

        new AlertDialog.Builder(context)
                .setTitle("New Collection")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean created = CollectionManager.createCollection(name);
                        if (created) {
                            CollectionManager.addPostToCollection(name, post);
                            Toast.makeText(context, "Collection created and post added", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Collection already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }
}
