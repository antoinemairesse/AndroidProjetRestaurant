package com.example.restaurants;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final Context context;
    private final List<Restaurant> restaurantList;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.restaurant_item, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        final Restaurant restaurant = restaurantList.get(position);
        ImageLoader.load(restaurant.getImages().get(0), holder.imageViewRestaurant);
        holder.textViewName.setText(restaurant.getName());
        holder.textViewAddress.setText(restaurant.getAddress());
        holder.textViewRating.setText("Rating: " + restaurant.getRating());
        holder.textViewPrice.setText("Average Price: " + restaurant.getPrice());

        // Set click listener
        holder.itemView.setOnClickListener(view -> {
            // Launch activity or fragment to display restaurant details
            Intent intent = new Intent(context, RestaurantDetailActivity.class);
            intent.putExtra("restaurant", restaurant);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRestaurant;
        TextView textViewName;
        TextView textViewAddress;
        TextView textViewRating;
        TextView textViewPrice;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewRestaurant = itemView.findViewById(R.id.imageViewRestaurant);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}

