package com.example.restaurants.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Restaurant implements Serializable {
    private int id;
    private String name;
    private String address;
    private double rating;
    private String price;

    private String description;

    private ArrayList<String> images;

    public Restaurant() {
    }

    public Restaurant(int id, String name, String address, double rating, String price, String description, ArrayList<String> images) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.price = price;
        this.description = description;
        this.images = images;
    }
    // Getters and setters for all fields

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRating() {
        return rating + "/10";
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPrice() {
        return price + "€";
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getId() {
        return String.valueOf(id);
    }
}

