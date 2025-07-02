package com.example.hci_test;

import com.example.hci_test.model.Collection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonFileHelper {
    private static final String FILE_NAME = "collections.json";
    private Context context;

    public JsonFileHelper(Context context) {
        this.context = context;
    }

    public void saveCollections(List<Collection> collections) {
        try {
            Gson gson = new Gson();
            String jsonString = gson.toJson(collections);

            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(jsonString);
            writer.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Collection> loadCollections() {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            InputStreamReader reader = new InputStreamReader(fis);
            Gson gson = new Gson();

            Type collectionListType = new TypeToken<List<Collection>>(){}.getType();
            List<Collection> collections = gson.fromJson(reader, collectionListType);

            reader.close();
            fis.close();

            return collections;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void addCollection(Collection newCollection) {
        List<Collection> collections = loadCollections();  // Load existing collections

        if (collections == null) {
            collections = new ArrayList<>();
        }

        collections.add(newCollection);

        saveCollections(collections);
    }
}
