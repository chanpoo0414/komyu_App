package com.example.komyuapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class CommuterLogsFragment extends Fragment {

    private static final String ARG_USERNAME = "username";
    private String username;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout containerLogs;
    private TextView tvDate;

    private List<APIResponse.RecentActivity> recentActivities = new ArrayList<>();
    private AuthRepository authRepository;
    private UserSessionManager sessionManager; // Add this

    public CommuterLogsFragment() {
        // Required empty public constructor
    }

    public static CommuterLogsFragment newInstance(String username) {
        CommuterLogsFragment fragment = new CommuterLogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize session manager
        sessionManager = new UserSessionManager(requireContext());

        // Get username from arguments first
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            Log.d("CommuterLogs", "Username from arguments: " + username);
        }

        // If still null, get from session manager
        if (username == null || username.isEmpty()) {
            username = sessionManager.getUsername();
            Log.d("CommuterLogs", "Username from session: " + username);
        }

        authRepository = new AuthRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commuter_logs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupSwipeRefresh();

        // Final check for username
        if (username == null || username.isEmpty()) {
            username = sessionManager.getUsername();
            Log.d("CommuterLogs", "Final username check: " + username);
        }

        if (username == null || username.isEmpty()) {
            Log.e("CommuterLogs", "CRITICAL: Username is still null or empty!");
            showEmptyState("Please login again");
        } else {
            loadRecentActivity();
        }
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh_commuterLogs);
        containerLogs = view.findViewById(R.id.container_commuterLogs);
        tvDate = view.findViewById(R.id.tv_logs_date);

        // Hide template sections initially
        view.findViewById(R.id.tv_logs_today).setVisibility(View.GONE);
        view.findViewById(R.id.tv_logs_yesterday).setVisibility(View.GONE);
        view.findViewById(R.id.tv_logs_past).setVisibility(View.GONE);
        view.findViewById(R.id.card_transaction_trip).setVisibility(View.GONE);
        view.findViewById(R.id.card_transaction_points).setVisibility(View.GONE);
        view.findViewById(R.id.card_transaction_yesterday).setVisibility(View.GONE);
        view.findViewById(R.id.card_transaction_past).setVisibility(View.GONE);

        // Set current date
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText("As of " + currentDate);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRecentActivity();
        });
    }

    private void loadRecentActivity() {
        if (username == null || username.isEmpty()) {
            Log.e("CommuterLogs", "Username is null or empty - cannot load data");
            swipeRefreshLayout.setRefreshing(false);
            showEmptyState("Please login again");
            return;
        }

        Log.d("CommuterLogs", "Loading recent activity for: " + username);

        // Use the getRecentActivity API call directly instead of fetchUserProfile
        APIService apiService = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = apiService.getRecentActivity(username);

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    APIResponse apiResponse = response.body();
                    Log.d("CommuterLogs", "API Response - Success: " + apiResponse.success);

                    if (apiResponse.success == 1 && apiResponse.recent_activity != null) {
                        recentActivities = apiResponse.recent_activity;
                        Log.d("CommuterLogs", "Received " + recentActivities.size() + " activities");
                        updateUI();
                    } else {
                        Log.e("CommuterLogs", "No recent activity data in response");
                        showEmptyState("No transactions found");
                    }
                } else {
                    Log.e("CommuterLogs", "API call failed: " + response.message());
                    showEmptyState("Failed to load data");
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("CommuterLogs", "Network error: " + t.getMessage());
                showEmptyState("Network error: " + t.getMessage());
            }
        });
    }

    private void updateUI() {
        // Clear container except the header and date
        clearContainer();

        if (recentActivities.isEmpty()) {
            showEmptyState("No transactions yet");
            return;
        }

        // Sort activities by date (newest first)
        sortActivitiesByDate();

        // Group activities by date
        Map<String, List<APIResponse.RecentActivity>> groupedActivities = groupActivitiesByDate();

        // Define the order we want sections to appear
        List<String> sectionOrder = getSortedSectionKeys(groupedActivities);

        // Create sections in the correct order
        for (String section : sectionOrder) {
            if (groupedActivities.containsKey(section)) {
                // Add date header
                addDateHeader(section);

                // Add activity cards for this section (already sorted newest first)
                List<APIResponse.RecentActivity> activities = groupedActivities.get(section);
                for (APIResponse.RecentActivity activity : activities) {
                    addActivityCard(activity);
                }
            }
        }
    }

    private List<String> getSortedSectionKeys(Map<String, List<APIResponse.RecentActivity>> groupedActivities) {
        List<String> keys = new ArrayList<>(groupedActivities.keySet());

        // Custom comparator to sort: Today -> Yesterday -> then actual dates (newest first)
        keys.sort((key1, key2) -> {
            if (key1.equals("Today")) return -1;
            if (key2.equals("Today")) return 1;
            if (key1.equals("Yesterday")) return -1;
            if (key2.equals("Yesterday")) return 1;

            // For actual dates, sort in descending order (newest first)
            try {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date1 = format.parse(key1);
                Date date2 = format.parse(key2);
                return date2.compareTo(date1);
            } catch (Exception e) {
                return key2.compareTo(key1);
            }
        });

        return keys;
    }

    // Add this new method to sort activities by date (newest first)
    private void sortActivitiesByDate() {
        recentActivities.sort((activity1, activity2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date1 = format.parse(activity1.date);
                Date date2 = format.parse(activity2.date);
                return date2.compareTo(date1); // Descending order (newest first)
            } catch (Exception e) {
                return 0;
            }
        });
    }

    // Update the groupActivitiesByDate method to maintain order within groups
    // Update the groupActivitiesByDate method to show individual dates for older history
    private Map<String, List<APIResponse.RecentActivity>> groupActivitiesByDate() {
        Map<String, List<APIResponse.RecentActivity>> grouped = new HashMap<>();
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Date today = new Date();
        Date yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);

        String todayStr = displayFormat.format(today);
        String yesterdayStr = displayFormat.format(yesterday);

        for (APIResponse.RecentActivity activity : recentActivities) {
            try {
                Date activityDate = apiFormat.parse(activity.date);
                String displayDate = displayFormat.format(activityDate);

                // Convert to relative dates
                String dateKey;
                if (displayDate.equals(todayStr)) {
                    dateKey = "Today";
                } else if (displayDate.equals(yesterdayStr)) {
                    dateKey = "Yesterday";
                } else {
                    dateKey = displayDate; // Use actual date for older history
                }

                if (!grouped.containsKey(dateKey)) {
                    grouped.put(dateKey, new ArrayList<>());
                }
                grouped.get(dateKey).add(activity);

            } catch (Exception e) {
                Log.e("CommuterLogs", "Date parsing error: " + e.getMessage());
                // Use original date as fallback
                if (!grouped.containsKey(activity.date)) {
                    grouped.put(activity.date, new ArrayList<>());
                }
                grouped.get(activity.date).add(activity);
            }
        }

        return grouped;
    }

    private void addDateHeader(String dateText) {
        TextView dateHeader = new TextView(getContext());
        dateHeader.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dateHeader.setText(dateText);
        dateHeader.setTextSize(16);
        dateHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        dateHeader.setTextColor(getResources().getColor(android.R.color.black));
        dateHeader.setPadding(0, dpToPx(12), 0, dpToPx(8));

        containerLogs.addView(dateHeader);
    }

    private void addActivityCard(APIResponse.RecentActivity activity) {
        // Make sure you have item_transaction_card.xml layout
        CardView cardView = (CardView) LayoutInflater.from(getContext())
                .inflate(R.layout.item_transaction_card, containerLogs, false);

        // Configure card based on activity type
        configureCardView(cardView, activity);

        containerLogs.addView(cardView);

        // Add bottom margin
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
        params.bottomMargin = dpToPx(12);
        cardView.setLayoutParams(params);
    }

    private void configureCardView(CardView cardView, APIResponse.RecentActivity activity) {
        TextView title = cardView.findViewById(R.id.tv_transaction_title);
        TextView amount = cardView.findViewById(R.id.tv_transaction_amount);

        title.setText(activity.title);
        amount.setText(activity.amount);

        // Set amount color based on type
        if (activity.amount.startsWith("+")) {
            amount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            amount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void clearContainer() {
        // Remove all views except the first 2 (header and date)
        int childCount = containerLogs.getChildCount();
        for (int i = childCount - 1; i >= 2; i--) {
            containerLogs.removeViewAt(i);
        }
    }

    private void showEmptyState(String message) {
        clearContainer();

        TextView emptyText = new TextView(getContext());
        emptyText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        emptyText.setText(message);
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        emptyText.setPadding(0, dpToPx(50), 0, 0);
        emptyText.setGravity(android.view.Gravity.CENTER);

        // Make it full width
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) emptyText.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        emptyText.setLayoutParams(params);

        containerLogs.addView(emptyText);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}