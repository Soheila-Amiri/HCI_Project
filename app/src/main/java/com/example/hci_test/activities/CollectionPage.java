package com.example.hci_test.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hci_test.R;
import com.example.hci_test.adapter.CollectionAdapter;
import com.example.hci_test.model.CollectionManager;

import java.util.Objects;

public class CollectionPage extends AppCompatActivity implements CollectionAdapter.OnCollectionClickListener
{

    RecyclerView recyclerView;
    CollectionAdapter adapter;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_collection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        findViewById(R.id.imageViewBack).setOnClickListener(v -> finish());

        findViewById(R.id.imageViewAddCollection).setOnClickListener(v -> showAddCollectionDialog());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new CollectionAdapter(CollectionManager.getAllCollections(), this);
        recyclerView.setAdapter(adapter);
    }

    private void showAddCollectionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.editTextCollectionName);

        new AlertDialog.Builder(this)
                .setTitle("New Collection")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean created = CollectionManager.createCollection(name);
                        if (created) {
                            adapter.updateData(CollectionManager.getAllCollections());
                            Toast.makeText(this, "Collection added", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Collection name already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onCollectionLongPressed(String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Collection")
                .setMessage("Are you sure you want to delete \"" + name + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    CollectionManager.removeCollection(name);
                    adapter.updateData(CollectionManager.getAllCollections());
                    Toast.makeText(this, "Collection deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }


}
