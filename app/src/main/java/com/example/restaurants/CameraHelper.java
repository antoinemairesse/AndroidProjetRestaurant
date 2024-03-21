package com.example.restaurants;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraHelper {

    public static void startCamera(CameraActivity activity, PreviewView previewView,
                                   ImageButton capture, ActivityResultLauncher<String> permissionLauncher,
                                   ActivityResultLauncher<Intent> startActivityIntent) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            startCameraInternal(activity, previewView, capture, startActivityIntent);
        }
    }

    private static void startCameraInternal(CameraActivity activity, PreviewView previewView,
                                            ImageButton capture, ActivityResultLauncher<Intent> startActivityIntent) {
        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(activity);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(activity.getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageCapture);

                capture.setOnClickListener(view -> captureImage(activity, imageCapture, startActivityIntent));

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    private static void captureImage(CameraActivity activity, ImageCapture imageCapture, ActivityResultLauncher<Intent> startActivityIntent) {
        imageCapture.takePicture(ContextCompat.getMainExecutor(activity), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Bitmap capturedBitmap = ImageUtils.convertImageProxyToBitmap(image);

                // Save the bitmap to a file
                File imageFile = ImageUtils.saveBitmapToFile(activity, capturedBitmap);
                Uri imageUri = Uri.fromFile(imageFile);

                // Start FilterActivity with the image file URI
                Intent intent = new Intent(activity, FilterActivity.class);
                intent.putExtra("imageUri", imageUri);

                startActivityIntent.launch(intent);

                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle capture error
            }
        });
    }

    private static int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        return Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0) ?
                AspectRatio.RATIO_4_3 : AspectRatio.RATIO_16_9;
    }
}
