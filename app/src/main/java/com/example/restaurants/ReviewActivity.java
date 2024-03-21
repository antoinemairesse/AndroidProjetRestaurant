package com.example.restaurants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    EditText description;
    private final ArrayList<Uri> modifiedImages = new ArrayList<>();
    private LinearLayout imageContainer;
    private Restaurant restaurant;
    ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    postReviewToFirestore(description.getText().toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        setupToolbar(true);

        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");

        Button buttonAddPicture = findViewById(R.id.buttonAddPicture);
        Button buttonPostReview = findViewById(R.id.buttonPostReview);
        description = findViewById(R.id.description);
        imageContainer = findViewById(R.id.imageContainer);

        ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri modifiedImage = result.getData().getParcelableExtra("modifiedImage");
                        modifiedImages.add(modifiedImage);
                        displayModifiedImages();
                    }
                });

        buttonAddPicture.setOnClickListener(v -> {
            Intent reviewIntent = new Intent(this, CameraActivity.class);
            startActivityIntent.launch(reviewIntent);
        });

        buttonPostReview.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                locationPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            } else postReviewToFirestore(description.getText().toString());
        });
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
        // Check if there are images to upload
        if (modifiedImages.isEmpty()) {
            // No images to upload
            Toast.makeText(this, "Please add images before posting the review", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload images to Firebase Storage and save the review in Firestore
        uploadImagesAndSaveReview(description);
    }

    private void uploadImagesAndSaveReview(String description) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // List to store the download URLs of the uploaded images
        List<String> imageUrls = new ArrayList<>();

        // Counter to keep track of the number of images uploaded
        AtomicInteger uploadCount = new AtomicInteger(0);

        // Upload each image to Firebase Storage
        for (Uri imageUri : modifiedImages) {
            // Generate a unique file name for each image
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child("reviews/" + imageName);

            // Upload the image to Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Add the download URL to the list of image URLs
                            String imageUrl = uri.toString();
                            imageUrls.add(imageUrl);

                            // Check if all images have been uploaded
                            if (uploadCount.incrementAndGet() == modifiedImages.size()) {
                                // All images uploaded, save the review in Firestore
                                saveReviewToFirestore(description, imageUrls);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle unsuccessful uploads
                        Toast.makeText(ReviewActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private Location getCurrentLocation() {
        // Check if the app has permission to access the device's location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Create a LocationManager instance
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // Get the last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return lastKnownLocation;
        }
        return null;
    }

    private void saveReviewToFirestore(String description, List<String> imageUrls) {
        System.out.println("SAVE REVIEW" + restaurant.getId());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
                .whereEqualTo("name", restaurant.getName())
                .get()
                .addOnCompleteListener(taskSnapshot -> {
                    if (taskSnapshot.isSuccessful()) {
                        DocumentSnapshot document = taskSnapshot.getResult().getDocuments().get(0);
                        // Create a new review document in Firestore
                        Map<String, Object> reviewData = new HashMap<>();
                        reviewData.put("restaurant", document);
                        reviewData.put("description", description);
                        reviewData.put("latitude", getCurrentLocation().getLatitude());
                        reviewData.put("longitude", getCurrentLocation().getLongitude());
                        reviewData.put("images", imageUrls);

                        db.collection("reviews")
                                .add(reviewData)
                                .addOnSuccessListener(documentReference -> {
                                    // Review document added successfully
                                    Toast.makeText(ReviewActivity.this, "Review posted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle any errors
                                    Toast.makeText(ReviewActivity.this, "Failed to post review", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    } else {
                        // Handle error
                    }
                });


    }

}