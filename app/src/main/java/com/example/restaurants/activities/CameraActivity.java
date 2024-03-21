package com.example.restaurants.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.example.restaurants.R;
import com.example.restaurants.utils.CameraHelper;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageButton capture;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.cameraPreview);
        capture = findViewById(R.id.capture);

        // Initialize permission launcher for requesting camera permission
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) startCamera();
                else {
                    Toast.makeText(this, "Allow camera usage for this app to continue !", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        );

        // Initialize activity launcher for starting camera activity
        activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::handleActivityResult
        );

        startCamera();
    }

    /**
     * Handles the result of camera activity.
     *
     * @param result The result of camera activity
     */
    private void handleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Uri modifiedImage = result.getData().getParcelableExtra("modifiedImage");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("modifiedImage", modifiedImage);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    private void startCamera() {
        CameraHelper.startCamera(this, previewView, capture, cameraPermissionLauncher, activityResultLauncher);
    }
}
