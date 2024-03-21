package com.example.restaurants;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.List;


public class ReviewMapActivity extends BaseActivity {
    private MapView mapView;
    private Restaurant restaurant;
    private FirebaseFirestore db;
    private OnCompleteListener<QuerySnapshot> onRestaurantFetched = taskSnapshot -> {
        List<DocumentSnapshot> documents = getDocumentsFromTask(taskSnapshot);
        assert documents != null;
        DocumentSnapshot restaurant = documents.get(0);
        fetchReviews(restaurant);
    };

    private void fetchReviews(DocumentSnapshot restaurant) {

        OnCompleteListener<QuerySnapshot> onReviewsFetched = task -> {
            List<DocumentSnapshot> reviews = getDocumentsFromTask(task);
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

    // TODO MOVE IN UTILS
    private List<DocumentSnapshot> getDocumentsFromTask(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            return task.getResult().getDocuments();
        } else {
            Toast.makeText(ReviewMapActivity.this, "Error while fetching data, please try again", Toast.LENGTH_SHORT).show();
        }
        return null;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_map);
        setupToolbar(true);

        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Get reference to MapView from layout
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

    class AddReviewToMapTask extends AsyncTask<DocumentSnapshot, Void, Void> {

        private void getImageDrawableAndAddToMap(String imageUrl, double latitude, double longitude) {
            try {
                Drawable drawable = ImageUtils.drawableFromUrl(imageUrl);
                mapView.getOverlays().add(new CustomImageOverlay(drawable, latitude, longitude));
            } catch (IOException e) {
                Toast.makeText(ReviewMapActivity.this, "Failed to display image on map", Toast.LENGTH_SHORT).show();
            }
        }

        protected Void doInBackground(DocumentSnapshot... docs) {
            Double latitude = docs[0].getDouble("latitude");
            Double longitude = docs[0].getDouble("longitude");
            List<String> images = (List<String>) docs[0].get("images");

            if(images == null) return null;

            images.forEach(imageUrl -> getImageDrawableAndAddToMap(imageUrl, latitude, longitude));

            mapView.invalidate(); // refresh map
            return null;
        }


    }

}