package com.example.hci_test.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hci_test.PostAdaptor;
import com.example.hci_test.R;
import com.example.hci_test.PostAdaptor;
import com.example.hci_test.model.Collection;
import com.example.hci_test.model.CollectionManager;
import com.example.hci_test.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleCollectionActivity extends AppCompatActivity {
    public static final String EXTRA_COLLECTION_NAME = "collection_name";

    private Collection collection;
    private PostAdaptor postAdapter;
    private TextView textViewTitle;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_collection);

        String collectionName = getIntent().getStringExtra(EXTRA_COLLECTION_NAME);
        collection = CollectionManager.getCollectionByName(collectionName);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(v -> finish());

        textViewTitle = findViewById(R.id.textViewCollection);
        textViewTitle.setText(collection.getName());

        ImageView settings = findViewById(R.id.imageViewCollectionSettings);
        settings.setOnClickListener(v -> showPopupMenu());

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCollection);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<Post> reversedPosts = new ArrayList<>(collection.getPosts());
        Collections.reverse(reversedPosts);

        postAdapter = new PostAdaptor(reversedPosts, this,true);
        recyclerView.setAdapter(postAdapter);
    }

    private void showPopupMenu() {
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.buttom_sheet_collection_menu, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(bottomSheetView)
                .create();

        bottomSheetView.findViewById(R.id.optionEdit).setOnClickListener(v -> {
            dialog.dismiss();
            showEditCollectionDialog();
        });

        bottomSheetView.findViewById(R.id.optionDelete).setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Delete Collection")
                    .setMessage("Are you sure you want to delete this collection?")
                    .setPositiveButton("Yes", (d, w) -> {
                        CollectionManager.removeCollection(collection.getName());
                        Toast.makeText(this, "Collection deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        bottomSheetView.findViewById(R.id.optionCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void showEditCollectionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.editTextNewName);
        editText.setText(collection.getName());

        new AlertDialog.Builder(this)
                .setTitle("Edit Collection Name")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(collection.getName())) {
                        boolean renamed = CollectionManager.renameCollection(collection.getName(), newName);
                        if (renamed) {
                            collection = CollectionManager.getCollectionByName(newName);
                            textViewTitle.setText(newName);
                            Toast.makeText(this, "Collection renamed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Name already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
