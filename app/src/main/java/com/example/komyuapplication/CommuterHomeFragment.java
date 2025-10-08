package com.example.komyuapplication;

import android.content.pm.PackageManager;
import android.os.Bundle;
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
                    Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(requireContext(), "Data updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void fetchRecentActivity() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) return;

        APIService api = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = api.getRecentActivity(sessionManager.getUsername());

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (getActivity() != null && response.isSuccessful() && response.body() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            APIResponse apiResponse = response.body();
                            if (apiResponse.success == 1 && apiResponse.recent_activity != null) {
                                updateRecentActivityUI(apiResponse.recent_activity);
                            } else {
                                loadSampleRecentActivity();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
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
                    .addOnCanceledListener(() -> Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(requireContext(), "QR scanner not available: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Invalid QR code format", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Invalid QR code format", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "Error processing QR code", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showBusPaymentDialog(APIResponse.BusQRData busData) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate fare based on user category
        double fare = 35.00; // Default regular fare
        if (busData.fares != null) {
            String userCategory = currentUser.categories != null ? currentUser.categories : "Regular";
            switch (userCategory.toLowerCase()) {
                case "student":
                case "senior citizen":
                case "pwd":
                    fare = busData.fares.student != null ? busData.fares.student : 28.00;
                    break;
                default:
                    fare = busData.fares.regular != null ? busData.fares.regular : 35.00;
                    break;
            }
        }

        double finalFare = fare;

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

        // Set data
        tvBusNumber.setText(busData.bus != null ? busData.bus : "N/A");
        tvDriver.setText(busData.driver_name != null ? busData.driver_name : "Not assigned");
        tvConductor.setText(busData.conductor_name != null ? busData.conductor_name : "Not assigned");
        tvFare.setText(String.format("₱%.2f", finalFare));
        tvUserCategory.setText(currentUser.categories != null ? currentUser.categories : "Regular");
        tvCurrentBalance.setText(String.format("₱%.2f", currentUser.pointsBalance));

        // Calculate and display new balance
        double newBalance = currentUser.pointsBalance - finalFare;
        tvNewBalance.setText(String.format("₱%.2f", newBalance));

        // Set text color based on balance
        if (newBalance < 0) {
            tvNewBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.RedLogout));
        } else {
            tvNewBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.greenKomyu));
        }

        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCancelable(false);

        // Set button listeners
        btnPay.setOnClickListener(v -> {
            if (currentUser.pointsBalance >= finalFare) {
                processBusPayment(busData, finalFare, dialog);
            } else {
                Toast.makeText(requireContext(), "Insufficient balance!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void processBusPayment(APIResponse.BusQRData busData, double fare, AlertDialog dialog) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        dialog.dismiss();
        Toast.makeText(requireContext(), "Processing payment...", Toast.LENGTH_SHORT).show();

        APIService api = APIClient.getInstance().create(APIService.class);
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
                if (response.isSuccessful() && response.body() != null) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse.success == 1) {
                        // Payment successful - show success dialog with trip details
                        if (apiResponse.data != null) {
                            showPaymentSuccessDialog(apiResponse.data);
                        } else {
                            Toast.makeText(requireContext(), "Payment successful!", Toast.LENGTH_SHORT).show();
                        }
                        // Refresh user data
                        fetchUserProfileFromServer();
                    } else {
                        Toast.makeText(requireContext(), "Payment failed: " + apiResponse.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Payment failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Payment of ₱" + amount + " to " + terminalId + " confirmed", Toast.LENGTH_LONG).show();
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
                Toast.makeText(requireContext(), "Invalid QR code format", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error processing QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPaymentConfirmation(String terminalId, String amount) {
        Toast.makeText(requireContext(), "Confirm payment of ₱" + amount + " to Terminal " + terminalId, Toast.LENGTH_LONG).show();
    }
}