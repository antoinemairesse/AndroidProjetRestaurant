package com.example.restaurants.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.camera.core.ImageProxy;

import com.example.restaurants.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;

// ImageUtils.java
public class ImageUtils {

    public static Bitmap convertImageProxyToBitmap(ImageProxy imageProxy) {
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private static String generateRandomFileName(String base, String extension) {
        String randomString = UUID.randomUUID().toString();
        return base + randomString + extension;
    }

    public static File saveBitmapToFile(Context context, Bitmap bitmap) {
        String fileName = generateRandomFileName("captured_image_", ".jpg");
        File file = new File(context.getFilesDir(), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public interface UploadToFirebaseStorageCallback {
        void onSuccess(Uri uri);
        void onFailure(Exception e);
    }

    public static void uploadToFirebaseStorage(String pathString, Uri imageUri, UploadToFirebaseStorageCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference imageRef = storageRef.child(pathString + generateRandomFileName("image_", ".jpg"));

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully, now get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(callback::onSuccess);
            })
            .addOnFailureListener(callback::onFailure);
    }

    public static void loadWithPlaceholder(String imgUrl, ImageView imageView) {
        Picasso.get().load(imgUrl).placeholder(R.drawable.loading).into(imageView);
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(Resources.getSystem(), x);
    }
}
