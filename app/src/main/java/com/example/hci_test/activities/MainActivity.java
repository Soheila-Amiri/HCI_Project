package com.example.hci_test.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hci_test.model.CollectionManager;
import com.example.hci_test.model.Post;
import com.example.hci_test.PostAdaptor;
import com.example.hci_test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    OkHttpClient okHttpClient;
    List<Post> postList = new ArrayList<>();
    RecyclerView recyclerView;
    PostAdaptor postAdaptor;
    LinearLayoutManager layoutManager;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);

        EditText editTextSearch = findViewById(R.id.editTextSearch);
        ImageView imageViewSearch = findViewById(R.id.imageViewSearch);
        progressBar = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        okHttpClient = new OkHttpClient();

        ImageView imageViewCollection = findViewById(R.id.imageViewCollection);
        imageViewCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CollectionPage.class);
                startActivity(intent);
            }
        });

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postList.clear();
                String textSearch = editTextSearch.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                makeCall(textSearch);
            }
        });

    }
    public void makeCall(String textSearch){
        Request request = new Request.Builder()
                .url("https://api.pexels.com/v1/search?query="+textSearch+"&per_page=80&page=1") // loop to have all pages' results
                .addHeader("Authorization", "tNEayw052ElHvI5oL0RHHFvVqrBYAPO64QkP8tSsjRR6IxuL8VLg3Qaf")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //Log.d("download", "onResponse: " + response.body().string());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.getInt("total_results") == 0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(MainActivity.this, "Your Search had no results!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    JSONArray postObjects = jsonObject.getJSONArray("photos");
                    long randomSeed = textSearch.toLowerCase().hashCode();
                    Random random = new Random(randomSeed);
                    for (int i = 0; i < postObjects.length(); i++){
                        JSONObject singlePost = postObjects.getJSONObject(i);
                        Post postObject = new Post();
                        if (singlePost.getString("url") != null){
                            String imageUrl = singlePost.getJSONObject("src").getString("landscape");
                            postObject.setUrl(imageUrl);
                            postObject.setPhotographer(singlePost.getString("photographer"));
                            postObject.setDescription(singlePost.getString("alt"));
                            int profileImageIndex = random.nextInt(10) + 1;
                            String profileImagePath = "file:///android_asset/Profile_pictures/" + profileImageIndex + ".jpg";
                            String likeNum = "" + random.nextInt(100) + 1;
                            postObject.setUserProfile(profileImagePath);
                            postObject.setLikes(likeNum);
                            postList.add(postObject);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postAdaptor = new PostAdaptor(postList, MainActivity.this, false, post -> {});
                            recyclerView.setAdapter(postAdaptor);
                            progressBar.setVisibility(View.INVISIBLE);
                            recyclerView.setLayoutManager(layoutManager);
                        }
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void openNewCollectionDialog() {
        openNewCollectionDialog(null);
    }

   /* public void openNewCollectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.editTextCollectionName);

        new AlertDialog.Builder(this)
                .setTitle("New Collection")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean created = CollectionManager.createCollection(name);
                        if (created) {
                            Toast.makeText(this, "Collection created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Collection already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    } */

    public void openNewCollectionDialog(Post postToAdd) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.editTextCollectionName);

        new AlertDialog.Builder(this)
                .setTitle("New Collection")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean created = CollectionManager.createCollection(name);
                        if (created) {
                            if (postToAdd != null) {
                                CollectionManager.addPostToCollection(name, postToAdd);
                            }
                            Toast.makeText(this, "Collection created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Collection already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}