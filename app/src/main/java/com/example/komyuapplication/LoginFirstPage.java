package com.example.komyuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFirstPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_first_page);

        // NEW: Check if user should be automatically logged in (30-day remember me)
        UserSessionManager sessionManager = new UserSessionManager(LoginFirstPage.this);
        if (sessionManager.isSessionValid()) {
            // Auto-login user and redirect to dashboard
            CustomToast.showSuccess(this, "Welcome back!");
            startActivity(new Intent(LoginFirstPage.this, CommuterDashboard.class));
            finish();
            return;
        }

        // ALL YOUR EXISTING CODE CONTINUES UNCHANGED BELOW
        TextInputEditText editUsername = findViewById(R.id.editUsername);
        TextInputEditText editPassword = findViewById(R.id.editPassword);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView forgotPassword = findViewById(R.id.textForgotPassword);
        TextView signup = findViewById(R.id.textGoToSignup);

        // Forgot Password
        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // Go to Sign Up
        signup.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        // Login
        btnSignIn.setOnClickListener(v -> {
            String username = editUsername.getText() == null ? "" : editUsername.getText().toString().trim();
            String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                CustomToast.showWarning(this, "Please enter both Username and password.");
                return;
            }

            // All users must go through API
            APIService api = APIClient.getInstance().create(APIService.class);
            Call<APIResponse> call = api.login(username, password);
            call.enqueue(new Callback<APIResponse>() {
                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        CustomToast.showError(LoginFirstPage.this, "Login failed. Try again.");
                        return;
                    }

                    APIResponse res = response.body();
                    if (res.success == 1) {
                        // CHANGED: Create remember me session instead of regular session
                        UserSessionManager sessionManager = new UserSessionManager(LoginFirstPage.this);
                        sessionManager.createRememberMeSession(username);

                        // Fetch complete user profile and save it
                        fetchAndSaveUserProfile(username, sessionManager);

                        CustomToast.showSuccess(LoginFirstPage.this, "Login successful!");
                        startActivity(new Intent(LoginFirstPage.this, CommuterDashboard.class));
                        finish();
                    } else {
                        CustomToast.showError(LoginFirstPage.this,
                                res.message == null ? "Invalid credentials" : res.message);
                    }
                }

                @Override
                public void onFailure(Call<APIResponse> call, Throwable t) {
                    CustomToast.showError(LoginFirstPage.this, "Network error: " + t.getMessage());
                }
            });
        });

        // Insets handler (your layout has @id/main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchAndSaveUserProfile(String username, UserSessionManager sessionManager) {
        APIService api = APIClient.getInstance().create(APIService.class);
        Call<APIResponse> call = api.getUserProfile(username);
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convert the response to JSON and save it
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String userJson = gson.toJson(response.body());
                    sessionManager.saveUserData(userJson);
                }
                // Note: Even if profile fetch fails, login is still successful
                // The home fragment will try to fetch the profile again
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                // Profile fetch failed, but login is still successful
                // The home fragment will handle retrying the profile fetch
            }
        });
    }
}