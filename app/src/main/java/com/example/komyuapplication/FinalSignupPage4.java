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
    private SignUPData data;
    private Uri frontImageUri = null;
    private Uri backImageUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_signup_page4);

        validIdFragment = (ValidIdFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_valid_id_container);

        categoriesFragment = (CategoriesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_categories_container);

        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) {
            Toast.makeText(this, "Missing signup data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundTintList(getColorStateList(R.color.lightGreenKomyu));

        btnSubmit.setOnClickListener(v -> submitToServer());

        TextView txtLogin = findViewById(R.id.txtLogin);
        txtLogin.setOnClickListener(v -> showCancelDialog());
    }

    public void openImagePicker(String side) {
        currentSide = side;
        imagePickerLauncher.launch("image/*");
    }

    private void onImagePicked(Uri uri) {
        if (uri != null && validIdFragment != null) {
            if ("front".equals(currentSide)) {
                validIdFragment.updateFrontImage(uri);
                frontImageUri = uri;
            } else if ("back".equals(currentSide)) {
                validIdFragment.updateBackImage(uri);
                backImageUri = uri;
            }
        }
    }

    @Override
    public void onValidIdSelected(String id) {
        isValidIdSelected = (id != null && !id.isEmpty());
        data.typeOfId = id;

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
        data.categories = role;
        validateForm();
    }

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

    private void submitToServer() {
        if (frontImageUri == null || backImageUri == null) {
            Toast.makeText(this, "Please upload both front and back ID images", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Uploading...");

        // FIX: Use the constructor with context
        AuthRepository repo = new AuthRepository(this); // Pass 'this' as context

        uploadImagesAndRegister(repo);
    }

    private void uploadImagesAndRegister(AuthRepository repo) {
        // Upload front image
        repo.uploadImage(frontImageUri, "front_" + System.currentTimeMillis() + ".jpg", new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success == 1) {
                    String frontImageUrl = response.body().message; // URL returned from server
                    data.frontId = frontImageUrl;

                    // Upload back image
                    repo.uploadImage(backImageUri, "back_" + System.currentTimeMillis() + ".jpg", new Callback<APIResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().success == 1) {
                                String backImageUrl = response.body().message;
                                data.backId = backImageUrl;

                                // Now register with image URLs
                                completeRegistration(repo);
                            } else {
                                handleUploadError("Back image upload failed");
                            }
                        }

                        @Override
                        public void onFailure(Call<APIResponse> call, Throwable t) {
                            handleUploadError("Back image upload error: " + t.getMessage());
                        }
                    });
                } else {
                    handleUploadError("Front image upload failed");
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                handleUploadError("Front image upload error: " + t.getMessage());
            }
        });
    }

    private void completeRegistration(AuthRepository repo) {
        repo.register(data, new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit");

                if (response.isSuccessful() && response.body() != null) {
                    APIResponse res = response.body();
                    if (res.success == 1) {
                        Toast.makeText(FinalSignupPage4.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
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
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit");
                Toast.makeText(FinalSignupPage4.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUploadError(String message) {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Sign Up?")
                .setMessage("Are you sure you want to cancel the process of signing up?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> finish())
                .setNegativeButton("No, stay", (dialog, which) -> dialog.dismiss())
                .show();
    }
}