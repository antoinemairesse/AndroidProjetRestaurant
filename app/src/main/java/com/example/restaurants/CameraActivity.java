package com.example.restaurants;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

// CameraActivity.java
public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageButton capture;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> startActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.cameraPreview);
        capture = findViewById(R.id.capture);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> {
                    if (result) {
                        startCamera();
                    }
                });

        startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri modifiedImage = result.getData().getParcelableExtra("modifiedImage");
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("modifiedImage", modifiedImage);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });

        CameraHelper.startCamera(this, previewView, capture, permissionLauncher, startActivityIntent);
    }

    private void startCamera() {
        CameraHelper.startCamera(this, previewView, capture, permissionLauncher, startActivityIntent);
    }
}
