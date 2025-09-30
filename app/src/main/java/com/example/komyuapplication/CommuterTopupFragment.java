package com.example.komyuapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommuterTopupFragment extends Fragment {

    private TextInputEditText inputID, inputCustomAmount;
    private MaterialButton btnProceed;
    private TextView tvFullName, tvStudentId, tvUserRole;

    // Points cards
    private List<MaterialCardView> pointsCards = new ArrayList<>();
    private MaterialCardView selectedPointsCard = null;

    // Counters
    private MaterialCardView cardCounter1, cardCounter2, selectedCounterCard = null;

    private boolean isIDValid = false;
    private boolean hasAmountSelected = false;
    private boolean hasCounterSelected = false;

    private View rootView; // root view reference

    // User session management
    private UserSessionManager sessionManager;
    private APIResponse.User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_commuter_topup, container, false);

        // Initialize session manager
        sessionManager = new UserSessionManager(requireContext());

        // --- Init views ---
        inputID = rootView.findViewById(R.id.input_ID);
        inputCustomAmount = rootView.findViewById(R.id.input_custom_amount);
        btnProceed = rootView.findViewById(R.id.btn_proceed);

        // User profile views
        tvFullName = rootView.findViewById(R.id.tvFullName);
        tvStudentId = rootView.findViewById(R.id.tvStudentId);
        tvUserRole = rootView.findViewById(R.id.tvUserRole);

        // Add all point cards
        pointsCards.add(rootView.findViewById(R.id.card_points_20));
        pointsCards.add(rootView.findViewById(R.id.card_points_40));
        pointsCards.add(rootView.findViewById(R.id.card_points_80));
        pointsCards.add(rootView.findViewById(R.id.card_points_100));
        pointsCards.add(rootView.findViewById(R.id.card_points_250));
        pointsCards.add(rootView.findViewById(R.id.card_points_500));
        pointsCards.add(rootView.findViewById(R.id.card_points_800));
        pointsCards.add(rootView.findViewById(R.id.card_points_1000));
        pointsCards.add(rootView.findViewById(R.id.card_points_1500));

        cardCounter1 = rootView.findViewById(R.id.card_counter1);
        cardCounter2 = rootView.findViewById(R.id.card_counter2);

        // Add wallet click listener to navigate back to home
        TextView tvWallet = rootView.findViewById(R.id.btn_wallet);
        if (tvWallet != null) {
            tvWallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View CommuterHomeFragment) {
                    navigateToHomeFragment();
                }
            });
        }

        // Load user data and auto-fill ID
        loadUserData();

        // --- ID Validation ---
        inputID.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                isIDValid = s.toString().matches("\\d{3}-\\d{3}-\\d{2}"); // simple regex for ID
                updateProceedButtonState();
            }
        });

        // --- Custom Amount ---
        inputCustomAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    unselectAllPoints();
                    hasAmountSelected = true;
                } else {
                    // If custom amount is empty, check if points card is selected
                    hasAmountSelected = selectedPointsCard != null;
                }
                updateProceedButtonState();
            }
        });

        // --- Points Cards Selection ---
        for (MaterialCardView card : pointsCards) {
            card.setOnClickListener(v -> {
                inputCustomAmount.setText(""); // clear custom input
                selectPointsCard(card);
                hasAmountSelected = true;
                updateProceedButtonState();
            });
        }

        // --- Counter Selection ---
        cardCounter1.setOnClickListener(v -> selectCounter(cardCounter1));
        cardCounter2.setOnClickListener(v -> selectCounter(cardCounter2));

        // --- Proceed Button ---
        btnProceed.setOnClickListener(v -> showConfirmationModal());

        return rootView;
    }

    private void navigateToHomeFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new CommuterHomeFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadUserData() {
        // Try to get user data from session first
        String userJson = sessionManager.getUserData();
        if (userJson != null) {
            try {
                Gson gson = new Gson();
                APIResponse response = gson.fromJson(userJson, APIResponse.class);
                if (response != null && response.user != null) {
                    currentUser = response.user;
                    updateUIWithUserData();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If no session data, fetch from server
        if (sessionManager.isLoggedIn()) {
            fetchUserProfileFromServer();
        }
    }

    private void fetchUserProfileFromServer() {
        AuthRepository authRepo = new AuthRepository();
        authRepo.fetchUserProfile(sessionManager.getUsername(), new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    currentUser = response.body().user;

                    // Save to session
                    Gson gson = new Gson();
                    sessionManager.saveUserData(gson.toJson(response.body()));

                    // Update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUIWithUserData();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                // Handle failure silently - user can still proceed with manual input
            }
        });
    }

    private void updateUIWithUserData() {
        if (currentUser != null) {
            // Update header section
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
                // Auto-fill the ID input field
                inputID.setText(currentUser.commuterId);
                // Trigger validation
                isIDValid = true;
            }

            if (currentUser.categories != null) {
                tvUserRole.setText(currentUser.categories);
            }

            updateProceedButtonState();
        }
    }

    private void selectPointsCard(MaterialCardView card) {
        unselectAllPoints();
        selectedPointsCard = card;
        card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.greenKomyu));
        card.setStrokeWidth(4);
    }

    private void unselectAllPoints() {
        for (MaterialCardView c : pointsCards) {
            c.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.lightblack));
            c.setStrokeWidth(1);
        }
        selectedPointsCard = null;
    }

    private void selectCounter(MaterialCardView card) {
        if (selectedCounterCard != null) {
            selectedCounterCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.lightblack));
            selectedCounterCard.setStrokeWidth(1);
        }
        selectedCounterCard = card;
        selectedCounterCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.greenKomyu));
        selectedCounterCard.setStrokeWidth(4);
        hasCounterSelected = true;
        updateProceedButtonState();
    }

    private void updateProceedButtonState() {
        // FIXED: Only require ID, amount (either points card OR custom amount), and counter
        boolean hasValidAmount =
                (selectedPointsCard != null) ||
                        (inputCustomAmount.getText() != null &&
                                !inputCustomAmount.getText().toString().trim().isEmpty());

        if (isIDValid && hasValidAmount && hasCounterSelected) {
            btnProceed.setEnabled(true);
            btnProceed.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.greenKomyu));
        } else {
            btnProceed.setEnabled(false);
            btnProceed.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.lightblack));
        }
    }

    // === Confirmation Modal ===
    private void showConfirmationModal() {
        String id = inputID.getText() != null ? inputID.getText().toString().trim() : "";

        // Determine points value and type - make them effectively final
        final String pointsValue;
        final boolean isCustom;

        if (selectedPointsCard != null) {
            pointsValue = getPointsValueFromCard(selectedPointsCard);
            isCustom = false;
        } else if (inputCustomAmount.getText() != null &&
                !inputCustomAmount.getText().toString().trim().isEmpty()) {
            pointsValue = inputCustomAmount.getText().toString().trim();
            isCustom = true;
        } else {
            // This should not happen due to validation, but as fallback
            Toast.makeText(requireContext(), "Please select an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCounter = selectedCounterCard == cardCounter1 ?
                "Counter 1 - Terminal 1 • SM Seaside" :
                "Counter 2 - Terminal 2 • SM City";

        View modalView = LayoutInflater.from(getContext()).inflate(R.layout.modal_payment_confirmation, null);
        TextView tvDetails = modalView.findViewById(R.id.tv_payment_details);
        MaterialButton btnConfirm = modalView.findViewById(R.id.btn_confirm_payment);
        MaterialButton btnCancel = modalView.findViewById(R.id.btn_cancel_payment);

        String details = String.format("ID: %s\n\nPoints: %s\n\nCounter: %s\n\nType: %s\n\nStatus: Pending Admin Approval",
                id, pointsValue, selectedCounter, isCustom ? "Custom Amount" : "Predefined Points");
        tvDetails.setText(details);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(modalView)
                .setCancelable(false)
                .create();

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            processTopUp(id, pointsValue, selectedCounter, isCustom);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String getPointsValueFromCard(MaterialCardView card) {
        if (card == null) return "";

        // Map card IDs to point values
        HashMap<Integer, String> pointsMap = new HashMap<>();
        pointsMap.put(R.id.card_points_20, "20");
        pointsMap.put(R.id.card_points_40, "40");
        pointsMap.put(R.id.card_points_80, "80");
        pointsMap.put(R.id.card_points_100, "100");
        pointsMap.put(R.id.card_points_250, "250");
        pointsMap.put(R.id.card_points_500, "500");
        pointsMap.put(R.id.card_points_800, "800");
        pointsMap.put(R.id.card_points_1000, "1000");
        pointsMap.put(R.id.card_points_1500, "1500");

        return pointsMap.get(card.getId());
    }

    private void processTopUp(String id, String pointsValue, String counter, boolean isCustom) {
        // --- Validate numeric points ---
        int parsedPoints;
        try {
            parsedPoints = Integer.parseInt(pointsValue.trim());
            if (parsedPoints <= 0) {
                Toast.makeText(requireContext(), "Points must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid points value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Normalize pointsValue to integer string
        pointsValue = String.valueOf(parsedPoints);

        // Debug log - check what's being sent to PHP
        android.util.Log.d("TopUpDebug",
                "Sending commuter_id=" + id + ", points=" + pointsValue + ", counter=" + counter);

        // --- Show processing dialog ---
        showProcessingDialog();

        // --- Retrofit API call ---
        APIService api = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = api.topUpPoints(
                id,          // commuter_id
                pointsValue, // points
                counter      // counter_location
        );

        String finalPointsValue = pointsValue;
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful() && response.body() != null) {
                            APIResponse apiResponse = response.body();
                            if (apiResponse.success == 1) {
                                Toast.makeText(requireContext(),
                                        String.format("Top-up request submitted! %s points pending admin approval.", finalPointsValue),
                                        Toast.LENGTH_LONG).show();

                                // Don't refresh balance immediately since it's pending approval
                                // fetchUserProfileFromServer();

                                // Navigate back to home after success
                                new android.os.Handler().postDelayed(() -> {
                                    navigateToHomeFragment();
                                }, 2000);
                            } else {
                                String errorMessage = apiResponse.message != null ?
                                        apiResponse.message : "Unknown error occurred";
                                Toast.makeText(requireContext(),
                                        "Top-up failed: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "HTTP Error: " + response.code();
                                Toast.makeText(requireContext(),
                                        "Top-up failed: " + errorBody,
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(requireContext(),
                                        "Top-up failed: Network error " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        resetForm();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Top-up failed: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        resetForm();
                    });
                }
            }
        });
    }

    private void showProcessingDialog() {
        View processingView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_processing, null);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(processingView)
                .setCancelable(false);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Auto-dismiss after 3 seconds (API call should complete by then)
        new android.os.Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3000);
    }

    private void resetForm() {
        // Reset all form fields but keep the auto-filled ID
        if (inputID != null && currentUser != null && currentUser.commuterId != null) {
            // Keep the auto-filled ID, just clear other fields
            inputID.setText(currentUser.commuterId);
            isIDValid = true;
        } else {
            inputID.setText("");
            isIDValid = false;
        }

        if (inputCustomAmount != null) {
            inputCustomAmount.setText("");
        }

        // Reset points selection
        unselectAllPoints();
        selectedPointsCard = null;
        hasAmountSelected = false;

        // Reset counter selection
        if (selectedCounterCard != null) {
            selectedCounterCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.lightblack));
            selectedCounterCard.setStrokeWidth(1);
            selectedCounterCard = null;
        }
        hasCounterSelected = false;

        // Update button state
        updateProceedButtonState();
    }
}