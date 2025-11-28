package com.example.komyuapplication;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionTopupDetsFragment extends Fragment {

    private static final String ARG_ACTIVITY = "activity_data";
    private APIResponse.RecentActivity activity;

    public static TransactionTopupDetsFragment newInstance(APIResponse.RecentActivity activity) {
        TransactionTopupDetsFragment fragment = new TransactionTopupDetsFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_topup_dets, container, false);
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
            Log.e("TopupDets", "Activity is NULL!");
            return;
        }

        // DEBUG: Log all received data
        Log.d("TopupDets", "=== DEBUG DATA RECEIVED ===");
        Log.d("TopupDets", "Type: " + activity.type);
        Log.d("TopupDets", "Title: " + activity.title);
        Log.d("TopupDets", "Subtitle: " + activity.subtitle);
        Log.d("TopupDets", "Amount: " + activity.amount);
        Log.d("TopupDets", "Date: " + activity.date);
        Log.d("TopupDets", "Counter Location: " + activity.counter_location);
        Log.d("TopupDets", "Transaction Code: " + activity.transaction_code);
        Log.d("TopupDets", "=== END DEBUG ===");

        try {
            // Set main title and icon
            TextView mainTitle = getView().findViewById(R.id.tv_transactionDetails_mainTitle);
            ImageView icon = getView().findViewById(R.id.icon_transactionDetails);

            mainTitle.setText(activity.title != null ? activity.title : "Points Top-up");
            icon.setImageResource(R.drawable.plus_points_icon);

            // Set amount (this is working)
            TextView amount = getView().findViewById(R.id.tv_transactionDetails_amount);
            amount.setText(activity.amount);

            // Set amount color
            if (activity.amount.startsWith("+")) {
                amount.setTextColor(getResources().getColor(R.color.greenKomyu));
            } else {
                amount.setTextColor(getResources().getColor(R.color.RedLogout));
            }

//edit ko ni

            // FIX DATE & TIME - Handle potential parsing issues
            TextView datetime = getView().findViewById(R.id.tv_transactionDetails_datetime);
            if (activity.date != null && !activity.date.isEmpty()) {
                try {
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

                    Date activityDate = apiFormat.parse(activity.date);
                    String formattedDate = displayFormat.format(activityDate);
                    datetime.setText(formattedDate);
                    Log.d("TopupDets", "Date parsed successfully: " + formattedDate);
                } catch (Exception e) {
                    Log.e("TopupDets", "Date parsing error: " + e.getMessage());
                    datetime.setText("Date not available");
                }
            } else {
                datetime.setText("Date not available");
                Log.d("TopupDets", "Date field is null or empty");
            }

            // FIX COUNTER LOCATION
            TextView counter = getView().findViewById(R.id.tv_transactionDetails_counter);
            if (activity.counter_location != null && !activity.counter_location.isEmpty()) {
                counter.setText(activity.counter_location);
                Log.d("TopupDets", "Counter set from counter_location: " + activity.counter_location);
            } else if (activity.subtitle != null && activity.subtitle.contains("•")) {
                // Fallback to subtitle parsing
                String[] parts = activity.subtitle.split("•");
                if (parts.length > 0) {
                    String counterFromSubtitle = parts[0].trim();
                    counter.setText(counterFromSubtitle);
                    Log.d("TopupDets", "Counter set from subtitle: " + counterFromSubtitle);
                } else {
                    counter.setText("Counter not specified");
                    Log.d("TopupDets", "Subtitle parsing failed");
                }
            } else {
                counter.setText("Counter not specified");
                Log.d("TopupDets", "No counter data available");
            }

            // Use transaction code
            TextView transactionNumber = getView().findViewById(R.id.tv_transactionDetails_number);
            String transactionId = activity.transaction_code != null ?
                    activity.transaction_code : "TXN" + System.currentTimeMillis();
            transactionNumber.setText("Transaction Number: " + transactionId);

        } catch (Exception e) {
            Log.e("TopupDets", "Error in populateData: " + e.getMessage());
            e.printStackTrace();
            showDefaultValues();
        }
    }

    private void showDefaultValues() {
        try {
            TextView mainTitle = getView().findViewById(R.id.tv_transactionDetails_mainTitle);
            TextView amount = getView().findViewById(R.id.tv_transactionDetails_amount);
            TextView datetime = getView().findViewById(R.id.tv_transactionDetails_datetime);
            TextView counter = getView().findViewById(R.id.tv_transactionDetails_counter);
            TextView transactionNumber = getView().findViewById(R.id.tv_transactionDetails_number);

            mainTitle.setText("Points Top-up");
            amount.setText(activity.amount != null ? activity.amount : "+0.00");
            datetime.setText("Date not available");
            counter.setText("Counter not specified");
            transactionNumber.setText("Transaction Number: Unknown");
        } catch (Exception e) {
            Log.e("TopupDets", "Error in showDefaultValues: " + e.getMessage());
        }
    }
}