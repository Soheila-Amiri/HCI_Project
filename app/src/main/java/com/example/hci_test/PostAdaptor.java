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
import java.util.Random;

public class PostAdaptor extends RecyclerView.Adapter<PostViewHolder> {
    List<Post> postList;
    Context context;
    private boolean collectionMode;
    

    public PostAdaptor(List<Post> postList, Context context, boolean collectionMode) {
        this.postList = postList;
        this.context = context;
        this.collectionMode = collectionMode;
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
        holder.textViewLikes.setText(post.getLikes());
        holder.textViewDescription.setText(post.getDescription());

        Glide.with(context).load(post.getUserProfile())
                .circleCrop()
                .into(holder.imageViewUser);
        holder.addToCollectionButton.setOnClickListener(v -> showAddToCollectionDialog(post));

        if (collectionMode) {
            holder.addToCollectionButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                Post postToRemove = postList.get(adapterPosition);

                for (Collection collection : CollectionManager.getAllCollections()) {
                    if (collection.getPosts().contains(postToRemove)) {
                        collection.getPosts().remove(postToRemove);
                        break;
                    }
                }

                postList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
            });
        } else {
            holder.addToCollectionButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
        }

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
                .setPositiveButton("Save", null) // We'll override this after .show()
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean anySelected = false;
            boolean anySaved = false;
            boolean anyDuplicate = false;

            for (String collectionName : adapter.getSelectedNames()) {
                anySelected = true;
                boolean added = CollectionManager.addPostToCollection(collectionName, post);
                if (added) {
                    anySaved = true;
                } else {
                    anyDuplicate = true;
                }
            }

            if (!anySelected) {
                Toast.makeText(context, "No collections selected", Toast.LENGTH_SHORT).show();
            } else {
                if (anySaved) {
                    Toast.makeText(context, "Post saved successfully", Toast.LENGTH_SHORT).show();
                }
                if (anyDuplicate) {
                    Toast.makeText(context, "Post already exists in one or more collections", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        newCollectionBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (context instanceof MainActivity) {
                ((MainActivity) context).openNewCollectionDialog(post);
            }
        });
    }


    //    private void showAddToCollectionDialog(Post post) {
//        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_collection, null);
//        ListView listView = dialogView.findViewById(R.id.listViewCollections);
//        TextView newCollectionBtn = dialogView.findViewById(R.id.textViewNewCollection);
//
//        List<Collection> allCollections = CollectionManager.getAllCollections();
//        CollectionChoiceAdapter adapter = new CollectionChoiceAdapter(context, allCollections);
//        listView.setAdapter(adapter);
//
//        AlertDialog dialog = new AlertDialog.Builder(context)
//                .setView(dialogView)
//                .setPositiveButton("Save", (d, which) -> {
//                    for (String collectionName : adapter.getSelectedNames()) {
//                        boolean added = CollectionManager.addPostToCollection(collectionName, post);
//                        if (added) {
//                            Toast.makeText(context, "Saved to selected collections", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(context, "Post already exists in selected collection", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .create();
//
//        dialog.show();
//
//        newCollectionBtn.setOnClickListener(v -> {
//            dialog.dismiss();
//            if (context instanceof MainActivity) {
//                ((MainActivity) context).openNewCollectionDialog(post);
//            }
//        });
//    }
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
