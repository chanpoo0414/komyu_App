package com.example.komyuapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalSignupPage4 extends AppCompatActivity implements SignUpPage4Listener {

    private ValidIdFragment validIdFragment;
    private CategoriesFragment categoriesFragment;
    private String currentSide = "";

    private boolean isValidIdSelected = false;
    private boolean isRoleSelected = false;

    private MaterialButton btnSubmit;
    private SignUPData data; // ✅ carries all data from previous pages

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_signup_page4);

        // ✅ Make sure fragments exist in the XML layout
        validIdFragment = (ValidIdFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_valid_id_container);

        categoriesFragment = (CategoriesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_categories_container);

        // ✅ retrieve full signup data from previous pages
        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) {
            Toast.makeText(this, "Missing signup data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Submit button
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundTintList(getColorStateList(R.color.lightGreenKomyu));

        btnSubmit.setOnClickListener(v -> submitToServer());

        // Login text
        TextView txtLogin = findViewById(R.id.txtLogin);
        txtLogin.setOnClickListener(v -> showCancelDialog());
    }

    // === pick image (not saving to DB yet) ===
    public void openImagePicker(String side) {
        currentSide = side;
        imagePickerLauncher.launch("image/*");
    }

    private void onImagePicked(Uri uri) {
        if (uri != null && validIdFragment != null) {
            if ("front".equals(currentSide)) {
                validIdFragment.updateFrontImage(uri);
            } else if ("back".equals(currentSide)) {
                validIdFragment.updateBackImage(uri);
            }
        }
    }

    // === listener callbacks ===
    @Override
    public void onValidIdSelected(String id) {
        isValidIdSelected = (id != null && !id.isEmpty());
        data.typeOfId = id; // ✅ save to data

        // auto-assign role if matches
        if (id.equals("Student ID")) {
            onRoleSelected("Student");
        } else if (id.equals("Senior Citizen ID")) {
            onRoleSelected("Senior");
        } else if (id.equals("PWD ID")) {
            onRoleSelected("PWD");
        } else if (id.equals("Valid ID")) {
            onRoleSelected("Regular");
        }

        validateForm();
    }

    @Override
    public void onRoleSelected(String role) {
        isRoleSelected = (role != null && !role.isEmpty());
        data.categories = role; // ✅ save to data
        validateForm();
    }

    // === validate form ===
    private void validateForm() {
        boolean allValid = isValidIdSelected && isRoleSelected;
        btnSubmit.setEnabled(allValid);

        if (allValid) {
            btnSubmit.setBackgroundTintList(getColorStateList(R.color.greenKomyu));
            btnSubmit.setTextColor(getColor(R.color.white));
        } else {
            btnSubmit.setBackgroundTintList(getColorStateList(R.color.grey));
            btnSubmit.setTextColor(getColor(R.color.black));
        }
    }

    // === submit to API ===
    private void submitToServer() {
        // ✅ for now, don’t send pictures to backend
        data.frontId = "";
        data.backId = "";

        AuthRepository repo = new AuthRepository();
        repo.register(data, new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    APIResponse res = response.body();
                    if (res.success == 1) {
                        Toast.makeText(FinalSignupPage4.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                        // ✅ go to login
                        Intent i = new Intent(FinalSignupPage4.this, LoginFirstPage.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(FinalSignupPage4.this, "Failed: " + res.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FinalSignupPage4.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Toast.makeText(FinalSignupPage4.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // === cancel dialog ===
    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Sign Up?")
                .setMessage("Are you sure you want to cancel the process of signing up?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> finish())
                .setNegativeButton("No, stay", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
