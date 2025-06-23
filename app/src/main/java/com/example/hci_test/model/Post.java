package com.example.hci_test.model;

import java.util.Objects;

public class Post {
    private String url;
    private String photographer;
    private String description;
    private String likes;

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    private String userProfile;

    public Post() {}

    public Post(String url, String photographer, String description, String likes, String userProfile) {
        this.url = url;
        this.photographer = photographer;
        this.description = description;
        this.likes = likes;
        this.userProfile = userProfile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhotographer() {
        return photographer;
    }

    public void setPhotographer(String photographer) {
        this.photographer = photographer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Prevent duplicates in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return Objects.equals(url, post.url); // unique URL
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
