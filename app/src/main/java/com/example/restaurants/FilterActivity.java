package com.example.restaurants;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FilterActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sm = null;
    private Bitmap modifiedImage;
    private ImageView imageView;
    private Bitmap capturedImage;
    private TextView accelerometer;
    private int intensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Initialize views
        imageView = findViewById(R.id.imageView);

        // Retrieve the image file URI from the intent
        Uri imageUri = getIntent().getParcelableExtra("imageUri");

        // Load the image from the file
        capturedImage = loadBitmapFromFile(imageUri);

        setImage(capturedImage);

        accelerometer = findViewById(R.id.accelerometer);

        findViewById(R.id.applyGrayscale).setOnClickListener(v -> {
            Bitmap filteredImage = applyGrayscaleFilter(capturedImage);
            setImage(filteredImage);
        });

        findViewById(R.id.applyBrightness).setOnClickListener(v -> {
            Bitmap filteredImage = applyBrightnessFilter(capturedImage, intensity);
            setImage(filteredImage);
        });

        findViewById(R.id.sendImage).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            File imageFile = ImageUtils.saveBitmapToFile(this, modifiedImage);
            resultIntent.putExtra("modifiedImage", Uri.fromFile(imageFile));
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor mAccelerometer =
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private Bitmap loadBitmapFromFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Delete the image file when the activity is destroyed
        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri != null) {
            File imageFile = new File(imageUri.getPath());
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    // Example method to apply grayscale filter to the image
    private Bitmap applyGrayscaleFilter(Bitmap image) {
        // Convert image to grayscale
        // You can use ColorMatrixColorFilter or RenderScript for better performance
        // For simplicity, let's use BitmapFactory to convert to grayscale
        Bitmap grayImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(grayImage);
        android.graphics.Paint paint = new android.graphics.Paint();
        android.graphics.ColorMatrix colorMatrix = new android.graphics.ColorMatrix();
        colorMatrix.setSaturation(0);
        android.graphics.ColorMatrixColorFilter filter = new android.graphics.ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(image, 0, 0, paint);
        return grayImage;
    }

    // Example method to apply brightness filter to the image based on intensity
    private Bitmap applyBrightnessFilter(Bitmap image, int intensity) {
        // Adjust brightness based on intensity
        // For demonstration, let's adjust the brightness linearly based on intensity
        float brightnessFactor = (float) intensity / 100;
        Bitmap filteredImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.ColorMatrix colorMatrix = new android.graphics.ColorMatrix();
        colorMatrix.setScale(brightnessFactor, brightnessFactor, brightnessFactor, 1f);
        android.graphics.ColorMatrixColorFilter filter = new android.graphics.ColorMatrixColorFilter(colorMatrix);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColorFilter(filter);
        android.graphics.Canvas canvas = new android.graphics.Canvas(filteredImage);
        canvas.drawBitmap(image, 0, 0, paint);
        return filteredImage;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {

            // Calculate the magnitude of acceleration vector
            float x = Math.abs(event.values[0]);
            float y = Math.abs(event.values[1]);
            float z = Math.abs(event.values[2]);
            double accelerationMagnitude = Math.sqrt(x * x + y * y + z * z) * 4;

            intensity = (int) accelerationMagnitude;
            accelerometer.setText(String.valueOf(intensity));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setImage(Bitmap image) {
        modifiedImage = image;
        imageView.setImageBitmap(image);
    }
}
