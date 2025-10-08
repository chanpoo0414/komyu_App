package com.example.komyuapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
    private UserSessionManager sessionManager;

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

        sessionManager = new UserSessionManager(requireContext());

        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            Log.d("CommuterLogs", "Username from arguments: " + username);
        }

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
        clearContainer();

        if (recentActivities.isEmpty()) {
            showEmptyState("No transactions yet");
            return;
        }

        sortActivitiesByDate();
        Map<String, List<APIResponse.RecentActivity>> groupedActivities = groupActivitiesByDate();
        List<String> sectionOrder = getSortedSectionKeys(groupedActivities);

        for (String section : sectionOrder) {
            if (groupedActivities.containsKey(section)) {
                addDateHeader(section);
                List<APIResponse.RecentActivity> activities = groupedActivities.get(section);
                for (APIResponse.RecentActivity activity : activities) {
                    addActivityCard(activity);
                }
            }
        }
    }

    private List<String> getSortedSectionKeys(Map<String, List<APIResponse.RecentActivity>> groupedActivities) {
        List<String> keys = new ArrayList<>(groupedActivities.keySet());

        keys.sort((key1, key2) -> {
            if (key1.equals("Today")) return -1;
            if (key2.equals("Today")) return 1;
            if (key1.equals("Yesterday")) return -1;
            if (key2.equals("Yesterday")) return 1;

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

    private void sortActivitiesByDate() {
        recentActivities.sort((activity1, activity2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date1 = format.parse(activity1.date);
                Date date2 = format.parse(activity2.date);
                return date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });
    }

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

                String dateKey;
                if (displayDate.equals(todayStr)) {
                    dateKey = "Today";
                } else if (displayDate.equals(yesterdayStr)) {
                    dateKey = "Yesterday";
                } else {
                    dateKey = displayDate;
                }

                if (!grouped.containsKey(dateKey)) {
                    grouped.put(dateKey, new ArrayList<>());
                }
                grouped.get(dateKey).add(activity);

            } catch (Exception e) {
                Log.e("CommuterLogs", "Date parsing error: " + e.getMessage());
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
        CardView cardView = (CardView) LayoutInflater.from(getContext())
                .inflate(R.layout.item_transaction_card, containerLogs, false);

        configureCardView(cardView, activity);

        // Make card clickable
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setOnClickListener(v -> {
            handleCardClick(activity);
        });

        containerLogs.addView(cardView);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
        params.bottomMargin = dpToPx(12);
        cardView.setLayoutParams(params);
    }

    private void handleCardClick(APIResponse.RecentActivity activity) {
        Log.d("CommuterLogs", "=== CLICK DEBUG - Topup Details ===");
        Log.d("CommuterLogs", "Type: " + activity.type);
        Log.d("CommuterLogs", "Title: " + activity.title);
        Log.d("CommuterLogs", "Amount: " + activity.amount);
        Log.d("CommuterLogs", "Date: " + activity.date);
        Log.d("CommuterLogs", "Counter Location: " + activity.counter_location);
        Log.d("CommuterLogs", "Subtitle: " + activity.subtitle);
        Log.d("CommuterLogs", "Transaction Code: " + activity.transaction_code);
        Log.d("CommuterLogs", "=== END CLICK DEBUG ===");

        if (activity.type != null && activity.type.equals("topup")) {
            navigateToTopupDetails(activity);
        } else if (activity.type != null && activity.type.equals("bus_trip")) {
            navigateToTripDetails(activity);
        } else {
            if (activity.amount.startsWith("+")) {
                navigateToTopupDetails(activity);
            } else {
                navigateToTripDetails(activity);
            }
        }
    }

    private void navigateToTopupDetails(APIResponse.RecentActivity activity) {
        TransactionTopupDetsFragment fragment = TransactionTopupDetsFragment.newInstance(activity);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("topup_details");
        transaction.commit();
    }

    private void navigateToTripDetails(APIResponse.RecentActivity activity) {
        TransactionTripsDetsFragment fragment = TransactionTripsDetsFragment.newInstance(activity);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("trip_details");
        transaction.commit();
    }

    private void configureCardView(CardView cardView, APIResponse.RecentActivity activity) {
        TextView title = cardView.findViewById(R.id.tv_title);
        TextView amount = cardView.findViewById(R.id.tv_amount);
        ImageView icon = cardView.findViewById(R.id.img_icon);

        title.setText(activity.title);
        amount.setText(activity.amount);

        // Set icon based on activity type or icon field
        if (activity.type != null) {
            if (activity.type.equals("topup")) {
                icon.setImageResource(R.drawable.plus_points_icon);
            } else if (activity.type.equals("bus_trip")) {
                icon.setImageResource(R.drawable.bus_icon);
            } else {
                icon.setImageResource(R.drawable.simple_activity_icon);
            }
        } else if (activity.icon != null) {
            if (activity.icon.equals("points")) {
                icon.setImageResource(R.drawable.plus_points_icon);
            } else if (activity.icon.equals("bus")) {
                icon.setImageResource(R.drawable.bus_icon);
            } else {
                icon.setImageResource(R.drawable.simple_activity_icon);
            }
        } else {
            // Fallback: detect by amount prefix
            if (activity.amount.startsWith("+")) {
                icon.setImageResource(R.drawable.plus_points_icon);
            } else {
                icon.setImageResource(R.drawable.bus_icon);
            }
        }

        // Set amount color
        if (activity.amount.startsWith("+")) {
            amount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            amount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void clearContainer() {
        int childCount = containerLogs.getChildCount();
        for (int i = childCount - 1; i >= 2; i--) {
            containerLogs.removeViewAt(i);
        }
    }

    private void showEmptyState(String message) {
        clearContainer();

        View emptyView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_empty_logs, containerLogs, false);

        TextView emptyText = emptyView.findViewById(R.id.tv_empty_message);
        emptyText.setText(message);

        containerLogs.addView(emptyView);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}