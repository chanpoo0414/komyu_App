package com.example.komyuapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommuterMenuFragment extends Fragment {

    private TextView tvFullName, tvStudentId, tvUserRole;
    private ImageView imgProfileIcon;
    private AuthRepository authRepository;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commuter_menu, container, false);

        // Initialize views
        initializeViews(view);

        // Initialize SharedPreferences


        // Initialize repository
        authRepository = new AuthRepository();

        // Load user data
        loadUserData();

        // Set up click listeners
        setupClickListeners(view);

        return view;
    }

    private void initializeViews(View view) {
        tvFullName = view.findViewById(R.id.tvFullName);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        imgProfileIcon = view.findViewById(R.id.imgProfileIcon);
    }

    private void loadUserData() {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());

        // First try to load from cached data in UserSessionManager
        String cachedUserData = sessionManager.getUserData();
        if (cachedUserData != null) {
            try {
                Gson gson = new Gson();
                APIResponse apiResponse = gson.fromJson(cachedUserData, APIResponse.class);
                if (apiResponse != null && apiResponse.user != null) {
                    updateUIWithUserData(apiResponse.user);
                    return; // Use cached data if available
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If no cached data, fetch from API
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            authRepository.fetchUserProfile(username, new Callback<APIResponse>() {
                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                        updateUIWithUserData(response.body().user);

                        // Cache the user data using UserSessionManager
                        Gson gson = new Gson();
                        String userJson = gson.toJson(response.body());
                        sessionManager.saveUserData(userJson);
                    } else {
                        showError("Failed to load user data");
                    }
                }

                @Override
                public void onFailure(Call<APIResponse> call, Throwable t) {
                    showError("Network error: " + t.getMessage());
                }
            });
        } else {
            showError("No user session found");
        }
    }

    private void updateUIWithUserData(APIResponse.User user) {
        // Update profile information
        if (user.fullName != null && !user.fullName.isEmpty()) {
            tvFullName.setText(user.fullName);
        } else if (user.firstName != null && user.lastName != null) {
            tvFullName.setText(user.firstName + " " + user.lastName);
        } else {
            tvFullName.setText("User");
        }

        if (user.commuterId != null && !user.commuterId.isEmpty()) {
            tvStudentId.setText(user.commuterId);
        } else {
            tvStudentId.setText("N/A");
        }

        if (user.categories != null && !user.categories.isEmpty()) {
            tvUserRole.setText(user.categories);
        } else {
            tvUserRole.setText("Commuter");
        }

        // Load profile picture if available
        if (user.profilePicture != null && !user.profilePicture.isEmpty()) {
            Glide.with(this)
                    .load(user.profilePicture)
                    .placeholder(R.drawable.username_icon)
                    .error(R.drawable.username_icon)
                    .transform(new CircleCrop())
                    .into(imgProfileIcon);
        }
    }

    private void setupClickListeners(View view) {
        // Edit Profile Button
        View btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileFragment();
            }
        });

        // Account Settings Button
        View btnAccountSettings = view.findViewById(R.id.btnAccountSettings);
        btnAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountSettings();
            }
        });

        // Security & Privacy Button
        View btnSecurityPrivacy = view.findViewById(R.id.btnSecurityPrivacy);
        btnSecurityPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSecurityPrivacy();
            }
        });

        // Notifications Button
        View btnNotifications = view.findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotifications();
            }
        });

        // Help & Support Button
        View btnHelpSupport = view.findViewById(R.id.btnHelpSupport);
        btnHelpSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpSupport();
            }
        });

        // Terms & Conditions Button
        View btnTermsConditions = view.findViewById(R.id.btnTermsConditions);
        btnTermsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTermsConditions();
            }
        });

        // Privacy Policy Button
        View btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPrivacyPolicy();
            }
        });

        // Logout Button
        View btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });
    }

    private void openEditProfileFragment() {
        SettingsEditProfileFragment editProfileFragment = new SettingsEditProfileFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, editProfileFragment);
        transaction.addToBackStack("edit_profile");
        transaction.commit();
    }

    private void openAccountSettings() {
        // Create and open Account Settings Fragment
        AccountSettingsFragment accountSettingsFragment = new AccountSettingsFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, accountSettingsFragment);
        transaction.addToBackStack("account_settings");
        transaction.commit();

        Toast.makeText(requireContext(), "Account Settings", Toast.LENGTH_SHORT).show();
    }

    private void openSecurityPrivacy() {
        // Create and open Security & Privacy Fragment
        SecurityPrivacyFragment securityPrivacyFragment = new SecurityPrivacyFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, securityPrivacyFragment);
        transaction.addToBackStack("security_privacy");
        transaction.commit();

        Toast.makeText(requireContext(), "Security & Privacy", Toast.LENGTH_SHORT).show();
    }

    private void openNotifications() {
        // Create and open Notifications Fragment
        NotificationsFragment notificationsFragment = new NotificationsFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, notificationsFragment);
        transaction.addToBackStack("notifications");
        transaction.commit();

        Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show();
    }

    private void openHelpSupport() {
        // Create and open Help & Support Fragment
        HelpSupportFragment helpSupportFragment = new HelpSupportFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, helpSupportFragment);
        transaction.addToBackStack("help_support");
        transaction.commit();

        Toast.makeText(requireContext(), "Help & Support", Toast.LENGTH_SHORT).show();
    }

    private void openTermsConditions() {
        // Create and open Terms & Conditions Fragment
        TermsConditionsFragment termsConditionsFragment = new TermsConditionsFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, termsConditionsFragment);
        transaction.addToBackStack("terms_conditions");
        transaction.commit();

        Toast.makeText(requireContext(), "Terms & Conditions", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        // Create and open Privacy Policy Fragment
        PrivacyPolicyFragment privacyPolicyFragment = new PrivacyPolicyFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, privacyPolicyFragment);
        transaction.addToBackStack("privacy_policy");
        transaction.commit();

        Toast.makeText(requireContext(), "Privacy Policy", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmationDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout_confirm, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void performLogout() {
        // Clear session data
        clearUserSession();

        // Navigate back to login page
        Intent intent = new Intent(requireActivity(), LoginFirstPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private String getCurrentUsername() {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        return sessionManager.getUsername();
    }

    private void clearUserSession() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}