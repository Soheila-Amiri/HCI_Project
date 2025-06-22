package com.example.hci_test.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Collection {
    private String name;
    private List<Post> posts;

    public Collection(String name) {
        this.name = name;
        this.posts = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Collection)) return false;
        Collection other = (Collection) obj;
        return Objects.equals(this.name, other.name);
    }

    public String getThumbnailUrl() {
        if (posts != null && !posts.isEmpty()) {
            return posts.get(0).getUrl();
        }
        return "placeholder";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void setName(String name) {
        this.name = name;
    }
}
