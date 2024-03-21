package com.example.restaurants;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageLoader {
    public static void load(String imgUrl, ImageView imageView) {
        Picasso.get().load(imgUrl).placeholder(R.drawable.loading).into(imageView);
    }
}
