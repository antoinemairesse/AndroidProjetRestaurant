package com.example.restaurants;

import static com.example.restaurants.AppConfig.MARKER_COORDS_NOISE;
import static com.example.restaurants.AppConfig.MARKER_IMAGE_HEIGHT;
import static com.example.restaurants.AppConfig.MARKER_IMAGE_WIDTH;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.Random;

public class CustomImageOverlay extends Overlay {
    private final Drawable imageDrawable;
    private final GeoPoint geoPoint;

    public CustomImageOverlay(Drawable imageDrawable, double latitude, double longitude) {
        super();
        this.imageDrawable = imageDrawable;
        // Add a small random offset to the latitude and longitude so we can see images on same coordinates
        double offset = MARKER_COORDS_NOISE;
        Random random = new Random();
        double randomLatOffset = offset * (random.nextDouble() - 0.5);
        double randomLonOffset = offset * (random.nextDouble() - 0.5);
        this.geoPoint = new GeoPoint(latitude + randomLatOffset, longitude + randomLonOffset);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if (!shadow) {

            // Convert geoPoint to screen coordinates
            Point point = mapView.getProjection().toPixels(geoPoint, null);

            // Calculate image bounds
            int imageWidth = MARKER_IMAGE_WIDTH;
            int imageHeight = MARKER_IMAGE_HEIGHT;
            int left = point.x - (imageWidth / 2);
            int top = point.y - imageHeight;

            // Draw the image on the canvas
            imageDrawable.setBounds(left, top, left + imageWidth, top + imageHeight);
            imageDrawable.draw(canvas);
        }
    }
}
