package com.example.komyuapplication;

//new 09-23-25
import android.util.Log;
import android.content.Context;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AuthRepository {
    private final APIService api;
    private UserSessionManager sessionManager;

    // Existing constructor (keep for backward compatibility)
    public AuthRepository() {
        api = APIClient.getInstance().create(APIService.class);
    }

    // Enhanced constructor with context for session management
    public AuthRepository(Context context) {
        api = APIClient.getInstance().create(APIService.class);
        if (context != null) {
            sessionManager = new UserSessionManager(context);
        }
    }

    // Set session manager separately if needed
    public void setSessionManager(Context context) {
        this.sessionManager = new UserSessionManager(context);
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

    // NEW: Enhanced login with automatic session creation and profile fetching
    public void login(String username, String password, Callback<APIResponse> cb) {
        Log.d("AuthRepository", "Logging in user: " + username);

        Call<APIResponse> call = api.login(username, password);
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("AuthRepository", "Login successful, fetching user profile...");

                    // Create login session if sessionManager is available
                    if (sessionManager != null) {
                        sessionManager.createLoginSession(username);
                        Log.d("AuthRepository", "Login session created for: " + username);
                    }

                    // After successful login, fetch complete user profile
                    fetchUserProfile(username, new Callback<APIResponse>() {
                        @Override
                        public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                            // Combine login success with profile data
                            if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                                APIResponse combinedResponse = new APIResponse();
                                combinedResponse.success = 1;
                                combinedResponse.message = "Login successful";
                                combinedResponse.user = response.body().user;

                                // Save user data to session if sessionManager is available
                                if (sessionManager != null) {
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    String userJson = gson.toJson(response.body().user);
                                    sessionManager.saveUserData(userJson);
                                    Log.d("AuthRepository", "User profile data saved to session");
                                    Log.d("AuthRepository", "User data - First: " + response.body().user.firstName +
                                            ", Last: " + response.body().user.lastName +
                                            ", Email: " + response.body().user.email);
                                }

                                // Create a mock response with the combined data
                                Response<APIResponse> mockResponse = Response.success(combinedResponse);
                                cb.onResponse(call, mockResponse);
                            } else {
                                // Even if profile fetch fails, still return login success but log warning
                                Log.w("AuthRepository", "User profile fetch failed, but login succeeded");
                                cb.onResponse(call, response);
                            }
                        }

                        @Override
                        public void onFailure(Call<APIResponse> call, Throwable t) {
                            // Even if profile fetch fails, still return login success but log warning
                            Log.w("AuthRepository", "User profile fetch failed: " + t.getMessage() + ", but login succeeded");
                            cb.onResponse(call, response);
                        }
                    });
                } else {
                    cb.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                cb.onFailure(call, t);
            }
        });
    }



    // NEW: Separate login method without automatic profile fetching (for backward compatibility)
    public void loginBasic(String username, String password, Callback<APIResponse> cb) {
        Log.d("AuthRepository", "Basic login for user: " + username);

        Call<APIResponse> call = api.login(username, password);
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                cb.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                cb.onFailure(call, t);
            }
        });
    }

    // NEW: Method to manually save user data to session
    public void saveUserDataToSession(APIResponse.User user) {
        if (sessionManager != null && user != null) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String userJson = gson.toJson(user);
            sessionManager.saveUserData(userJson);
            Log.d("AuthRepository", "User data manually saved to session");
        }
    }

    // NEW: Method to check if user is logged in
    public boolean isUserLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    // NEW: Method to get current username
    public String getCurrentUsername() {
        return sessionManager != null ? sessionManager.getUsername() : null;
    }

    // NEW: Method to logout user
    public void logout() {
        if (sessionManager != null) {
            sessionManager.logoutUser();
            Log.d("AuthRepository", "User logged out");
        }
    }

    public void fetchUserProfile(String username, Callback<APIResponse> cb) {
        Log.d("AuthRepository", "Fetching profile for: " + username);

        Call<APIResponse> call = api.getUserProfile(username);
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                // Save user data to session if available and sessionManager is set
                if (response.isSuccessful() && response.body() != null && response.body().user != null && sessionManager != null) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String userJson = gson.toJson(response.body().user);
                    sessionManager.saveUserData(userJson);
                    Log.d("AuthRepository", "User profile data saved during fetch");
                }
                cb.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                cb.onFailure(call, t);
            }
        });
    }

    // Helper method remains the same
    private String safe(String val) {
        return val == null ? "" : val;
    }
}