package com.example.komyuapplication;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AuthRepository {
    private final APIService api;

    public AuthRepository() {
        api = APIClient.getInstance().create(APIService.class);
    }

    public void register(SignUPData d, Callback<APIResponse> cb) {
        // Add debug logging
        Log.d("AuthRepository", "Registering user: " + d.username);
        Log.d("AuthRepository", "Email: " + d.email);
        Log.d("AuthRepository", "Name: " + d.firstName + " " + d.lastName);

        String dob = convertDob(d.dateOfBirth);
        Log.d("AuthRepository", "Converted DOB: " + d.dateOfBirth + " -> " + dob);

        Call<APIResponse> call = api.register(
                safe(d.username),
                safe(d.email),
                safe(d.password),
                safe(d.firstName),
                safe(d.middleName),
                safe(d.lastName),
                dob,
                safe(d.address),
                safe(d.sex),
                safe(d.typeOfId),
                safe(d.frontId),  // Use the correct field name
                safe(d.backId),   // Use the correct field name
                safe(d.categories),
                safe(d.phone)
        );

        // Log the actual API call being made
        Log.d("AuthRepository", "Making API call to: commuterRegister.php");

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Success: " + response.body().success);
                    Log.d("API Response", "Message: " + response.body().message);
                    if (response.body().commuter_id != null) {
                        Log.d("API Response", "Commuter ID: " + response.body().commuter_id);
                    }
                    // Forward the response to the original callback
                    cb.onResponse(call, response);
                } else {
                    Log.e("API Response", "Error code: " + response.code());
                    Log.e("API Response", "Error message: " + response.message());

                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Response", "Error body: " + errorBody);
                    } catch (IOException e) {
                        Log.e("API Response", "Error reading error body: " + e.getMessage());
                    }

                    // Still forward to original callback
                    cb.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                Log.e("API Failure", "Network error: " + t.getMessage());
                Log.e("API Failure", "Error type: " + t.getClass().getSimpleName());
                t.printStackTrace();

                // Forward the failure to the original callback
                cb.onFailure(call, t);
            }
        });
    }

    // Helper: return "" if null
    private String safe(String val) {
        return val == null ? "" : val;
    }

    private String convertDob(String mmddyy) {
        if (mmddyy == null || mmddyy.isEmpty()) {
            Log.w("AuthRepository", "Empty or null DOB provided");
            return "";
        }
        try {
            SimpleDateFormat in  = new SimpleDateFormat("MM/dd/yy", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String converted = out.format(in.parse(mmddyy));
            Log.d("AuthRepository", "Successfully converted DOB: " + mmddyy + " -> " + converted);
            return converted;
        } catch (ParseException e) {
            Log.w("AuthRepository", "DOB conversion failed for: " + mmddyy + ", using as-is");
            // Assume already valid
            return mmddyy;
        }
    }
}