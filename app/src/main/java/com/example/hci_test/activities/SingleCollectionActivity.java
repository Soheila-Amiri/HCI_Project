package com.example.hci_test.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.hci_test.model.Collection;
import com.example.hci_test.model.CollectionManager;
import com.example.hci_test.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SingleCollectionActivity extends AppCompatActivity {

    public static final String EXTRA_COLLECTION_NAME = "collection_name";

    private Collection collection;
    private PostAdaptor postAdapter;
    private TextView textViewTitle;
    private TextView textViewEmptyCollection;
    private TextView textViewNoResults;
    private EditText editTextSearch;
    private List<Post> reversedPosts;
    private List<Post> allPosts;

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

        textViewEmptyCollection = findViewById(R.id.textViewEmptyCollection);
        textViewNoResults = findViewById(R.id.textViewNoResults);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCollection);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        reversedPosts = new ArrayList<>(collection.getPosts());
        Collections.reverse(reversedPosts);

        allPosts = new ArrayList<>(collection.getPosts());
        Collections.reverse(allPosts); // newest first

        postAdapter = new PostAdaptor(reversedPosts, this, true, post -> {
            allPosts.remove(post);
            filterPosts(editTextSearch.getText().toString()); // update after deletion
        });
        recyclerView.setAdapter(postAdapter);

        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }
        });

        // Hide keyboard when user presses Enter/Done/Search on the keyboard
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                return false; // let it propagate if you want further listeners, or true to consume
            }
            return false;
        });

        ImageView imageViewMic = findViewById(R.id.imageViewMicC);
        imageViewMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Listening...");
                activityResultLauncher.launch(intent);
            }
        });

        updateEmptyCollectionMessage();
    }

    private void filterPosts(String query) {
        reversedPosts.clear();
        textViewNoResults.setVisibility(View.GONE);
        textViewEmptyCollection.setVisibility(View.GONE);

        if (query.isEmpty()) {
            reversedPosts.addAll(allPosts);
            updateEmptyCollectionMessage();
        } else {
            for (Post post : allPosts) {
                if (post.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    reversedPosts.add(post);
                }
            }
            if (reversedPosts.isEmpty()) {
                textViewNoResults.setVisibility(View.VISIBLE);
            }
        }
        postAdapter.notifyDataSetChanged();
    }

    private void updateEmptyCollectionMessage() {
        if (allPosts.isEmpty()) {
            textViewEmptyCollection.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyCollection.setVisibility(View.GONE);
        }
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

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null) {
                        ArrayList<String> d = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        editTextSearch.setText(d.get(0));

                        // Hide the keyboard after setting text from voice recognition
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view = getCurrentFocus();
                        if (view == null) view = new View(SingleCollectionActivity.this);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                        // filterPosts(d.get(0)); // Not needed if TextWatcher is in place
                    }
                }
            });
}
