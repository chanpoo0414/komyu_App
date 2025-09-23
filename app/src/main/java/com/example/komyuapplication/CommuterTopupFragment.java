package com.example.komyuapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CommuterTopupFragment extends Fragment {

    private TextInputEditText inputID, inputCustomAmount;
    private MaterialButton btnProceed;
    private RadioGroup radioGroupCounters;
    private RadioButton radioCounter1, radioCounter2;
    private MaterialCardView cardCounter1, cardCounter2;

    private List<MaterialCardView> pointsCards;
    private MaterialCardView selectedPointsCard = null;
    private String selectedPointsValue = "";
    private boolean isCustomAmountSelected = false;

    public CommuterTopupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commuter_topup, container, false);

        // Initialize views
        initializeViews(root);

        // Set up click listeners
        setupClickListeners();

        // Set up text watchers for validation
        setupTextWatchers();

        // Update proceed button state initially
        updateProceedButtonState();

        return root;
    }

    private void initializeViews(View root) {
        // Text inputs
        inputID = root.findViewById(R.id.input_ID);
        inputCustomAmount = root.findViewById(R.id.input_custom_amount);

        // Buttons
        btnProceed = root.findViewById(R.id.btn_proceed);
        Button btnWallet = root.findViewById(R.id.btn_wallet);
        Button btnTopup = root.findViewById(R.id.btn_topup);

        // RadioGroup and buttons
        radioGroupCounters = root.findViewById(R.id.radio_group_counters);
        radioCounter1 = root.findViewById(R.id.radio_counter1);
        radioCounter2 = root.findViewById(R.id.radio_counter2);

        // Counter cards - FIXED: Get the cards by their actual IDs
        cardCounter1 = root.findViewById(R.id.card_counter1);
        cardCounter2 = root.findViewById(R.id.card_counter2);

        // Set click listeners for the counter cards
        if (cardCounter1 != null) {
            cardCounter1.setOnClickListener(v -> radioCounter1.setChecked(true));
        }
        if (cardCounter2 != null) {
            cardCounter2.setOnClickListener(v -> radioCounter2.setChecked(true));
        }

        // Initialize points cards list
        pointsCards = new ArrayList<>();

        // Add all points cards to the list
        int[] pointsCardIds = {
                R.id.card_points_20, R.id.card_points_40, R.id.card_points_80,
                R.id.card_points_100, R.id.card_points_250, R.id.card_points_500,
                R.id.card_points_800, R.id.card_points_1000, R.id.card_points_1500
        };

        for (int id : pointsCardIds) {
            MaterialCardView card = root.findViewById(id);
            if (card != null) {
                pointsCards.add(card);
                // Make sure cards are clickable
                card.setClickable(true);
                card.setFocusable(true);
            }
        }

        // Wallet button click - navigate back to home fragment
        btnWallet.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterHomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Topup button click - already on this page, so do nothing or show message
        btnTopup.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "You are already on Top Up page", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        // Set up points cards click listeners
        for (MaterialCardView card : pointsCards) {
            card.setOnClickListener(v -> onPointsCardClicked(card));
        }

        // Set up custom amount focus listener
        inputCustomAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                onCustomAmountSelected();
            }
        });

        // Set up radio group listener for proper single selection
        radioGroupCounters.setOnCheckedChangeListener((group, checkedId) -> {
            onCounterSelected();
        });

        // Proceed button click listener
        btnProceed.setOnClickListener(v -> onProceedClicked());
    }

    private void setupTextWatchers() {
        // Text watcher for custom amount input
        inputCustomAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    onCustomAmountSelected();
                } else {
                    // If custom amount is cleared, ensure points selection is also reset
                    if (isCustomAmountSelected) {
                        isCustomAmountSelected = false;
                    }
                }
                updateProceedButtonState();
            }
        });

        // Text watcher for ID input
        inputID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateProceedButtonState();
            }
        });
    }

    private void onPointsCardClicked(MaterialCardView clickedCard) {
        // If the same card is clicked again, deselect it
        if (selectedPointsCard == clickedCard) {
            resetAllPointsCards();
            selectedPointsCard = null;
            selectedPointsValue = "";
            isCustomAmountSelected = false;
        } else {
            // Reset all points cards to default appearance
            resetAllPointsCards();

            // Set the clicked card as selected with light grey background
            clickedCard.setCardBackgroundColor(getResources().getColor(R.color.lightGrey));
            clickedCard.setStrokeColor(getResources().getColor(R.color.greenKomyu));
            clickedCard.setStrokeWidth(3); // Slightly thicker border for selection

            // Keep text colors black for better visibility on light grey
            updatePointsCardTextColor(clickedCard, R.color.black);

            selectedPointsCard = clickedCard;
            selectedPointsValue = getPointsValueFromCard(clickedCard);
            isCustomAmountSelected = false;

            // Clear custom amount field
            inputCustomAmount.setText("");
            inputCustomAmount.clearFocus();
        }

        updateProceedButtonState();
    }

    private void onCustomAmountSelected() {
        // Reset points card selection
        resetAllPointsCards();
        selectedPointsCard = null;
        selectedPointsValue = "";
        isCustomAmountSelected = true;
        updateProceedButtonState();
    }

    private void onCounterSelected() {
        updateProceedButtonState();
    }

    private void resetAllPointsCards() {
        for (MaterialCardView card : pointsCards) {
            card.setCardBackgroundColor(getResources().getColor(R.color.white));
            card.setStrokeColor(getResources().getColor(R.color.lightblack));
            card.setStrokeWidth(1);
            updatePointsCardTextColor(card, R.color.black);
        }
    }

    private void updatePointsCardTextColor(MaterialCardView card, int colorResId) {
        // Update text colors in the card
        ViewGroup layout = (ViewGroup) card.getChildAt(0);
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(getResources().getColor(colorResId));
                }
            }
        }
    }

    private String getPointsValueFromCard(MaterialCardView card) {
        // Extract points value from the card's text views
        ViewGroup layout = (ViewGroup) card.getChildAt(0);
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    String text = textView.getText().toString();
                    // Return the first numeric value found (points value)
                    if (text.matches("\\d+")) {
                        return text;
                    }
                }
            }
        }
        return "";
    }

    private void updateProceedButtonState() {
        boolean isIDValid = inputID.getText() != null &&
                !inputID.getText().toString().trim().isEmpty();

        boolean isPointsSelected = (selectedPointsCard != null && !selectedPointsValue.isEmpty()) ||
                (inputCustomAmount.getText() != null &&
                        !inputCustomAmount.getText().toString().trim().isEmpty());

        boolean isCounterSelected = radioCounter1.isChecked() || radioCounter2.isChecked();

        boolean isFormValid = isIDValid && isPointsSelected && isCounterSelected;

        // Update proceed button appearance
        if (isFormValid) {
            btnProceed.setBackgroundColor(getResources().getColor(R.color.greenKomyu));
            btnProceed.setEnabled(true);
            btnProceed.setAlpha(1.0f);
        } else {
            btnProceed.setBackgroundColor(getResources().getColor(R.color.grey));
            btnProceed.setEnabled(false);
            btnProceed.setAlpha(0.5f);
        }
    }

    private void onProceedClicked() {
        // Validate all fields one more time
        String id = inputID.getText() != null ? inputID.getText().toString().trim() : "";
        String pointsValue = "";
        boolean isCustom = false;

        if (selectedPointsCard != null) {
            pointsValue = selectedPointsValue;
            isCustom = false;
        } else if (inputCustomAmount.getText() != null &&
                !inputCustomAmount.getText().toString().trim().isEmpty()) {
            pointsValue = inputCustomAmount.getText().toString().trim();
            isCustom = true;
        }

        String selectedCounter = radioCounter1.isChecked() ? "Counter 1 - Terminal 1 • SM Seaside" :
                radioCounter2.isChecked() ? "Counter 2 - Terminal 2 • SM City" : "";

        if (id.isEmpty() || pointsValue.isEmpty() || selectedCounter.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation modal instead of direct processing
        showConfirmationModal(id, pointsValue, selectedCounter, isCustom);
    }

    private void showConfirmationModal(String id, String pointsValue, String counter, boolean isCustom) {
        // Create custom view for the modal
        View modalView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_payment_confirmation, null);

        TextView tvDetails = modalView.findViewById(R.id.tv_payment_details);
        MaterialButton btnConfirm = modalView.findViewById(R.id.btn_confirm_payment);
        MaterialButton btnCancel = modalView.findViewById(R.id.btn_cancel_payment);

        // Set payment details
        String details = String.format("ID: %s\n\nPoints: %s\n\nCounter: %s\n\nType: %s",
                id, pointsValue, counter, isCustom ? "Custom Amount" : "Predefined Points");
        tvDetails.setText(details);

        // Create the dialog
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(modalView)
                .setCancelable(false);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();

        // Set up button listeners
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            processPayment(id, pointsValue, counter, isCustom);
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void processPayment(String id, String pointsValue, String counter, boolean isCustom) {
        // Show processing dialog
        showProcessingDialog();

        // Simulate API call delay
        new android.os.Handler().postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Show success message
                    Toast.makeText(requireContext(), "Payment successful! " + pointsValue + " points added to your account.", Toast.LENGTH_LONG).show();

                    // Reset all fields
                    resetForm();
                });
            }
        }, 2000); // 2 second delay to simulate processing
    }

    private void showProcessingDialog() {
        View processingView = LayoutInflater.from(requireContext()).inflate(R.layout.modal_processing, null);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(processingView)
                .setCancelable(false);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Auto-dismiss after 2 seconds (simulated processing time)
        new android.os.Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 2000);
    }

    private void resetForm() {
        // Reset all form fields
        if (inputID != null) {
            inputID.setText("");
        }

        if (inputCustomAmount != null) {
            inputCustomAmount.setText("");
        }

        // Reset points selection
        resetAllPointsCards();
        selectedPointsCard = null;
        selectedPointsValue = "";
        isCustomAmountSelected = false;

        // Reset counter selection
        if (radioGroupCounters != null) {
            radioGroupCounters.clearCheck();
        }

        // Update button state
        updateProceedButtonState();

        // Clear focus
        if (inputCustomAmount != null) {
            inputCustomAmount.clearFocus();
        }
    }
}