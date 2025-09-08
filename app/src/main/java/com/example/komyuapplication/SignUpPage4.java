package com.example.komyuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;

public class SignUpPage4 extends AppCompatActivity implements SignUpPage4Listener {

    private String selectedValidId = "";
    private String selectedCategory = "";

    private TextView toastMessage;

    private ImageView toastIcon;
    private MaterialButton btnSubmit;
    private SignUPData data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page4);

        // ✅ Get data from previous page
        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) data = new SignUPData();

        btnSubmit = findViewById(R.id.btnSubmit);

        // Start disabled
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.gray)
        );

        // Attach fragments
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.fragment_valid_id_container) == null) {
            fm.beginTransaction()
                    .replace(R.id.fragment_valid_id_container, new ValidIdFragment())
                    .commit();
        }
        if (fm.findFragmentById(R.id.fragment_categories_container) == null) {
            fm.beginTransaction()
                    .replace(R.id.fragment_categories_container, new CategoriesFragment())
                    .commit();
        }

        // Submit button click
        btnSubmit.setOnClickListener(v -> {
            // Save selections
            data.typeOfId = selectedValidId;
            data.categories = selectedCategory;

            // (Later: add upload image file paths here)
            data.frontId = "front_id_mock.jpg";
            data.backId = "back_id_mock.jpg";

            // For now, just mock success and go to MainPage
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, findViewById(android.R.id.content), false);

            TextView toastMessage = layout.findViewById(R.id.toastMessage);
            toastMessage.setText(getString(R.string.account_created)); // dynamic text

            ImageView toastIcon = layout.findViewById(R.id.toastIcon);
            toastIcon.setImageResource(R.drawable.komyu_header_logo); // your logo

            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @Override
    public void onValidIdSelected(String id) {
        selectedValidId = (id == null ? "" : id.trim());

        // Show/hide upload buttons inside the fragment
        ValidIdFragment vFrag = (ValidIdFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_valid_id_container);
        if (vFrag != null) {
            boolean show = !selectedValidId.isEmpty();
            vFrag.setUploadButtonsVisible(show);
        }

        // Auto-select category based on ID type
        CategoriesFragment categoryFragment = (CategoriesFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_categories_container);

        if (categoryFragment != null) {
            if ("Student ID".equalsIgnoreCase(selectedValidId)) {
                categoryFragment.setCategory("Student");
            } else if ("Senior Citizen ID".equalsIgnoreCase(selectedValidId)) {
                categoryFragment.setCategory("Senior");
            } else if ("PWD ID".equalsIgnoreCase(selectedValidId)) {
                categoryFragment.setCategory("PWD");
            }
        }

        validateForm();
    }

    @Override
    public void onRoleSelected(String role) {
        selectedCategory = (role == null ? "" : role.trim());
        validateForm();
    }

    private void validateForm() {
        boolean isValid = selectedValidId != null && !selectedValidId.isEmpty()
                && selectedCategory != null && !selectedCategory.isEmpty();

        btnSubmit.setEnabled(isValid);
        btnSubmit.setBackgroundTintList(
                ContextCompat.getColorStateList(this,
                        isValid ? R.color.greenKomyu : R.color.gray)
        );
    }
}
