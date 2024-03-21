package com.example.restaurants.activities;

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

import com.example.restaurants.R;
import com.example.restaurants.models.Restaurant;
import com.example.restaurants.utils.ImageUtils;

import java.util.Calendar;

public class RestaurantDetailActivity extends BaseActivity {

    private Restaurant restaurant;
    private ViewPager viewPager;
    private ImagePagerAdapter adapter;
    private EditText reservationDateField;
    private Button makeReservationBtn;
    private EditText numberPeopleField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        setupToolbar(true);

        // Get the selected restaurant from the intent
        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");


        // Initialize views
        TextView nameTextView = findViewById(R.id.textViewRestaurantName);
        TextView addressTextView = findViewById(R.id.textViewRestaurantAddress);
        TextView ratingTextView = findViewById(R.id.textViewRestaurantRating);
        TextView priceTextView = findViewById(R.id.textViewRestaurantPrice);
        TextView descriptionTextView = findViewById(R.id.textViewRestaurantDescription);
        makeReservationBtn = findViewById(R.id.buttonMakeReservation);
        reservationDateField = findViewById(R.id.textViewReservationDate);
        numberPeopleField = findViewById(R.id.editTextNumberOfPeople);
        viewPager = findViewById(R.id.viewPager);

        makeReservationBtn.setEnabled(false); // set button disable initially

        // Set restaurant details to views
        assert restaurant != null;
        nameTextView.setText(restaurant.getName());
        addressTextView.setText(restaurant.getAddress());
        ratingTextView.setText("Rating : " + restaurant.getRating());
        priceTextView.setText("Average Price : " + restaurant.getPrice());
        descriptionTextView.setText(restaurant.getDescription());

        // Set up the ViewPager adapter
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);

        setupListeners();

    }

    // Method to add TextChangedListener to EditText fields
    private void addTextChangedListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Update button state based on EditText fields
    private void updateButtonState() {
        makeReservationBtn.setEnabled(!TextUtils.isEmpty(reservationDateField.getText()) &&
            !TextUtils.isEmpty(numberPeopleField.getText()));
    }

    private void setupListeners() {
        addTextChangedListener(reservationDateField);
        addTextChangedListener(numberPeopleField);
        reservationDateField.setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.buttonAddReview).setOnClickListener(v -> startReviewActivity());
        findViewById(R.id.buttonSeeReview).setOnClickListener(v -> startReviewMapActivity());
        makeReservationBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Reservation successful!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startReviewActivity() {
        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        reviewIntent.putExtra("restaurant", restaurant);
        this.startActivity(reviewIntent);
    }

    private void startReviewMapActivity() {
        Intent reviewIntent = new Intent(this, ReviewMapActivity.class);
        reviewIntent.putExtra("restaurant", restaurant);
        this.startActivity(reviewIntent);
    }

    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog and set listener
        new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth1) -> {
            // Update reservation date TextView with selected date
            String month_ = String.valueOf(monthOfYear + 1);
            if (monthOfYear + 1 < 10) month_ = "0" + (monthOfYear + 1);
            String selectedDate = dayOfMonth1 + "/" + month_ + "/" + year1;
            reservationDateField.setText(selectedDate);
        }, year, month, dayOfMonth).show();
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

            ImageUtils.loadWithPlaceholder(restaurant.getImages().get(position), imageView);

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

}
