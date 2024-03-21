package com.example.restaurants.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.restaurants.CustomImageOverlay;
import com.example.restaurants.R;
import com.example.restaurants.models.Restaurant;
import com.example.restaurants.utils.FirebaseUtils;
import com.example.restaurants.utils.ImageUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.List;

/**
 * ReviewMapActivity displays a map with reviews of a selected restaurant.
 * It fetches restaurant data from Firestore, then retrieves reviews associated with that restaurant,
 * and displays them as markers on the map along with images.
 */
public class ReviewMapActivity extends BaseActivity {
    private MapView mapView;
    private Restaurant restaurant;
    private FirebaseFirestore db;
    private final OnCompleteListener<QuerySnapshot> onRestaurantFetched = taskSnapshot -> {
        List<DocumentSnapshot> documents = FirebaseUtils.getDocumentsFromTask(this, taskSnapshot);
        assert documents != null;
        DocumentSnapshot restaurant = documents.get(0);
        fetchReviews(restaurant);
    };

    /**
     * Fetches reviews associated with the provided restaurant document.
     *
     * @param restaurant DocumentSnapshot containing restaurant data.
     */
    private void fetchReviews(DocumentSnapshot restaurant) {

        OnCompleteListener<QuerySnapshot> onReviewsFetched = task -> {
            List<DocumentSnapshot> reviews = FirebaseUtils.getDocumentsFromTask(this, task);
            assert reviews != null;
            for (DocumentSnapshot review : reviews) {
                new AddReviewToMapTask().execute(review);
            }
            mapView.invalidate(); // refresh map
        };

        db.collection("reviews")
            .whereEqualTo("restaurant", restaurant)
            .get()
            .addOnCompleteListener(onReviewsFetched);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_map);
        setupToolbar(true);

        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Setup mapView (osmdroid)
        mapView = findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(5);


        db = FirebaseFirestore.getInstance();

        db.collection("restaurants")
            .whereEqualTo("name", restaurant.getName())
            .get()
            .addOnCompleteListener(onRestaurantFetched);
    }

    /**
     * AsyncTask to add review markers with images to the map asynchronously.
     */
    class AddReviewToMapTask extends AsyncTask<DocumentSnapshot, Void, Void> {

        // Method to fetch image drawable from URL and add it to the map as a marker
        private void getImageDrawableAndAddToMap(String imageUrl, double latitude, double longitude) {
            try {
                Drawable drawable = ImageUtils.drawableFromUrl(imageUrl);
                mapView.getOverlays().add(new CustomImageOverlay(drawable, latitude, longitude));
            } catch (IOException e) {
                Toast.makeText(ReviewMapActivity.this, "Failed to display image on map", Toast.LENGTH_SHORT).show();
            }
        }

        // Background task to process review documents and add markers to the map
        protected Void doInBackground(DocumentSnapshot... docs) {
            Double latitude = docs[0].getDouble("latitude");
            Double longitude = docs[0].getDouble("longitude");
            List<String> images = (List<String>) docs[0].get("images");

            if (images == null) return null;

            images.forEach(imageUrl -> getImageDrawableAndAddToMap(imageUrl, latitude, longitude));

            mapView.invalidate(); // refresh map
            return null;
        }


    }

}