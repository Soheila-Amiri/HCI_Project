package com.example.hci_test.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CollectionManager {

    private static final LinkedHashMap<String, Collection> collections = new LinkedHashMap<>();

    public static boolean createCollection(String name) {
        if (collections.containsKey(name)) return false;
        collections.put(name, new Collection(name));
        return true;
    }

    public static List<Collection> getAllCollections() {
        List<Collection> list = new ArrayList<>(collections.values());
        // Return in reverse order (latest first)
        List<Collection> reversed = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) reversed.add(list.get(i));
        return reversed;
    }

    public static void removeCollection(String name) {
        collections.remove(name);
    }

    public static List<String> getAllCollectionNames() {
        return new ArrayList<>(collections.keySet());
    }

    public static Collection getCollectionByName(String name) {
        return collections.get(name);
    }


    public static boolean addPostToCollection(String collectionName, Post post) {
        if (!collections.containsKey(collectionName)) return false;
        Collection collection = collections.get(collectionName);
        if (collection.getPosts().contains(post)) return false;
        collection.addPost(post);
        return true;
    }

    public static boolean renameCollection(String oldName, String newName) {
        if (!collections.containsKey(oldName) || collections.containsKey(newName)) return false;

        Collection collection = collections.remove(oldName);
        collection.setName(newName);
        collections.put(newName, collection);
        return true;
    }

}
