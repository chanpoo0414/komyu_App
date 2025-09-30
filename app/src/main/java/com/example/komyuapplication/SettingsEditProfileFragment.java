package com.example.komyuapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsEditProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1001;

    private ImageButton btnEditProfilePicture;
    private EditText editFirstName, editMiddleName, editLastName, editUsername, editEmail, editPhone, editAddress;
    private APIService apiService;
    private UserSessionManager sessionManager;
    private String currentUsername;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = APIClient.getInstance().create(APIService.class);
        sessionManager = new UserSessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_edit_profile, container, false);

        // Initialize views
        initViews(view);

        // Load current user data
        loadUserProfile();

        return view;
    }

    private void initViews(View view) {
        // 🔹 Back button
        ImageView btnBack = view.findViewById(R.id.btn_back_edit_profile);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 🔹 Profile picture
        btnEditProfilePicture = view.findViewById(R.id.btn_edit_profile_picture);
        btnEditProfilePicture.setOnClickListener(v -> openGallery());

        // 🔹 Change photo text
        TextView textChangePhoto = view.findViewById(R.id.text_change_photo);
        textChangePhoto.setOnClickListener(v -> openGallery());

        // 🔹 Personal info fields - Updated for separate name fields
        editFirstName = view.findViewById(R.id.edit_first_name);
        editMiddleName = view.findViewById(R.id.edit_middle_name);
        editLastName = view.findViewById(R.id.edit_last_name);
        editUsername = view.findViewById(R.id.edit_username);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);
        editAddress = view.findViewById(R.id.edit_address);

        // Make username non-editable since it's used as identifier
        editUsername.setEnabled(false);

        // 🔹 Save button
        Button btnSave = view.findViewById(R.id.btn_save_changes);
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUsername = sessionManager.getUsername();
        Log.d("EditProfile", "Current username: " + currentUsername);

        // Try to load from saved user data first
        String userDataJson = sessionManager.getUserData();
        if (userDataJson != null) {
            Log.d("EditProfile", "Found saved user data");
            try {
                Gson gson = new Gson();
                APIResponse.User user = gson.fromJson(userDataJson, APIResponse.User.class);
                Log.d("EditProfile", "User data parsed - First: " + user.firstName + ", Middle: " + user.middleName + ", Last: " + user.lastName + ", Email: " + user.email);
                populateUserData(user);
            } catch (Exception e) {
                Log.e("EditProfile", "Error parsing saved user data: " + e.getMessage());
                e.printStackTrace();
                // If parsing fails, fetch from API
                fetchUserProfileFromAPI();
            }
        } else {
            Log.d("EditProfile", "No saved user data, fetching from API");
            // Fetch from API if no saved data
            fetchUserProfileFromAPI();
        }
    }

    private void fetchUserProfileFromAPI() {
        if (currentUsername == null) {
            Log.e("EditProfile", "currentUsername is null!");
            return;
        }

        Log.d("EditProfile", "Fetching profile from API for: " + currentUsername);
        Call<APIResponse> call = apiService.getUserProfile(currentUsername);
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    APIResponse.User user = response.body().user;
                    Log.d("EditProfile", "API Response successful - First: " + user.firstName + ", Email: " + user.email);
                    populateUserData(user);

                    // Save user data for future use
                    Gson gson = new Gson();
                    String userJson = gson.toJson(user);
                    sessionManager.saveUserData(userJson);
                    Log.d("EditProfile", "User data saved to session");

                } else {
                    Log.e("EditProfile", "API response failed - Code: " + response.code());
                    if (response.body() != null) {
                        Log.e("EditProfile", "Error message: " + response.body().message);
                    }
                    Toast.makeText(getContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Log.e("EditProfile", "API call failed: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(APIResponse.User user) {
        if (getActivity() == null) {
            Log.e("EditProfile", "Activity is null, cannot update UI");
            return;
        }

        getActivity().runOnUiThread(() -> {
            try {
                // Set individual name fields
                Log.d("EditProfile", "Setting first name: " + user.firstName);
                editFirstName.setText(user.firstName != null ? user.firstName : "");

                Log.d("EditProfile", "Setting middle name: " + user.middleName);
                editMiddleName.setText(user.middleName != null ? user.middleName : "");

                Log.d("EditProfile", "Setting last name: " + user.lastName);
                editLastName.setText(user.lastName != null ? user.lastName : "");

                // Set other fields
                Log.d("EditProfile", "Setting username: " + user.username);
                editUsername.setText(user.username != null ? user.username : "");

                Log.d("EditProfile", "Setting email: " + user.email);
                editEmail.setText(user.email != null ? user.email : "");

                Log.d("EditProfile", "Setting phone: " + user.phoneNumber);
                editPhone.setText(user.phoneNumber != null ? user.phoneNumber : "");

                Log.d("EditProfile", "Setting address: " + user.address);
                editAddress.setText(user.address != null ? user.address : "");

                Log.d("EditProfile", "All fields populated successfully");

            } catch (Exception e) {
                Log.e("EditProfile", "Error displaying user data: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(getContext(), "Error displaying user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Open gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // Handle result from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // Load into ImageButton using Glide
                Glide.with(this)
                        .load(selectedImage)
                        .circleCrop()
                        .into(btnEditProfilePicture);

                // TODO: Upload to Cloudinary when implemented
                Toast.makeText(getContext(), "Profile picture selected (upload feature coming soon)", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Save profile changes
    private void saveProfileChanges() {
        if (currentUsername == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = editFirstName.getText().toString().trim();
        String middleName = editMiddleName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        // Basic validation
        if (firstName.isEmpty()) {
            editFirstName.setError("First name is required");
            return;
        }

        if (lastName.isEmpty()) {
            editLastName.setError("Last name is required");
            return;
        }

        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            return;
        }

        // Show loading
        Toast.makeText(getContext(), "Updating profile...", Toast.LENGTH_SHORT).show();

        // Call API to update profile
        Call<APIResponse> call = apiService.updateProfile(currentUsername, firstName, middleName, lastName, email, phone, address);
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse.success == 1) {
                        Toast.makeText(getContext(), apiResponse.message, Toast.LENGTH_SHORT).show();

                        // Refresh user data after update
                        refreshUserData();

                        // Navigate back after successful update
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Update failed: " + apiResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Server error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshUserData() {
        // Clear cached data to force refresh
        sessionManager.saveUserData(null);

        // Fetch fresh data from API
        if (currentUsername != null) {
            fetchUserProfileFromAPI();
        }
    }
}