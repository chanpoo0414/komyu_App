package com.example.komyuapplication;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommuterHomeFragment extends Fragment {

    // Camera permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchQRScanner();
                } else {
                    CustomToast.showError(requireActivity(), "Camera permission is required to scan QR codes");
                }
            });

    private UserSessionManager sessionManager;
    private APIResponse.User currentUser;
    private TextView tvFullName, tvStudentId, tvUserRole, tvWalletAmount;
    private TextView tvTotalPointsSpentValue, tvTripCountValue;
    private RecyclerView rvRecentActivity;
    private RecentActivityAdapter recentActivityAdapter;
    private SwipeRefreshLayout swipeRefreshLayout; // Add this

    public CommuterHomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commuter_home, container, false);

        sessionManager = new UserSessionManager(requireContext());
        initializeViews(root);
        setupRecyclerView(root);
        setupSwipeRefresh(root); // Add this
        loadUserData();

        View scanBtn = root.findViewById(R.id.btn_scan_pay);
        scanBtn.setOnClickListener(v -> checkCameraPermission());

        View terminalsBtn = root.findViewById(R.id.btn_terminals);
        terminalsBtn.setOnClickListener(v -> showTerminalsDialog());

        View topUpBtn = root.findViewById(R.id.btn_topup);
        topUpBtn.setOnClickListener(v -> navigateToTopUp());

        View buyLoadBtn = root.findViewById(R.id.btn_buy_load);
        buyLoadBtn.setOnClickListener(v -> navigateToTopUp());

        TextView tvSeeAllRecent = root.findViewById(R.id.tv_see_all);
        tvSeeAllRecent.setOnClickListener(v -> navigateToCommuterLogs());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Optional: Refresh data when fragment becomes visible again
        loadUserData();
    }

    private void initializeViews(View root) {
        tvFullName = root.findViewById(R.id.tvFullName);
        tvStudentId = root.findViewById(R.id.tvStudentId);
        tvUserRole = root.findViewById(R.id.tvUserRole);
        tvWalletAmount = root.findViewById(R.id.tv_wallet_amount);
        tvTotalPointsSpentValue = root.findViewById(R.id.tv_total_points_value);
        tvTripCountValue = root.findViewById(R.id.tv_trip_count_value);
        rvRecentActivity = root.findViewById(R.id.recycler_recent_activity);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout); // Add this
    }

    private void setupSwipeRefresh(View root) {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // This method will be called when user pulls down to refresh
                refreshAllData();
            }
        });

        // Set the refresh indicator colors (optional)
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupRecyclerView(View root) {
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        List<RecentActivityAdapter.Item> emptyData = new ArrayList<>();
        recentActivityAdapter = new RecentActivityAdapter(emptyData);
        rvRecentActivity.setAdapter(recentActivityAdapter);
    }

    // New method to refresh all data
    private void refreshAllData() {
        if (sessionManager.isLoggedIn()) {
            fetchUserProfileFromServer();
        } else {
            // If not logged in, stop refresh animation
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void navigateToTopUp() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CommuterTopupFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToCommuterLogs() {
        if (getActivity() instanceof CommuterDashboard) {
            ((CommuterDashboard) getActivity()).setBottomNavSelected(R.id.nav_logs);
        } else {
            // Fallback: manually navigate to logs fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterLogsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadUserData() {
        String userJson = sessionManager.getUserData();
        if (userJson != null) {
            try {
                Gson gson = new Gson();
                APIResponse response = gson.fromJson(userJson, APIResponse.class);
                if (response != null && response.user != null) {
                    currentUser = response.user;
                    updateUIWithUserData();
                    fetchRecentActivity();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sessionManager.isLoggedIn()) {
            fetchUserProfileFromServer();
        }
    }

    // Modified to handle refresh completion
    private void fetchUserProfileFromServer() {
        AuthRepository authRepo = new AuthRepository();
        authRepo.fetchUserProfile(sessionManager.getUsername(), new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                // Stop refresh animation
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    currentUser = response.body().user;

                    Gson gson = new Gson();
                    sessionManager.saveUserData(gson.toJson(response.body()));

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUIWithUserData();
                                fetchRecentActivity();
                                CustomToast.showSuccess(getActivity(), "Data updated");
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CustomToast.showError(getActivity(), "Failed to load user data");
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                // Stop refresh animation
                swipeRefreshLayout.setRefreshing(false);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CustomToast.showError(getActivity(), "Network error: " + t.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void fetchRecentActivity() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) return;

        Log.d("RecentActivity", "Fetching recent activity for: " + sessionManager.getUsername());

        APIService api = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = api.getRecentActivity(sessionManager.getUsername());

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                Log.d("RecentActivity", "Response code: " + response.code());

                if (getActivity() != null && response.isSuccessful() && response.body() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            APIResponse apiResponse = response.body();
                            Log.d("RecentActivity", "Success: " + apiResponse.success);
                            Log.d("RecentActivity", "Message: " + apiResponse.message);

                            if (apiResponse.success == 1 && apiResponse.recent_activity != null) {
                                Log.d("RecentActivity", "Activities count: " + apiResponse.recent_activity.size());
                                updateRecentActivityUI(apiResponse.recent_activity);
                            } else {
                                Log.d("RecentActivity", "No activities or failed");
                                loadSampleRecentActivity();
                            }
                        }
                    });
                } else {
                    Log.d("RecentActivity", "Response not successful or null");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadSampleRecentActivity();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                Log.d("RecentActivity", "Network failure: " + t.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadSampleRecentActivity();
                        }
                    });
                }
            }
        });
    }

    private void updateRecentActivityUI(List<APIResponse.RecentActivity> recentActivities) {
        List<RecentActivityAdapter.Item> data = new ArrayList<>();

        for (APIResponse.RecentActivity activity : recentActivities) {
            RecentActivityAdapter.Item item = new RecentActivityAdapter.Item();
            item.title = activity.title;
            item.subtitle = activity.subtitle;
            item.amount = activity.amount;

            // Set icon based on the type from server
            if (activity.icon != null) {
                switch (activity.icon) {
                    case "bus":
                        item.iconRes = R.drawable.bus_icon;
                        break;
                    case "points":
                    default:
                        item.iconRes = R.drawable.plus_points_icon;
                        break;
                }
            } else {
                // Fallback: determine icon based on amount
                if (activity.amount.startsWith("-")) {
                    item.iconRes = R.drawable.bus_icon;
                } else {
                    item.iconRes = R.drawable.plus_points_icon;
                }
            }

            data.add(item);
        }

        if (data.size() > 3) {
            data = data.subList(0, 3);
        }

        // This will handle both empty and populated states
        recentActivityAdapter.setData(data);
    }

    private void loadSampleRecentActivity() {
        // Create empty list to show "No activities" message
        List<RecentActivityAdapter.Item> emptyData = new ArrayList<>();
        recentActivityAdapter.setData(emptyData);
    }

    private void updateUIWithUserData() {
        if (currentUser != null) {
            Log.d("UserDebug", "User Category: " + currentUser.categories);
            Log.d("UserDebug", "Points Balance: " + currentUser.pointsBalance);
            if (currentUser.fullName != null && !currentUser.fullName.isEmpty()) {
                tvFullName.setText(currentUser.fullName);
            } else {
                String fullName = currentUser.firstName +
                        (currentUser.middleName != null && !currentUser.middleName.isEmpty() ? " " + currentUser.middleName : "") +
                        " " + currentUser.lastName;
                tvFullName.setText(fullName);
            }

            if (currentUser.commuterId != null) {
                tvStudentId.setText(currentUser.commuterId);
            }

            if (currentUser.categories != null) {
                tvUserRole.setText(currentUser.categories);
            }

            // Update wallet balance - this will now reflect the latest balance after admin approval
            tvWalletAmount.setText(String.format("%.2f", currentUser.pointsBalance));

            // Update travel overview section
            if (tvTotalPointsSpentValue != null) {
                tvTotalPointsSpentValue.setText(String.format("%.2f", currentUser.totalPointsSpent));
            }

            if (tvTripCountValue != null) {
                tvTripCountValue.setText(String.valueOf(currentUser.tripCount));
            }

        } else {
            tvFullName.setText("Loading...");
            tvStudentId.setText("000-000-00");
            tvUserRole.setText("Commuter");
            tvWalletAmount.setText("0.00");

            if (tvTotalPointsSpentValue != null) {
                tvTotalPointsSpentValue.setText("0.00");
            }

            if (tvTripCountValue != null) {
                tvTripCountValue.setText("0");
            }
        }
    }

    private void showTerminalsDialog() {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_terminals, null);

        // Initialize views
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_terminals_ok);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        // Set button listener - just close the dialog, no payment action
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchQRScanner();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void launchQRScanner() {
        try {
            GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
                    .enableAutoZoom()
                    .build();

            GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(requireContext(), options);

            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        String scannedData = barcode.getRawValue();
                        if (scannedData != null) handleScannedQRCode(scannedData);
                    })
                    .addOnCanceledListener(() -> CustomToast.showInfo(getActivity(), "Scan cancelled"))
                    .addOnFailureListener(e -> CustomToast.showError(getActivity(), "Scan failed: " + e.getMessage()));

        } catch (Exception e) {
            CustomToast.showError(getActivity(), "QR scanner not available: " + e.getMessage());
        }
    }

    private void handleScannedQRCode(String qrData) {
        try {
            // Try to parse as JSON (new bus QR format)
            Gson gson = new Gson();
            APIResponse.BusQRData busData = gson.fromJson(qrData, APIResponse.BusQRData.class);

            if (busData != null && busData.type != null && busData.type.equals("bus_qr")) {
                // This is a bus QR code
                showBusPaymentDialog(busData);
            } else {
                // Try old terminal format
                String[] parts = qrData.split(":");
                if (parts.length == 2) {
                    String terminalId = parts[0];
                    String amount = parts[1];
                    showTerminalPaymentConfirmation(terminalId, amount);
                } else {
                    CustomToast.showError(requireActivity(), "Invalid QR code format");
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, try old format
            try {
                String[] parts = qrData.split(":");
                if (parts.length == 2) {
                    String terminalId = parts[0];
                    String amount = parts[1];
                    showTerminalPaymentConfirmation(terminalId, amount);
                } else {
                    CustomToast.showError(requireActivity(), "Invalid QR code format");
                }
            } catch (Exception ex) {
                CustomToast.showError(requireActivity(), "Error processing QR code");
            }
        }
    }


    private void showBusPaymentDialog(APIResponse.BusQRData busData) {
        if (currentUser == null) {
            CustomToast.showError(requireActivity(), "User data not loaded");
            return;
        }

        // Get user category
        String userCategory = currentUser.categories != null ? currentUser.categories : "Regular";

        // DEBUG: Log the QR data to see what fares are available
        Log.d("QRDebug", "QR Bus Data: " + new Gson().toJson(busData));

        // Get base regular fare - FIXED LOGIC
        double regularFare = 35.00; // Default regular fare

        // Check if QR has fare data and use it, otherwise use defaults
        if (busData.fares != null) {
            if (busData.fares.regular != null) {
                regularFare = busData.fares.regular;
            }
            Log.d("QRDebug", "QR Fares - Regular: " + busData.fares.regular +
                    ", Student: " + busData.fares.student +
                    ", Senior: " + busData.fares.senior_citizen +
                    ", PWD: " + busData.fares.pwd);
        } else {
            Log.d("QRDebug", "No fare data in QR, using defaults");
        }

        // Calculate discounted fare based on user category - SIMPLIFIED AND FIXED
        double finalFare = regularFare; // Default to regular fare

        // Apply 20% discount for students, PWD, and senior citizens
        if (userCategory.equalsIgnoreCase("student") ||
                userCategory.equalsIgnoreCase("pwd") ||
                userCategory.equalsIgnoreCase("senior")) { // CHANGED: "senior citizen" to "senior"

            // Try to get discounted fare from QR data first
            if (busData.fares != null) {
                if (userCategory.equalsIgnoreCase("student") && busData.fares.student != null) {
                    finalFare = busData.fares.student;
                } else if (userCategory.equalsIgnoreCase("senior") && busData.fares.senior_citizen != null) {
                    finalFare = busData.fares.senior_citizen;
                } else if (userCategory.equalsIgnoreCase("pwd") && busData.fares.pwd != null) {
                    finalFare = busData.fares.pwd;
                } else {
                    // Fallback: calculate 20% discount
                    finalFare = regularFare * 0.8;
                }
            } else {
                // If no QR fare data, always apply 20% discount
                finalFare = regularFare * 0.8;
            }
        }

        double finalFareValue = finalFare;

        // DEBUG: Log the fare calculation
        Log.d("FareDebug", "User: " + userCategory +
                ", Regular Fare: " + regularFare +
                ", Final Fare: " + finalFareValue);

        // Create custom dialog
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_bus_payment, null);

        // Initialize views
        TextView tvBusNumber = dialogView.findViewById(R.id.tv_bus_number);
        TextView tvDriver = dialogView.findViewById(R.id.tv_driver);
        TextView tvConductor = dialogView.findViewById(R.id.tv_conductor);
        TextView tvFare = dialogView.findViewById(R.id.tv_fare);
        TextView tvUserCategory = dialogView.findViewById(R.id.tv_user_category);
        TextView tvCurrentBalance = dialogView.findViewById(R.id.tv_current_balance);
        TextView tvNewBalance = dialogView.findViewById(R.id.tv_new_balance);
        MaterialButton btnPay = dialogView.findViewById(R.id.btn_pay);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // ✅ ADD THIS: Initialize discount info and HIDE it by default
        TextView tvDiscountInfo = dialogView.findViewById(R.id.tv_discount_info);
        if (tvDiscountInfo != null) {
            tvDiscountInfo.setVisibility(View.GONE); // Hide by default
        }

        // Set data
        tvBusNumber.setText(busData.bus != null ? busData.bus : "N/A");
        tvDriver.setText(busData.driver_name != null ? busData.driver_name : "Not assigned");
        tvConductor.setText(busData.conductor_name != null ? busData.conductor_name : "Not assigned");
        tvFare.setText(String.format("₱%.2f", finalFareValue));
        tvUserCategory.setText(userCategory);
        tvCurrentBalance.setText(String.format("₱%.2f", currentUser.pointsBalance));

        // Calculate and display new balance
        double newBalance = currentUser.pointsBalance - finalFareValue;
        tvNewBalance.setText(String.format("₱%.2f (pending)", newBalance));


        // Set text color based on balance
        if (newBalance < 0) {
            tvNewBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.RedLogout));
            btnPay.setEnabled(false); // Disable pay button if insufficient balance
        } else {
            tvNewBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.greenKomyu));
            btnPay.setEnabled(true);
        }

        // Show discount information if applicable
        if (finalFareValue < regularFare) {
            // ✅ NOW tvDiscountInfo is already initialized above
            if (tvDiscountInfo != null) {
                tvDiscountInfo.setVisibility(View.VISIBLE); // SHOW only when discount applies
                tvDiscountInfo.setText(String.format("20%% discount applied! (Original: ₱%.2f)", regularFare));
            }
        }

        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCancelable(false);

        // Set button listeners
        btnPay.setOnClickListener(v -> {
            if (currentUser.pointsBalance >= finalFareValue) {
                processBusPayment(busData, finalFareValue, userCategory, dialog);
            } else {
                CustomToast.showError(requireActivity(), "Insufficient balance!");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    //    sadas
    private void processBusPayment(APIResponse.BusQRData busData, double fare, String userCategory, AlertDialog dialog) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        dialog.dismiss();
        CustomToast.show(getActivity(), "Processing payment...");

        APIService api = APIClient.getInstance().create(APIService.class);

        // TEMPORARY: Add debug logging in Android
        Log.d("PaymentDebug", "Sending payment request:");
        Log.d("PaymentDebug", "Username: " + sessionManager.getUsername());
        Log.d("PaymentDebug", "Bus: " + (busData.bus != null ? busData.bus : ""));
        Log.d("PaymentDebug", "Fare: " + fare);
        Log.d("PaymentDebug", "User Category: " + userCategory);

        Call<APIResponse> call = api.processBusPayment(
                sessionManager.getUsername(),
                busData.bus != null ? busData.bus : "",
                busData.driver_id != null ? busData.driver_id : "",
                busData.conductor_id != null ? busData.conductor_id : "",
                fare
        );

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                Log.d("PaymentDebug", "Response received: " + response.code());

                // Handle both 200 and 500 responses if they contain success data
                if (response.isSuccessful() || response.body() != null) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        Log.d("PaymentDebug", "Response success: " + apiResponse.success);
                        Log.d("PaymentDebug", "Response message: " + apiResponse.message);

                        if (apiResponse.success == 1) {
                            // Payment successful
                            if (apiResponse.data != null) {
                                Log.d("PaymentDebug", "Fare paid: " + apiResponse.data.fare_paid);
                                Log.d("PaymentDebug", "User category: " + apiResponse.data.user_category);
                                showPaymentSuccessDialog(apiResponse.data);
                            } else {
                                CustomToast.showSuccess(getActivity(), "QR passenger added successfully!");
                            }
                            // Refresh user data
                            fetchUserProfileFromServer();
                        } else {
                            CustomToast.showError(getActivity(), "Payment failed: " + apiResponse.message);
                        }
                    } else {
                        CustomToast.showError(getActivity(), "Payment failed: Invalid response format");
                    }
                } else {
                    CustomToast.showError(getActivity(), "Payment failed: No response from server");
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                Log.d("PaymentDebug", "Network failure: " + t.getMessage());
                CustomToast.show(getActivity(), "Network error: " + t.getMessage());
            }
        });
    }

    private void showPaymentSuccessDialog(APIResponse.PaymentData paymentData) {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_payment_success, null);

        // Initialize views
        TextView tvFare = dialogView.findViewById(R.id.tv_success_fare);
        TextView tvBus = dialogView.findViewById(R.id.tv_success_bus);
        TextView tvDriver = dialogView.findViewById(R.id.tv_success_driver);
        TextView tvConductor = dialogView.findViewById(R.id.tv_success_conductor);
        TextView tvBalance = dialogView.findViewById(R.id.tv_success_balance);
        TextView tvTripId = dialogView.findViewById(R.id.tv_success_trip_id);
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_success_ok);


        // Set data
        tvFare.setText(String.format("₱%.2f", paymentData.fare_paid));
        tvBus.setText(paymentData.bus_number != null ? paymentData.bus_number : "N/A");
        tvDriver.setText(paymentData.driver_name != null ? paymentData.driver_name : "Not assigned");
        tvConductor.setText(paymentData.conductor_name != null ? paymentData.conductor_name : "Not assigned");
        tvBalance.setText(String.format("₱%.2f", paymentData.new_balance));
        tvTripId.setText(String.valueOf(paymentData.trip_id));

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Set button listener
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showTerminalPaymentConfirmation(String terminalId, String amount) {
        // Keep your existing terminal payment logic
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Payment")
                .setMessage("Pay ₱" + amount + " to " + terminalId + "?")
                .setPositiveButton("Pay", (dialog, which) -> {
                    // Your existing terminal payment logic
                    CustomToast.showSuccess(requireActivity(), "Payment of ₱" + amount + " to " + terminalId + " confirmed");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment(String qrData) {
        try {
            String[] parts = qrData.split(":");
            if (parts.length == 2) {
                String terminalId = parts[0];
                String amount = parts[1];
                showPaymentConfirmation(terminalId, amount);
            } else {
                CustomToast.showError(requireActivity(), "Invalid QR code format");
            }
        } catch (Exception e) {
            CustomToast.showError(requireActivity(), "Invalid QR code format");
        }
    }

    private void showPaymentConfirmation(String terminalId, String amount) {
        CustomToast.showInfo(requireActivity(), "Confirm payment of ₱" + amount + " to Terminal " + terminalId);    }
}