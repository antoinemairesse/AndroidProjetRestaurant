package com.example.restaurants.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.restaurants.R;
import com.example.restaurants.models.Restaurant;
import com.example.restaurants.utils.FirebaseUtils;
import com.example.restaurants.utils.ImageUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewActivity extends BaseActivity {

    private final ArrayList<Uri> modifiedImages = new ArrayList<>();
    private LinearLayout imageContainer;
    private Restaurant restaurant;
    EditText description;
    Button buttonAddPicture;
    Button buttonPostReview;

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri modifiedImage = result.getData().getParcelableExtra("modifiedImage");
                modifiedImages.add(modifiedImage);
                displayModifiedImages();
            }
        }
    );

    ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), result -> {
            if (result) postReviewToFirestore(description.getText().toString());
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        setupToolbar(true);

        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");

        setUpViews();

        buttonAddPicture.setOnClickListener(v -> launchCameraActivity());
        buttonPostReview.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            } else postReviewToFirestore(description.getText().toString());
        });
    }

    private void setUpViews() {
        buttonAddPicture = findViewById(R.id.buttonAddPicture);
        buttonPostReview = findViewById(R.id.buttonPostReview);
        description = findViewById(R.id.description);
        imageContainer = findViewById(R.id.imageContainer);
    }

    private void launchCameraActivity() {
        Intent reviewIntent = new Intent(this, CameraActivity.class);
        cameraActivityResultLauncher.launch(reviewIntent);
    }

    private void displayModifiedImages() {
        imageContainer.removeAllViews(); // Clear previous views

        for (Uri uri : modifiedImages) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                200, 300
            );
            imageView.setLayoutParams(layoutParams);
            imageView.setImageURI(uri);
            imageContainer.addView(imageView);
        }
    }

    private void postReviewToFirestore(String description) {
        if (modifiedImages.isEmpty()) {
            Toast.makeText(this, "Please add images before posting the review", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadImagesAndSaveReview(description);
    }

    private void uploadImagesAndSaveReview(String description) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        List<String> imageUrls = new ArrayList<>();
        // Counter to keep track of the number of images uploaded
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : modifiedImages) {
            ImageUtils.uploadToFirebaseStorage("reviews/", imageUri, new ImageUtils.UploadToFirebaseStorageCallback() {
                @Override
                public void onSuccess(Uri uri) {
                    // Add the download URL to the list of image URLs
                    String imageUrl = uri.toString();
                    imageUrls.add(imageUrl);

                    // Check if all images have been uploaded
                    if (uploadCount.incrementAndGet() == modifiedImages.size()) {
                        // All images uploaded, save the review in Firestore
                        saveReviewToFirestore(description, imageUrls);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    showToastAndFinish("Failed to upload image");
                }
            });
        }
    }

    private Location getCurrentLocation() {
        // Check if the app has permission to access the device's location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider == null) provider = LocationManager.GPS_PROVIDER;
            return locationManager.getLastKnownLocation(provider);
        }
        showToastAndFinish("Couldn't fetch location");
        return null;
    }

    private void showToastAndFinish(String text) {
        Toast.makeText(ReviewActivity.this, text, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveReviewToFirestore(String description, List<String> imageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Location location = getCurrentLocation();
        if (location == null) return;

        db.collection("restaurants")
            .whereEqualTo("name", restaurant.getName())
            .get()
            .addOnCompleteListener(taskSnapshot -> {
                DocumentSnapshot document = FirebaseUtils.getDocumentsFromTask(this, taskSnapshot).get(0);

                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("restaurant", document);
                reviewData.put("description", description);
                reviewData.put("latitude", location.getLatitude());
                reviewData.put("longitude", location.getLongitude());
                reviewData.put("images", imageUrls);

                db.collection("reviews")
                    .add(reviewData)
                    .addOnSuccessListener(documentReference -> showToastAndFinish("Review posted successfully"))
                    .addOnFailureListener(e -> showToastAndFinish("Failed to post review"));
            });
    }

}