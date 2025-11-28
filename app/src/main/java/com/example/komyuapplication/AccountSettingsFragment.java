package com.example.komyuapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.SharedPreferences;

public class AccountSettingsFragment extends Fragment {

    private TextView tvUserName, tvUserId, tvUserEmail, tvUserType, tvMemberSince;
    private ImageView ivProfilePicture;
    private String username;
    private String commuterId; // ADD THIS

    private APIResponse.User currentUser;

    // Add a method to set username from outside
    public void setUsername(String username) {
        this.username = username;
        Log.d("AccountSettings", "Username set to: " + username);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        // GET USERNAME FROM PARENT ACTIVITY
        if (getActivity() instanceof CommuterDashboard) {
            CommuterDashboard activity = (CommuterDashboard) getActivity();
            username = activity.getUsername();
            Log.d("AccountSettings", "Username from CommuterDashboard: " + username);
        }

        // If still no username, try other sources
        if (username == null || username.isEmpty()) {
            // Try from arguments
            if (getArguments() != null) {
                username = getArguments().getString("username", "");
            }

            // Try from SharedPreferences as last resort
            if ((username == null || username.isEmpty()) && getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                username = prefs.getString("username", "");
            }
        }

        Log.d("AccountSettings", "Final username: " + username);

        // Initialize views
        initializeViews(view);

        // Load user data if we have username
        if (username != null && !username.isEmpty()) {
            loadUserData();
        } else {
            Toast.makeText(getActivity(), "Cannot load profile: User not identified", Toast.LENGTH_LONG).show();
            Log.e("AccountSettings", "CRITICAL: No username available");
        }

        // Set up back button
        ImageView btnBack = view.findViewById(R.id.btn_back_account_settings);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        // Set up edit profile button
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private void initializeViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserId = view.findViewById(R.id.tv_user_id);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserType = view.findViewById(R.id.tv_user_type);
        tvMemberSince = view.findViewById(R.id.tv_member_since);
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
    }

    private void loadUserData() {
        APIService apiService = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = apiService.getUserProfile(username);

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse.success == 1 && apiResponse.user != null) {
                        currentUser = apiResponse.user; // Store the user data
                        commuterId = apiResponse.user.commuterId; // Store commuterId for updates
                        updateUIWithUserData(apiResponse.user);
                    } else {
                        Toast.makeText(getActivity(), "Failed to load profile: " + apiResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithUserData(APIResponse.User user) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Better full name formatting that handles empty middle names
                String fullName;
                if (user.fullName != null && !user.fullName.isEmpty()) {
                    fullName = user.fullName;
                } else {
                    StringBuilder nameBuilder = new StringBuilder();
                    nameBuilder.append(user.firstName != null ? user.firstName : "");

                    if (user.middleName != null && !user.middleName.isEmpty()) {
                        nameBuilder.append(" ").append(user.middleName);
                    }

                    if (user.lastName != null && !user.lastName.isEmpty()) {
                        nameBuilder.append(" ").append(user.lastName);
                    }
                    fullName = nameBuilder.toString().trim();
                }

                tvUserName.setText(fullName);
                tvUserId.setText("ID: " + (user.commuterId != null ? user.commuterId : ""));
                tvUserEmail.setText(user.email != null ? user.email : "");
                tvUserType.setText(user.categories != null ? user.categories : "");
                tvMemberSince.setText(formatMemberSince(user.accountCreated));
            }
        });
    }

    private String formatMemberSince(String accountCreated) {
        try {
            if (accountCreated != null && !accountCreated.isEmpty()) {
                String[] parts = accountCreated.split(" ")[0].split("-");
                if (parts.length >= 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    return monthNames[month - 1] + " " + year;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void showEditProfileDialog() {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Profile");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        EditText etFirstName = dialogView.findViewById(R.id.et_first_name);
        EditText etMiddleName = dialogView.findViewById(R.id.et_middle_name);
        EditText etLastName = dialogView.findViewById(R.id.et_last_name);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etAddress = dialogView.findViewById(R.id.et_address);

        // Handle null values properly
        etFirstName.setText(currentUser.firstName != null ? currentUser.firstName : "");
        etMiddleName.setText(currentUser.middleName != null ? currentUser.middleName : "");
        etLastName.setText(currentUser.lastName != null ? currentUser.lastName : "");
        etEmail.setText(currentUser.email != null ? currentUser.email : "");
        etPhone.setText(currentUser.phoneNumber != null ? currentUser.phoneNumber : "");
        etAddress.setText(currentUser.address != null ? currentUser.address : "");

        builder.setView(dialogView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateProfile(
                                etFirstName.getText().toString().trim(),
                                etMiddleName.getText().toString().trim(), // will be "" if empty
                                etLastName.getText().toString().trim(),
                                etEmail.getText().toString().trim(),
                                etPhone.getText().toString().trim(),
                                etAddress.getText().toString().trim()
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfile(String firstName, String middleName, String lastName,
                               String email, String phone, String address) {
        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        APIService apiService = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = apiService.updateProfile(
                commuterId,
                username,
                firstName,
                middleName,
                lastName,
                email,
                phone,
                address,
                currentUser.dateOfBirth // Keep existing date of birth
        );

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    APIResponse apiResponse = response.body();
                    if (apiResponse.success == 1) {
                        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        // Reload user data to refresh the display
                        loadUserData();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update: " + apiResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Error updating profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}