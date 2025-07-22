package com.example.hci_test;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.app.ActivityCompat.finishAffinity;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hci_test.activities.CollectionPage;
import com.example.hci_test.activities.MainActivity;
import com.example.hci_test.activities.SingleCollectionActivity;
import com.example.hci_test.adapter.CollectionChoiceAdapter;
import com.example.hci_test.model.Collection;
import com.example.hci_test.model.CollectionManager;
import com.example.hci_test.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class PostAdaptor extends RecyclerView.Adapter<PostViewHolder> {
    List<Post> postList;
    Context context;
    private boolean collectionMode;

    private CollectionChoiceAdapter adapter;
    private EditText editTextSearch;

    List<Collection> searchedCollections;
    List<Collection> allCollections;
    List<Post> allPostList;

    private final OnPostDeletedListener onPostDeletedListener;

    public interface OnPostDeletedListener {
        void onPostDeleted(Post post);
    }

    public PostAdaptor(List<Post> postList, Context context, boolean collectionMode, OnPostDeletedListener listener) {
        this.postList = new ArrayList<>(postList);
        this.allPostList = new ArrayList<>(postList);
        this.context = context;
        this.collectionMode = collectionMode;
        this.onPostDeletedListener = listener;
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
                        CollectionManager.persistCollections();
                        break;
                    }
                }

                postList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);

                // Notify deletion to update other lists (like allPosts)
                if (onPostDeletedListener != null) {
                    onPostDeletedListener.onPostDeleted(postToRemove);
                }
            });
        } else {
            holder.addToCollectionButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).startVoiceInputForPost(post);
                }
                return true;
            }
        });

        holder.imageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

    }

    private void showAddToCollectionDialog(Post post) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_collection, null);
        ListView listView = dialogView.findViewById(R.id.listViewCollections);
        TextView newCollectionBtn = dialogView.findViewById(R.id.textViewNewCollection);

        //List<Collection> allCollections = CollectionManager.getAllCollections();
        searchedCollections = CollectionManager.getAllCollections();
        allCollections = CollectionManager.getAllCollections();
        adapter = new CollectionChoiceAdapter(context, allCollections, post);
        listView.setAdapter(adapter);

        editTextSearch = dialogView.findViewById(R.id.editTextSearchChoice);
        ImageView imageViewMicChoice = dialogView.findViewById(R.id.imageViewMicCh);
        imageViewMicChoice.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).startVoiceInputForSearch(result -> {
                    editTextSearch.setText(result);
                });
            } else if (context instanceof CollectionPage) {
                ((CollectionPage) context).startVoiceInputForSearch(result -> {
                    editTextSearch.setText(result);
                });
            }
        });


        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByCollectionName(editTextSearch.getText().toString());
                adapter = new CollectionChoiceAdapter(context, searchedCollections, post);
                listView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Save", null) // We'll override this after .show()
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Set<String> selectedNames = adapter.getSelectedNames();

            boolean anyChanges = false;

            for (Collection collection : allCollections) {
                String name = collection.getName();
                boolean contains = collection.getPosts().contains(post);
                boolean shouldContain = selectedNames.contains(name);

                if (shouldContain && !contains) {
                    collection.getPosts().add(post);
                    anyChanges = true;
                } else if (!shouldContain && contains) {
                    collection.getPosts().remove(post);
                    anyChanges = true;
                }
            }

            if (anyChanges) {
                CollectionManager.persistCollections();
                boolean postIsGone = true;
                for (Collection collection : allCollections) {
                    if (collection.getPosts().contains(post)) {
                        postIsGone = false;
                        break;
                    }
                }

                if (postIsGone && onPostDeletedListener != null) {
                    onPostDeletedListener.onPostDeleted(post);
                }

                Toast.makeText(context, "Changes saved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(context, "No changes made", Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void filter(String query) {
        postList.clear();
        if (query.isEmpty()) {
            postList.addAll(allPostList);
        } else {
            for (Post post : allPostList) {
                if (post.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    postList.add(post);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<Post> newPosts) {
        this.postList = new ArrayList<>(newPosts);
        this.allPostList = new ArrayList<>(newPosts);
        notifyDataSetChanged();
    }

    public void filterByCollectionName(String name) {
        searchedCollections.clear();
        if (name.isEmpty()) {
            searchedCollections.addAll(allCollections);
        } else {
            for (Collection collection : allCollections) {
                if (collection.getName().contains(name)) {
                    searchedCollections.add(collection);
                }
            }
        }
        notifyDataSetChanged();
    }
}
