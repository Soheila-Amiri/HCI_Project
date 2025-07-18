package com.example.hci_test.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hci_test.PostAdaptor;
import com.example.hci_test.R;
import com.example.hci_test.adapter.CollectionAdapter;
import com.example.hci_test.model.Collection;
import com.example.hci_test.model.CollectionManager;
import com.example.hci_test.model.Post;
import com.google.android.gms.common.server.converter.StringToIntConverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class CollectionPage extends AppCompatActivity implements CollectionAdapter.OnCollectionClickListener {

    private RecyclerView recyclerView;
    private CollectionAdapter adapter;
    private TextView textViewNoCollections;

    private EditText editTextSearchCo;
    private ImageView imageViewMicCo;
    private CheckBox checkBoxPosts;
    private TextView textViewNoR;

    private RecyclerView recyclerViewPosts;
    private PostAdaptor postAdaptor;
    List<Post> allPosts;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_collection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        checkBoxPosts = findViewById(R.id.checkBoxPosts);
        imageViewMicCo = findViewById(R.id.imageViewMicCo);
        editTextSearchCo = findViewById(R.id.editTextSearchCo);
        textViewNoR = findViewById(R.id.textViewNoPosts);

        imageViewMicCo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearchCo.setText("");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Listening...");
                activityResultLauncher.launch(intent);
            }
        });

        findViewById(R.id.imageViewBack).setOnClickListener(v -> finish());

        findViewById(R.id.imageViewAddCollection).setOnClickListener(v -> showAddCollectionDialog());

        textViewNoCollections = findViewById(R.id.textViewNoCollections);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new CollectionAdapter(CollectionManager.getAllCollections(), this);
        recyclerView.setAdapter(adapter);

        List<Collection> allCollections = CollectionManager.getAllCollections();
        Set<Post> uniquePostsSet = new HashSet<>();
        for (Collection collection : allCollections) {
            uniquePostsSet.addAll(collection.getPosts());
        }
        allPosts = new ArrayList<>(uniquePostsSet);

        checkBoxPosts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                recyclerViewPosts.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                refreshAllPostsView();

                postAdaptor = new PostAdaptor(allPosts, this, false, post -> refreshAllPostsView());
                recyclerViewPosts.setAdapter(postAdaptor);
                postAdaptor.filter(editTextSearchCo.getText().toString());
            }
            else{
                recyclerViewPosts.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.filter(editTextSearchCo.getText().toString());
            }
        });
        editTextSearchCo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkBoxPosts.isChecked()) {
                    postAdaptor.filter(s.toString());
                } else {
                    adapter.filter(s.toString());
                }
                /*if (postAdaptor.getItemCount() == 0 || adapter.getItemCount() == 0) {
                    textViewNoR.setVisibility(View.VISIBLE);
                } else {
                    textViewNoR.setVisibility(View.GONE);
                }*/
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        updateNoCollectionsMessage();
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
                            updateNoCollectionsMessage();
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
                    updateNoCollectionsMessage();
                    Toast.makeText(this, "Collection deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.updateData(CollectionManager.getAllCollections());
        updateNoCollectionsMessage();

        if (checkBoxPosts.isChecked()) {
            refreshAllPostsView();
        }
    }

    private void updateNoCollectionsMessage() {
        List<Collection> collections = CollectionManager.getAllCollections();
        if (collections.isEmpty()) {
            textViewNoCollections.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewNoCollections.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null) {
                        ArrayList<String> d = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        editTextSearchCo.setText(d.get(0));

                        // Hide the keyboard after setting text from voice recognition
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view = getCurrentFocus();
                        if (view == null) view = new View(CollectionPage.this);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                        // filterPosts(d.get(0)); // Not needed if TextWatcher is in place
                    }
                }
            });

    public void refreshAllPostsView() {
        List<Collection> updatedCollections = CollectionManager.getAllCollections();
        Set<Post> updatedUniquePosts = new HashSet<>();

        for (Collection collection : updatedCollections) {
            updatedUniquePosts.addAll(collection.getPosts());
        }

        allPosts.clear();
        allPosts.addAll(updatedUniquePosts);
        if (postAdaptor != null) {
            postAdaptor.updateData(allPosts);
            postAdaptor.filter(editTextSearchCo.getText().toString());
        }
    }
}
