package com.example.komyuapplication;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.List;

public class CommuterHomeFragment extends Fragment {

    // Camera permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, launch QR scanner
                    launchQRScanner();
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                }
            });

    public CommuterHomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commuter_home, container, false);

        // Recycler view for recent activity
        RecyclerView rv = root.findViewById(R.id.recycler_recent_activity);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        // Build sample data
        List<RecentActivityAdapter.Item> data = new ArrayList<>();
        RecentActivityAdapter.Item it1 = new RecentActivityAdapter.Item();
        it1.title = "Trip to SM City";
        it1.subtitle = "15 Sep 2025 · 11:11 AM";
        it1.amount = "-35.00";
        data.add(it1);

        RecentActivityAdapter.Item it2 = new RecentActivityAdapter.Item();
        it2.title = "Top-up";
        it2.subtitle = "14 Sep 2025 · 09:00 AM";
        it2.amount = "+100.00";
        data.add(it2);

        RecentActivityAdapter adapter = new RecentActivityAdapter(data);
        rv.setAdapter(adapter);

        // Scan & Pay CardView - Check if it's a CardView or Button
        View scanBtn = root.findViewById(R.id.btn_scan_pay);
        scanBtn.setOnClickListener(v -> {
            // Check camera permission before launching scanner
            checkCameraPermission();
        });

        // Terminals Button - FIXED: Use View instead of specific type
        View terminalsBtn = root.findViewById(R.id.btn_terminals);
        terminalsBtn.setOnClickListener(v -> {
            showTerminalsDialog();
        });

        // Buy Load Button - Use View instead of MaterialCardView
        View topUpBtn = root.findViewById(R.id.btn_topup);
        topUpBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterTopupFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Buy Load Button - also navigates to TopUpFragment
        View buyLoadBtn = root.findViewById(R.id.btn_buy_load);
        buyLoadBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterTopupFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    // Show terminals selection dialog
    private void showTerminalsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Counter to Pay")
                .setItems(new String[]{"Counter 1 - Terminal 1 • SM Seaside",
                                "Counter 2 - Terminal 2 • SM City"},
                        (dialog, which) -> {
                            String selectedTerminal = which == 0 ?
                                    "Counter 1 - Terminal 1 • SM Seaside" :
                                    "Counter 2 - Terminal 2 • SM City";
                            Toast.makeText(requireContext(), "Selected: " + selectedTerminal, Toast.LENGTH_SHORT).show();
                            // Here you would handle the terminal selection for payment
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Check camera permission
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, launch scanner
            launchQRScanner();
        } else {
            // Request camera permission
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    // Method to launch QR scanner using Google's ML Kit
    private void launchQRScanner() {
        try {
            GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC)
                    .enableAutoZoom()
                    .build();

            GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(requireContext(), options);

            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        // QR code scanned successfully
                        String scannedData = barcode.getRawValue();
                        if (scannedData != null) {
                            handleScannedQRCode(scannedData);
                        }
                    })
                    .addOnCanceledListener(() -> {
                        // Scan cancelled
                        Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Scan failed
                        Toast.makeText(requireContext(), "Scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "QR scanner not available: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Process the scanned QR code
    private void handleScannedQRCode(String qrData) {
        Toast.makeText(requireContext(), "QR Code Scanned: " + qrData, Toast.LENGTH_LONG).show();

        // Example QR processing - you can customize this
        processPayment(qrData);
    }

    // Example payment processing method
    private void processPayment(String qrData) {
        try {
            // Parse QR data (example format: "terminal_123:35.00")
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

    // Show payment confirmation
    private void showPaymentConfirmation(String terminalId, String amount) {
        Toast.makeText(requireContext(),
                "Confirm payment of ₱" + amount + " to Terminal " + terminalId,
                Toast.LENGTH_LONG).show();

        // TODO: Implement actual payment confirmation dialog or activity
    }
}