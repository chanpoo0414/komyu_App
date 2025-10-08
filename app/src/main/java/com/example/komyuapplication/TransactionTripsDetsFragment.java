package com.example.komyuapplication;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionTripsDetsFragment extends Fragment {

    private static final String ARG_ACTIVITY = "activity_data";
    private APIResponse.RecentActivity activity;
    private UserSessionManager sessionManager;

    public static TransactionTripsDetsFragment newInstance(APIResponse.RecentActivity activity) {
        TransactionTripsDetsFragment fragment = new TransactionTripsDetsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTIVITY, activity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            activity = (APIResponse.RecentActivity) getArguments().getSerializable(ARG_ACTIVITY);
        }
        sessionManager = new UserSessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_trips_dets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
        populateData();
    }

    private void setupBackButton() {
        ImageView btnBack = getView().findViewById(R.id.btn_back_transactionDetails);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void populateData() {
        if (activity == null) {
            Log.e("TripDets", "Activity is NULL!");
            return;
        }

        try {
            // DEBUG: Log all received data
            Log.d("TripDets", "=== DEBUG DATA RECEIVED ===");
            Log.d("TripDets", "Type: " + activity.type);
            Log.d("TripDets", "Title: " + activity.title);
            Log.d("TripDets", "Subtitle: " + activity.subtitle);
            Log.d("TripDets", "Amount: " + activity.amount);
            Log.d("TripDets", "Date: " + activity.date);
            Log.d("TripDets", "Route: " + activity.route);
            Log.d("TripDets", "Driver: " + activity.driver_name);
            Log.d("TripDets", "Conductor: " + activity.conductor_name);
            Log.d("TripDets", "User Category: " + activity.user_category);
            Log.d("TripDets", "Transaction Code: " + activity.transaction_code);
            Log.d("TripDets", "=== END DEBUG ===");

            // Set main title and icon
            TextView mainTitle = getView().findViewById(R.id.tv_transactionDetails_mainTitle);
            ImageView icon = getView().findViewById(R.id.icon_transactionDetails);

            mainTitle.setText(activity.title != null ? activity.title : "Bus Trip");
            icon.setImageResource(R.drawable.bus_icon);

            // Set amount
            TextView amount = getView().findViewById(R.id.tv_transactionDetails_amount);
            amount.setText(activity.amount);
            amount.setTextColor(getResources().getColor(R.color.RedLogout));

            // Parse and format date - CORRECT TIME FORMATTING
            TextView datetime = getView().findViewById(R.id.tv_transactionDetails_datetime);
            if (activity.date != null && !activity.date.isEmpty()) {
                try {
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

                    Date activityDate = apiFormat.parse(activity.date);
                    String formattedDate = displayFormat.format(activityDate);
                    datetime.setText(formattedDate);
                    Log.d("TripDets", "Date parsed successfully: " + formattedDate);
                } catch (Exception e) {
                    Log.e("TripDets", "Date parsing error: " + e.getMessage());
                    datetime.setText("Date not available");
                }
            } else {
                datetime.setText("Date not available");
                Log.d("TripDets", "Date field is null or empty");
            }

            // Set route - use the new field
            TextView route = getView().findViewById(R.id.tv_TransactionDetails_route);
            if (activity.route != null && !activity.route.isEmpty()) {
                route.setText(activity.route);
                Log.d("TripDets", "Route set from route field: " + activity.route);
            } else if (activity.subtitle != null && activity.subtitle.contains("•")) {
                // Fallback to subtitle parsing
                String[] parts = activity.subtitle.split("•");
                if (parts.length >= 2) {
                    String routeFromSubtitle = parts[1].trim();
                    route.setText(routeFromSubtitle);
                    Log.d("TripDets", "Route set from subtitle: " + routeFromSubtitle);
                } else {
                    route.setText("Route not specified");
                    Log.d("TripDets", "Subtitle parsing failed for route");
                }
            } else {
                route.setText("Route not specified");
                Log.d("TripDets", "No route data available");
            }

            // Set driver name - use the new field
            TextView driver = getView().findViewById(R.id.tv_TransactionDetails_driver);
            if (activity.driver_name != null && !activity.driver_name.isEmpty()) {
                driver.setText(activity.driver_name);
                Log.d("TripDets", "Driver set: " + activity.driver_name);
            } else {
                driver.setText("Not Available");
                Log.d("TripDets", "No driver data available");
            }

            // Set conductor name - use the new field
            TextView conductor = getView().findViewById(R.id.tv_TransactionDetails_conductor);
            if (activity.conductor_name != null && !activity.conductor_name.isEmpty()) {
                conductor.setText(activity.conductor_name);
                Log.d("TripDets", "Conductor set: " + activity.conductor_name);
            } else {
                conductor.setText("Not Available");
                Log.d("TripDets", "No conductor data available");
            }

            // Set user category - use the new field
            TextView category = getView().findViewById(R.id.tv_transactionDetails_category);
            if (activity.user_category != null && !activity.user_category.isEmpty()) {
                category.setText(activity.user_category);
                Log.d("TripDets", "Category set: " + activity.user_category);
            } else {
                category.setText("Regular");
                Log.d("TripDets", "No category data available, using default");
            }

            // Use transaction code if available, otherwise generate one
            TextView transactionNumber = getView().findViewById(R.id.tv_transactionDetails_number);
            String transactionId = activity.transaction_code != null ?
                    activity.transaction_code : "TRIP" + System.currentTimeMillis();
            transactionNumber.setText("Transaction Number: " + transactionId);
            Log.d("TripDets", "Transaction number set: " + transactionId);

            // ADD RATING CARD CLICK LISTENER - FIXED VERSION
            View reviewCard = getView().findViewById(R.id.card_review_btn);
            if (reviewCard != null) {
                reviewCard.setOnClickListener(v -> {
                    Log.d("TripDets", "Rating card clicked!");

                    // Get current commuter data from session
                    String currentCommuterId = getCurrentCommuterId();
                    String currentCommuterName = getCurrentCommuterName();

                    Log.d("TripDets", "Current Commuter ID: " + currentCommuterId);
                    Log.d("TripDets", "Current Commuter Name: " + currentCommuterName);

                    // Navigate to RatingFragment with complete trip data
                    RatingFragment ratingFragment = RatingFragment.newInstance(
                            activity.transaction_code != null ? activity.transaction_code : "Unknown",
                            activity.driver_name != null ? activity.driver_name : "Not Available",
                            activity.conductor_name != null ? activity.conductor_name : "Not Available",
                            activity.plate_number != null ? activity.plate_number : "Not Available",
                            activity.route != null ? activity.route : "Not Available",
                            currentCommuterId,
                            currentCommuterName
                    );

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, ratingFragment)
                            .addToBackStack("rating_fragment")
                            .commit();

                    Log.d("TripDets", "Navigating to RatingFragment with complete data");
                });

                // Also make sure the individual stars are NOT clickable in the transaction details
                // Only the entire card should be clickable to navigate to the rating page
                int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
                for (int starId : starIds) {
                    ImageView star = getView().findViewById(starId);
                    if (star != null) {
                        star.setClickable(false); // Disable individual star clicks in details page
                        star.setFocusable(false); // Also disable focus for better UX
                    }
                }
            } else {
                Log.e("TripDets", "Review card not found!");
            }

        } catch (Exception e) {
            Log.e("TripDets", "Error in populateData: " + e.getMessage());
            e.printStackTrace();
            showDefaultValues();
        }
    }

    /**
     * Get current commuter ID from UserSessionManager
     */
    private String getCurrentCommuterId() {
        try {
            if (sessionManager != null && sessionManager.isLoggedIn()) {
                String userData = sessionManager.getUserData();
                if (userData != null) {
                    Gson gson = new Gson();
                    APIResponse.User user = gson.fromJson(userData, APIResponse.User.class);
                    if (user != null && user.commuterId != null) {
                        return user.commuterId;
                    }
                }
                // Fallback to username if commuterId is not available
                String username = sessionManager.getUsername();
                if (username != null) {
                    return username; // Using username as fallback identifier
                }
            }
        } catch (Exception e) {
            Log.e("TripDets", "Error getting commuter ID: " + e.getMessage());
        }
        return "Unknown"; // Safe fallback
    }

    /**
     * Get current commuter name from UserSessionManager
     */
    private String getCurrentCommuterName() {
        try {
            if (sessionManager != null && sessionManager.isLoggedIn()) {
                String userData = sessionManager.getUserData();
                if (userData != null) {
                    Gson gson = new Gson();
                    APIResponse.User user = gson.fromJson(userData, APIResponse.User.class);
                    if (user != null) {
                        if (user.fullName != null && !user.fullName.isEmpty()) {
                            return user.fullName;
                        } else if (user.firstName != null && user.lastName != null) {
                            return user.firstName + " " + user.lastName;
                        }
                    }
                }
                // Fallback to username
                String username = sessionManager.getUsername();
                if (username != null) {
                    return username;
                }
            }
        } catch (Exception e) {
            Log.e("TripDets", "Error getting commuter name: " + e.getMessage());
        }
        return "Commuter"; // Safe fallback
    }

    private void showDefaultValues() {
        try {
            TextView mainTitle = getView().findViewById(R.id.tv_transactionDetails_mainTitle);
            TextView amount = getView().findViewById(R.id.tv_transactionDetails_amount);
            TextView datetime = getView().findViewById(R.id.tv_transactionDetails_datetime);
            TextView route = getView().findViewById(R.id.tv_TransactionDetails_route);
            TextView driver = getView().findViewById(R.id.tv_TransactionDetails_driver);
            TextView conductor = getView().findViewById(R.id.tv_TransactionDetails_conductor);
            TextView category = getView().findViewById(R.id.tv_transactionDetails_category);
            TextView transactionNumber = getView().findViewById(R.id.tv_transactionDetails_number);

            mainTitle.setText("Bus Trip");
            amount.setText(activity.amount != null ? activity.amount : "-0.00");
            datetime.setText("Unknown date");
            route.setText("Route not specified");
            driver.setText("Not Available");
            conductor.setText("Not Available");
            category.setText("Regular");
            transactionNumber.setText("Transaction Number: Unknown");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}