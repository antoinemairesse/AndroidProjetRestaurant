package com.example.restaurants.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurants.R;
import com.example.restaurants.adapter.RestaurantAdapter;
import com.example.restaurants.models.Restaurant;
import com.example.restaurants.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private final List<Restaurant> restaurantList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar(false);

        recyclerView = findViewById(R.id.recyclerViewRestaurants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        fetchAndDisplayRestaurants();
    }

    private void fetchAndDisplayRestaurants() {
        db.collection("restaurants")
            .get()
            .addOnCompleteListener(task -> {
                List<DocumentSnapshot> restaurants = FirebaseUtils.getDocumentsFromTask(this, task);

                assert restaurants != null;

                for (DocumentSnapshot restaurant : restaurants) {
                    restaurantList.add(restaurant.toObject(Restaurant.class));
                }

                adapter = new RestaurantAdapter(this, restaurantList);
                recyclerView.setAdapter(adapter);
            });
    }
}