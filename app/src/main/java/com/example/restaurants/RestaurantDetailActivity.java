package com.example.restaurants;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.Calendar;

public class RestaurantDetailActivity extends BaseActivity {

    private TextView textViewReservationDate;
    private Restaurant restaurant;
    private ViewPager viewPager;
    private ImagePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        setupToolbar(true);

        textViewReservationDate = findViewById(R.id.textViewReservationDate);

        // Set OnClickListener for the reservation date TextView
        textViewReservationDate.setOnClickListener(v -> showDatePickerDialog());

        // Get the selected restaurant from the intent
        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");


        // Initialize views
        TextView nameTextView = findViewById(R.id.textViewRestaurantName);
        TextView addressTextView = findViewById(R.id.textViewRestaurantAddress);
        TextView ratingTextView = findViewById(R.id.textViewRestaurantRating);
        TextView priceTextView = findViewById(R.id.textViewRestaurantPrice);
        TextView descriptionTextView = findViewById(R.id.textViewRestaurantDescription);

        // Set restaurant details to views
        assert restaurant != null;
        nameTextView.setText(restaurant.getName());
        addressTextView.setText(restaurant.getAddress());
        ratingTextView.setText("Rating : " + restaurant.getRating());
        priceTextView.setText("Average Price : " + restaurant.getPrice());
        descriptionTextView.setText(restaurant.getDescription());


        viewPager = findViewById(R.id.viewPager);

        // Set up the ViewPager adapter
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);


        Button makeReservationBtn = findViewById(R.id.buttonMakeReservation);
        EditText reservationDateField = findViewById(R.id.textViewReservationDate);
        EditText numberPeopleField = findViewById(R.id.editTextNumberOfPeople);

        makeReservationBtn.setEnabled(false); // set button disable initially

        // Add TextChangedListener to editTextField1
        reservationDateField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if both EditText fields have values
                // Disable the button
                makeReservationBtn.setEnabled(!TextUtils.isEmpty(reservationDateField.getText()) && !TextUtils.isEmpty(numberPeopleField.getText())); // Enable the button
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

// Add TextChangedListener to editTextField2
        numberPeopleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if both EditText fields have values
                // Disable the button
                makeReservationBtn.setEnabled(!TextUtils.isEmpty(reservationDateField.getText()) && !TextUtils.isEmpty(numberPeopleField.getText())); // Enable the button
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        findViewById(R.id.buttonAddReview).setOnClickListener(v -> {
            Intent reviewIntent = new Intent(this, ReviewActivity.class);
            reviewIntent.putExtra("restaurant", restaurant);
            this.startActivity(reviewIntent);
        });

        findViewById(R.id.buttonSeeReview).setOnClickListener(v -> {
            Intent reviewIntent = new Intent(this, ReviewMapActivity.class);
            reviewIntent.putExtra("restaurant", restaurant);
            this.startActivity(reviewIntent);
        });

        Button buttonMakeReservation = findViewById(R.id.buttonMakeReservation);
        buttonMakeReservation.setOnClickListener(v -> {
            Toast.makeText(this, "Reservation successful!", Toast.LENGTH_SHORT).show();
            finish();
        });

    }

    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog and set listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth1) -> {
                    // Update reservation date TextView with selected date
                    String selectedDate = dayOfMonth1 + "/" + (monthOfYear + 1) + "/" + year1;
                    textViewReservationDate.setText(selectedDate);
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }

    // Custom PagerAdapter for the ViewPager
    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return restaurant.getImages().size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            // Inflate the layout for each image
            View view = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.image_item, container, false);

            ImageView imageView = view.findViewById(R.id.imageView);

            ImageLoader.load(restaurant.getImages().get(position), imageView);


            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

}
