package com.example.hci_test;

import android.os.Bundle;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
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
                    for (int i = 0; i < postObjects.length(); i++){
                        JSONObject singlePost = postObjects.getJSONObject(i);
                        Post postObject = new Post();
                        if (singlePost.getString("url") != null){
                            String imageUrl = singlePost.getJSONObject("src").getString("landscape");
                            postObject.setUrl(imageUrl);
                            postObject.setPhotographer(singlePost.getString("photographer"));
                            postObject.setDescription(singlePost.getString("alt"));
                            postList.add(postObject);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postAdaptor = new PostAdaptor(postList, MainActivity.this);
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
}