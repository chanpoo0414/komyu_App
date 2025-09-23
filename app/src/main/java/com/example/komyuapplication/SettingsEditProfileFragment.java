package com.example.komyuapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class SettingsEditProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1001;

    private ImageButton btnEditProfilePicture;
    private EditText editFullName, editUsername, editEmail, editPhone, editAddress;
    private EditText editEmergencyName, editEmergencyPhone, editEmergencyRelationship;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_edit_profile, container, false);

        // 🔹 Back button
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // Navigate back to previous fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 🔹 Profile picture
        btnEditProfilePicture = view.findViewById(R.id.btn_edit_profile_picture);
        btnEditProfilePicture.setOnClickListener(v -> openGallery());

        // 🔹 Personal info
        editFullName = view.findViewById(R.id.edit_full_name);
        editUsername = view.findViewById(R.id.edit_username);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);
        editAddress = view.findViewById(R.id.edit_address);

        // 🔹 Emergency info
        editEmergencyName = view.findViewById(R.id.edit_emergency_name);
        editEmergencyPhone = view.findViewById(R.id.edit_emergency_phone);
        editEmergencyRelationship = view.findViewById(R.id.edit_emergency_relationship);

        // 🔹 Save button
        Button btnSave = view.findViewById(R.id.btn_save_changes);
        btnSave.setOnClickListener(v -> saveProfileChanges());

        return view;
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
            }
        }
    }

    // Save profile changes
    private void saveProfileChanges() {
        String fullName = editFullName.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        String emergencyName = editEmergencyName.getText().toString().trim();
        String emergencyPhone = editEmergencyPhone.getText().toString().trim();
        String emergencyRelationship = editEmergencyRelationship.getText().toString().trim();

        // TODO: Send to API or Database
        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

        // Navigate back after saving
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}