package com.example.restaurants.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.restaurants.activities.CameraActivity;
import com.example.restaurants.activities.FilterActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * A helper class for managing camera functionalities within the application.
 */
public class CameraHelper {

    /**
     * Checks if the application has permission to use the device's camera.
     *
     * @param context The context of the application.
     * @return True if the permission is granted, false otherwise.
     */
    private static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Starts the camera if permission is granted, otherwise requests permission.
     *
     * @param activity            The CameraActivity instance.
     * @param previewView         The PreviewView to display the camera preview.
     * @param capture             The ImageButton to capture images.
     * @param permissionLauncher  The ActivityResultLauncher for requesting permissions.
     * @param startActivityIntent The ActivityResultLauncher for starting activities.
     */
    public static void startCamera(CameraActivity activity, PreviewView previewView, ImageButton capture, ActivityResultLauncher<String> permissionLauncher, ActivityResultLauncher<Intent> startActivityIntent) {
        if (hasCameraPermission(activity))
            permissionLauncher.launch(android.Manifest.permission.CAMERA);
        else startCameraInternal(activity, previewView, capture, startActivityIntent);
    }

    /**
     * Starts the camera internally after ensuring permission is granted.
     *
     * @param activity            The CameraActivity instance.
     * @param previewView         The PreviewView to display the camera preview.
     * @param capture             The ImageButton to capture images.
     * @param startActivityIntent The ActivityResultLauncher for starting activities.
     */
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

                cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageCapture);

                capture.setOnClickListener(view -> captureImage(activity, imageCapture, startActivityIntent));

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    /**
     * Captures an image using the provided ImageCapture instance.
     *
     * @param context             The Context.
     * @param imageCapture        The ImageCapture instance to capture the image.
     * @param startActivityIntent The ActivityResultLauncher for starting activities.
     */
    private static void captureImage(Context context, ImageCapture imageCapture, ActivityResultLauncher<Intent> startActivityIntent) {
        imageCapture.takePicture(ContextCompat.getMainExecutor(context), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Bitmap capturedBitmap = ImageUtils.convertImageProxyToBitmap(image);

                // Save the bitmap to a file
                File imageFile = ImageUtils.saveBitmapToFile(context, capturedBitmap);
                Uri imageUri = Uri.fromFile(imageFile);

                // Start FilterActivity with the image file URI
                Intent intent = new Intent(context, FilterActivity.class);
                intent.putExtra("imageUri", imageUri);

                startActivityIntent.launch(intent);

                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(context, "Error while taking picture, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Calculates the aspect ratio based on the provided width and height.
     *
     * @param width  The width of the preview.
     * @param height The height of the preview.
     * @return The aspect ratio value.
     */
    private static int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        return Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0) ?
            AspectRatio.RATIO_4_3 : AspectRatio.RATIO_16_9;
    }
}
