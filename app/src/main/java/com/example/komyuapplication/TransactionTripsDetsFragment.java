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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionTripsDetsFragment extends Fragment {

    private static final String ARG_ACTIVITY = "activity_data";
    private APIResponse.RecentActivity activity;
    private UserSessionManager sessionManager;
    private AuthRepository authRepository;
    private boolean isAlreadyReviewed = false;
    private APIResponse.RatingResponse reviewDetails;

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
        authRepository = new AuthRepository(requireContext());
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
        checkIfAlreadyReviewed();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the rating status when returning from RatingFragment
        Log.d("TripDets", "Fragment resumed - refreshing rating status");
        checkIfAlreadyReviewed();
    }

    private void setupBackButton() {
        ImageView btnBack = getView().findViewById(R.id.btn_back_transactionDetails);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void checkIfAlreadyReviewed() {
        // Check if we have the is_reviewed field from the API
        if (activity.is_reviewed == 1) {
            Log.d("TripDets", "Trip is already reviewed (from API is_reviewed field)");
            isAlreadyReviewed = true;
            populateData();
            return;
        }

        // Only make API call if we don't have the field or it's 0
        String currentCommuterId = getCurrentCommuterId();
        String transactionId = activity.transaction_code != null ? activity.transaction_code : "Unknown";

        if (currentCommuterId.equals("Unknown") || transactionId.equals("Unknown")) {
            Log.w("TripDets", "Cannot check rating status - missing commuter ID or transaction ID");
            populateData();
            return;
        }

        Log.d("TripDets", "Checking if trip is already reviewed via API...");
        authRepository.checkTripRatingStatus(transactionId, currentCommuterId, new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success == 1) {
                    isAlreadyReviewed = true;
                    reviewDetails = response.body().rating_data;
                    Log.d("TripDets", "Trip is already reviewed via API");
                } else {
                    isAlreadyReviewed = false;
                    reviewDetails = null;
                    Log.d("TripDets", "Trip is not reviewed yet");
                }
                populateData();
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Log.e("TripDets", "Error checking rating status: " + t.getMessage());
                isAlreadyReviewed = false;
                reviewDetails = null;
                populateData();
            }
        });
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
            Log.d("TripDets", "User Category: " + activity.user_category); // This should now show "Senior"
            Log.d("TripDets", "Transaction Code: " + activity.transaction_code);
            Log.d("TripDets", "Already Reviewed: " + isAlreadyReviewed);
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

            // Parse and format date
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

            // Set route
            TextView route = getView().findViewById(R.id.tv_TransactionDetails_route);
            if (activity.route != null && !activity.route.isEmpty()) {
                route.setText(activity.route);
                Log.d("TripDets", "Route set from route field: " + activity.route);
            } else if (activity.subtitle != null && activity.subtitle.contains("•")) {
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

            // Set driver name
            TextView driver = getView().findViewById(R.id.tv_TransactionDetails_driver);
            if (activity.driver_name != null && !activity.driver_name.isEmpty()) {
                driver.setText(activity.driver_name);
                Log.d("TripDets", "Driver set: " + activity.driver_name);
            } else {
                driver.setText("Not Available");
                Log.d("TripDets", "No driver data available");
            }

            // Set conductor name
            TextView conductor = getView().findViewById(R.id.tv_TransactionDetails_conductor);
            if (activity.conductor_name != null && !activity.conductor_name.isEmpty()) {
                conductor.setText(activity.conductor_name);
                Log.d("TripDets", "Conductor set: " + activity.conductor_name);
            } else {
                conductor.setText("Not Available");
                Log.d("TripDets", "No conductor data available");
            }

            // Set user category
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

            // Configure the rating section based on review status
            configureRatingSection();

        } catch (Exception e) {
            Log.e("TripDets", "Error in populateData: " + e.getMessage());
            e.printStackTrace();
            showDefaultValues();
        }
    }
//ill edit this
    private void configureRatingSection() {
        View reviewCard = getView().findViewById(R.id.card_review_btn);
        TextView detailsText = getView().findViewById(R.id.tv_details);

        if (isAlreadyReviewed) {
            // Trip is already reviewed - show colored stars only
            configureReviewedView();
        } else {
            // Trip is not reviewed - show rating form
            configureRatingFormView();
        }
    }

    private void configureReviewedView() {
        View reviewCard = getView().findViewById(R.id.card_review_btn);
        TextView detailsText = getView().findViewById(R.id.tv_details);

        // Update the text to indicate it's reviewed
        detailsText.setText("Thank you for your feedback!");

        // Make card non-clickable and grey it out
        reviewCard.setClickable(false);
        reviewCard.setEnabled(false);
        reviewCard.setAlpha(0.6f);

        // Display the actual ratings if we have them, otherwise show default
        if (reviewDetails != null && reviewDetails.overall_rating > 0) {
            displayReviewStars();
        } else {
            // If no review details or no rating values, just show filled stars
            displayDefaultReviewedStars();
        }
    }

    private void configureRatingFormView() {
        View reviewCard = getView().findViewById(R.id.card_review_btn);
        TextView detailsText = getView().findViewById(R.id.tv_details);

        // Set default text for rating form
        detailsText.setText("Help us improve your experience by providing feedback");

        // Make card clickable for rating
        reviewCard.setClickable(true);
        reviewCard.setEnabled(true);
        reviewCard.setAlpha(1.0f);

        // Set click listener to navigate to RatingFragment
        reviewCard.setOnClickListener(v -> {
            Log.d("TripDets", "Rating card clicked - navigating to RatingFragment");

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

        // Reset stars to outline for unreviewed trips
        resetStarsToOutline();
    }

    private void displayReviewStars() {
        // Display the actual ratings from the review
        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
        int overallRating = reviewDetails.overall_rating;

        // Fill stars based on overall rating
        for (int i = 0; i < starIds.length; i++) {
            ImageView star = getView().findViewById(starIds[i]);
            if (star != null) {
                if (i < overallRating) {
                    star.setImageResource(R.drawable.star_filled_icon);
                } else {
                    star.setImageResource(R.drawable.star_outline_icon);
                }
                // Make sure stars are not clickable
                star.setClickable(false);
                star.setFocusable(false);
            }
        }

        Log.d("TripDets", "Displayed review stars with rating: " + overallRating + "/5");
    }

    private void displayDefaultReviewedStars() {
        // If we don't have specific rating details, just show all stars filled
        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};

        for (int starId : starIds) {
            ImageView star = getView().findViewById(starId);
            if (star != null) {
                star.setImageResource(R.drawable.star_filled_icon);
                star.setClickable(false);
                star.setFocusable(false);
            }
        }

        Log.d("TripDets", "Displayed default reviewed stars");
    }

    private void resetStarsToOutline() {
        // Reset all stars to outline for unreviewed trips
        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
        for (int starId : starIds) {
            ImageView star = getView().findViewById(starId);
            if (star != null) {
                star.setImageResource(R.drawable.star_outline_icon);
                star.setClickable(false);
                star.setFocusable(false);
            }
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
                    return username;
                }
            }
        } catch (Exception e) {
            Log.e("TripDets", "Error getting commuter ID: " + e.getMessage());
        }
        return "Unknown";
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
        return "Commuter";
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