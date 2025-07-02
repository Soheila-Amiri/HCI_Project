package com.example.hci_test.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hci_test.BuildConfig;
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
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    OkHttpClient okHttpClient;
    List<Post> postList = new ArrayList<>();
    RecyclerView recyclerView;
    PostAdaptor postAdaptor;
    LinearLayoutManager layoutManager;
    ProgressBar progressBar;
    EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);

        editTextSearch = findViewById(R.id.editTextSearch);
        ImageView imageViewSearch = findViewById(R.id.imageViewSearch);
        progressBar = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView imageViewMic = findViewById(R.id.imageViewMic);

        okHttpClient = new OkHttpClient();

        ImageView imageViewCollection = findViewById(R.id.imageViewCollection);
        imageViewCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CollectionPage.class);
                startActivity(intent);
            }
        });

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

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
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
                            if (postObject.getDescription().length() == 0)
                               imageCaptioning(postObject);
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

    private void performSearch() {
        // Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) view = new View(this); // fallback if no focus
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        postList.clear();
        String textSearch = editTextSearch.getText().toString();
        if (textSearch.isEmpty()) {
            Toast.makeText(MainActivity.this, "No query has been enterd!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        makeCall(textSearch);
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

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null) {
                        ArrayList<String> d = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        //editTextSearch.setText(editTextSearch.getText()+" "+d.get(0));
                        editTextSearch.setText(d.get(0));
                        performSearch();
                    }
                }
            });

    public void imageCaptioning(Post post) {
        String imageUrl = post.getUrl();
        String endpoint = "https://post-captioning.cognitiveservices.azure.com/";
        String subscriptionKey = BuildConfig.AZURE_KEY;
        String url = endpoint + "vision/v3.2/describe";

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("url", imageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray captions = jsonResponse.getJSONObject("description").getJSONArray("captions");
                        if (captions.length() > 0) {
                            String caption = captions.getJSONObject(0).getString("text");
                            Log.d("Caption", "Caption: " + caption);
                            post.setDescription(caption);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("AzureError", response.body().string());
                }
            }
        });
    }
}