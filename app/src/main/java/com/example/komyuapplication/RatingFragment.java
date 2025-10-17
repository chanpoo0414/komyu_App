package com.example.komyuapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingFragment extends Fragment {

    private static final String ARG_TRANSACTION_ID = "transaction_id";
    private static final String ARG_DRIVER_NAME = "driver_name";
    private static final String ARG_CONDUCTOR_NAME = "conductor_name";
    private static final String ARG_BUS_NUMBER = "bus_number";
    private static final String ARG_ROUTE = "route";
    private static final String ARG_COMMUTER_ID = "commuter_id";
    private static final String ARG_COMMUTER_NAME = "commuter_name";

    private String transactionId;
    private String driverName;
    private String conductorName;
    private String busNumber;
    private String route;
    private String commuterId;
    private String commuterName;

    // Rating variables
    private int selectedOverallRating = 0;
    private int selectedDriverRating = 0;
    private int selectedConductorRating = 0;
    private String selectedReportType = "Performance";
    private String userExperience = "";

    // UI Components
    private View submitButton;
    private TextView submitButtonText;

    // Prevent duplicate submissions
    private boolean isPostSubmissionInProgress = false;

    public static RatingFragment newInstance(String transactionId, String driverName, String conductorName,
                                             String busNumber, String route, String commuterId, String commuterName) {
        RatingFragment fragment = new RatingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRANSACTION_ID, transactionId);
        args.putString(ARG_DRIVER_NAME, driverName);
        args.putString(ARG_CONDUCTOR_NAME, conductorName);
        args.putString(ARG_BUS_NUMBER, busNumber);
        args.putString(ARG_ROUTE, route);
        args.putString(ARG_COMMUTER_ID, commuterId);
        args.putString(ARG_COMMUTER_NAME, commuterName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionId = getArguments().getString(ARG_TRANSACTION_ID);
            driverName = getArguments().getString(ARG_DRIVER_NAME);
            conductorName = getArguments().getString(ARG_CONDUCTOR_NAME);
            busNumber = getArguments().getString(ARG_BUS_NUMBER);
            route = getArguments().getString(ARG_ROUTE);
            commuterId = getArguments().getString(ARG_COMMUTER_ID);
            commuterName = getArguments().getString(ARG_COMMUTER_NAME);

            Log.d("RatingFragment", "Received data:");
            Log.d("RatingFragment", "Transaction ID: " + transactionId);
            Log.d("RatingFragment", "Driver: " + driverName);
            Log.d("RatingFragment", "Conductor: " + conductorName);
            Log.d("RatingFragment", "Bus: " + busNumber);
            Log.d("RatingFragment", "Route: " + route);
            Log.d("RatingFragment", "Commuter ID: " + commuterId);
            Log.d("RatingFragment", "Commuter Name: " + commuterName);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
        setupOverallRatingStars();
        setupDriverRatingStars();
        setupConductorRatingStars();
        setupReportTypeSelection();
        setupExperienceField();
        setupSubmitButton();
        populateDriverConductorInfo();
    }

    private void setupBackButton() {
        ImageView btnBack = getView().findViewById(R.id.btn_back_rating);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupOverallRatingStars() {
        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};

        for (int i = 0; i < starIds.length; i++) {
            final int rating = i + 1;
            ImageView star = getView().findViewById(starIds[i]);

            star.setOnClickListener(v -> {
                selectedOverallRating = rating;
                updateStarDisplay(starIds, selectedOverallRating);
                Log.d("RatingFragment", "Overall rating set to: " + selectedOverallRating);
            });
        }
    }

    private void setupDriverRatingStars() {
        int[] driverStarIds = {
                R.id.iv_driver_star1, R.id.iv_driver_star2, R.id.iv_driver_star3,
                R.id.iv_driver_star4, R.id.iv_driver_star5
        };

        for (int i = 0; i < driverStarIds.length; i++) {
            final int rating = i + 1;
            ImageView star = getView().findViewById(driverStarIds[i]);

            star.setOnClickListener(v -> {
                selectedDriverRating = rating;
                updateStarDisplay(driverStarIds, selectedDriverRating);
                Log.d("RatingFragment", "Driver rating set to: " + selectedDriverRating);
            });
        }
    }

    private void setupConductorRatingStars() {
        int[] conductorStarIds = {
                R.id.iv_conductor_star1, R.id.iv_conductor_star2, R.id.iv_conductor_star3,
                R.id.iv_conductor_star4, R.id.iv_conductor_star5
        };

        for (int i = 0; i < conductorStarIds.length; i++) {
            final int rating = i + 1;
            ImageView star = getView().findViewById(conductorStarIds[i]);

            star.setOnClickListener(v -> {
                selectedConductorRating = rating;
                updateStarDisplay(conductorStarIds, selectedConductorRating);
                Log.d("RatingFragment", "Conductor rating set to: " + selectedConductorRating);
            });
        }
    }

    private void updateStarDisplay(int[] starIds, int selectedRating) {
        for (int i = 0; i < starIds.length; i++) {
            ImageView star = getView().findViewById(starIds[i]);
            if (i < selectedRating) {
                star.setImageResource(R.drawable.star_filled_icon);
            } else {
                star.setImageResource(R.drawable.star_outline_icon);
            }
        }
    }

    private void setupReportTypeSelection() {
        int[] typeIds = {R.id.card_performance, R.id.card_behavior, R.id.card_others};
        String[] types = {"Performance", "Behavior", "Others"};

        for (int i = 0; i < typeIds.length; i++) {
            final String type = types[i];
            View card = getView().findViewById(typeIds[i]);

            card.setOnClickListener(v -> {
                selectedReportType = type;
                updateReportTypeSelection(typeIds);
                Log.d("RatingFragment", "Report type set to: " + selectedReportType);
            });
        }
    }

    private void updateReportTypeSelection(int[] typeIds) {
        // Reset all cards
        getView().findViewById(R.id.card_performance).setBackgroundResource(R.color.whiteKomyu);
        getView().findViewById(R.id.card_behavior).setBackgroundResource(R.color.whiteKomyu);
        getView().findViewById(R.id.card_others).setBackgroundResource(R.color.whiteKomyu);

        // Highlight selected card
        switch (selectedReportType) {
            case "Performance":
                getView().findViewById(R.id.card_performance).setBackgroundResource(R.drawable.card_border_enhanced);
                break;
            case "Behavior":
                getView().findViewById(R.id.card_behavior).setBackgroundResource(R.drawable.card_border_enhanced);
                break;
            case "Others":
                getView().findViewById(R.id.card_others).setBackgroundResource(R.drawable.card_border_enhanced);
                break;
        }
    }

    private void setupExperienceField() {
        EditText etExperience = getView().findViewById(R.id.et_experience);
        etExperience.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                userExperience = etExperience.getText().toString().trim();
                Log.d("RatingFragment", "Experience text updated: " + userExperience);
            }
        });
    }

    private void setupSubmitButton() {
        submitButton = getView().findViewById(R.id.card_submit_review);

        // Find the TextView inside the submit button card
        View submitButtonChild = ((ViewGroup) submitButton).getChildAt(0);
        if (submitButtonChild instanceof TextView) {
            submitButtonText = (TextView) submitButtonChild;
        }

        submitButton.setOnClickListener(v -> submitReview());
    }

    private void populateDriverConductorInfo() {
        TextView driverInfo = getView().findViewById(R.id.tv_driver_name);
        TextView conductorInfo = getView().findViewById(R.id.tv_conductor_name);

        driverInfo.setText(driverName != null ? driverName : "Not specified");
        conductorInfo.setText(conductorName != null ? conductorName : "Not specified");

        Log.d("RatingFragment", "Driver info set: " + driverName);
        Log.d("RatingFragment", "Conductor info set: " + conductorName);
    }

    private void submitReview() {
        // Validate ratings
        if (selectedOverallRating == 0) {
            Toast.makeText(getContext(), "Please provide an overall ride rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDriverRating == 0) {
            Toast.makeText(getContext(), "Please rate the driver", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedConductorRating == 0) {
            Toast.makeText(getContext(), "Please rate the conductor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the experience text if any
        EditText etExperience = getView().findViewById(R.id.et_experience);
        userExperience = etExperience.getText().toString().trim();

        // Submit to API
        submitRatingToAPI();
    }

    private void submitRatingToAPI() {
        // Show loading state
        setSubmitButtonState(false, "Submitting...");

        // Log the data being sent
        logRatingData();

        // Create API service instance
        APIService apiService = APIClient.getInstance().create(APIService.class);

        // Try GET method first (Hostinger compatible)
        Call<APIResponse> call = apiService.submitRatingGet(
                commuterId,
                commuterName,
                transactionId,
                driverName,
                conductorName,
                busNumber,
                route,
                selectedOverallRating,
                selectedDriverRating,
                selectedConductorRating,
                selectedReportType,
                userExperience
        );

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                Log.d("RatingFragment", "GET Response code: " + response.code());

                if (response.isSuccessful()) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        if (apiResponse.success == 1) {
                            // Success with GET method
                            handleSubmissionSuccess(apiResponse);
                        } else {
                            // GET returned but with error, try POST
                            Log.d("RatingFragment", "GET returned error: " + apiResponse.message);
                            tryPostSubmission();
                        }
                    } else {
                        // Response body is null, try POST
                        Log.d("RatingFragment", "GET response body is null");
                        tryPostSubmission();
                    }
                } else {
                    // HTTP error (like 500), try POST
                    Log.e("RatingFragment", "GET HTTP Error: " + response.code());
                    tryPostSubmission();
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Log.e("RatingFragment", "GET Network error: " + t.getMessage());
                tryPostSubmission();
            }
        });
    }

    private void tryPostSubmission() {
        Log.d("RatingFragment", "Attempting POST submission as fallback");

        // Prevent infinite recursion by checking if we're already in fallback
        if (isPostSubmissionInProgress) {
            Log.e("RatingFragment", "POST submission already in progress, skipping");
            return;
        }

        isPostSubmissionInProgress = true;

        APIService apiService = APIClient.getInstance().create(APIService.class);

        Call<APIResponse> call = apiService.submitRating(
                commuterId,
                commuterName,
                transactionId,
                driverName,
                conductorName,
                busNumber,
                route,
                selectedOverallRating,
                selectedDriverRating,
                selectedConductorRating,
                selectedReportType,
                userExperience
        );

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                isPostSubmissionInProgress = false;
                Log.d("RatingFragment", "POST Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse.success == 1) {
                        handleSubmissionSuccess(apiResponse);
                    } else {
                        handleSubmissionError("Failed to submit rating: " +
                                (apiResponse.message != null ? apiResponse.message : "Unknown error"));
                    }
                } else {
                    String errorMsg = "Server error";
                    if (!response.isSuccessful()) {
                        errorMsg = "Server error (HTTP " + response.code() + ")";
                    } else if (response.body() == null) {
                        errorMsg = "Server returned empty response";
                    }
                    handleSubmissionError(errorMsg + ". Please try again later.");
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                isPostSubmissionInProgress = false;
                handleSubmissionError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleSubmissionSuccess(APIResponse apiResponse) {
        setSubmitButtonState(true, "Submit Rating");

        String successMessage = String.format(
                String.format("✅ Rating Submitted Successfully!\n\nOverall Ride: %%d/5 ⭐\nDriver (%%s): %%d/5 ⭐\nConductor (%%s): %%d/5 ⭐\nReport Type: %%s\nThank you for your feedback!"),
                selectedOverallRating,
                driverName != null ? driverName : "Not specified",
                selectedDriverRating,
                conductorName != null ? conductorName : "Not specified",
                selectedConductorRating,
                selectedReportType
        );

        Toast.makeText(getContext(), successMessage, Toast.LENGTH_LONG).show();

        // Log success
        if (apiResponse.rating_data != null) {
            Log.d("RatingFragment", "Rating submitted successfully. Report ID: " + apiResponse.rating_data.report_id);
        } else {
            Log.d("RatingFragment", "Rating submitted successfully.");
        }

        // Navigate back after successful submission
        requireActivity().onBackPressed();
    }

    private void handleSubmissionError(String error) {
        setSubmitButtonState(true, "Submit Rating");

        Log.e("RatingFragment", "Submission error: " + error);
        Toast.makeText(getContext(), "❌ " + error, Toast.LENGTH_LONG).show();

        // Show retry option for network errors
        if (error.contains("Network") || error.contains("connection")) {
            showRetryDialog(error);
        }
    }

    private void setSubmitButtonState(boolean enabled, String text) {
        if (submitButton != null) {
            submitButton.setEnabled(enabled);
            submitButton.setClickable(enabled);
            submitButton.setAlpha(enabled ? 1.0f : 0.6f);
        }

        if (submitButtonText != null) {
            submitButtonText.setText(text);
        }
    }

    private void showRetryDialog(String error) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Submission Failed")
                .setMessage(error + "\n\nWould you like to try again?")
                .setPositiveButton("Try Again", (dialog, which) -> submitRatingToAPI())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Log rating data for debugging
    private void logRatingData() {
        Log.d("RatingFragment", "=== RATING DATA TO SUBMIT ===");
        Log.d("RatingFragment", "Commuter: " + commuterName + " (" + commuterId + ")");
        Log.d("RatingFragment", "Transaction: " + transactionId);
        Log.d("RatingFragment", "Bus: " + busNumber + ", Route: " + route);
        Log.d("RatingFragment", "Driver: " + driverName + " - Rating: " + selectedDriverRating);
        Log.d("RatingFragment", "Conductor: " + conductorName + " - Rating: " + selectedConductorRating);
        Log.d("RatingFragment", "Overall Rating: " + selectedOverallRating);
        Log.d("RatingFragment", "Report Type: " + selectedReportType);
        Log.d("RatingFragment", "Comments: " + (userExperience.isEmpty() ? "None" : userExperience));
        Log.d("RatingFragment", "=== END RATING DATA ===");
    }
}