package com.example.komyuapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;

public class UserSessionManager {
    private static final String PREF_NAME = "KomyuUserPref";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_DATA = "userData";

    // NEW: Keys for remember me functionality
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    // Session duration (30 days in milliseconds)
    private static final long SESSION_DURATION = 30L * 24L * 60L * 60L * 1000L; // 30 days

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // EXISTING METHODS - KEEP EXACTLY THE SAME
    public void createLoginSession(String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public void saveUserData(String userJson) {
        editor.putString(KEY_USER_DATA, userJson);
        editor.apply();
    }

    public String getUserData() {
        return pref.getString(KEY_USER_DATA, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    // NEW METHODS FOR REMEMBER ME FUNCTIONALITY

    /**
     * Enhanced login session with timestamp for auto-logout after 30 days
     */
    public void createRememberMeSession(String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_REMEMBER_ME, true);

        // Store login timestamp
        long loginTime = System.currentTimeMillis();
        editor.putLong(KEY_LOGIN_TIMESTAMP, loginTime);

        editor.apply();

        Log.d("UserSessionManager", "Remember me session created for: " + username +
                " at: " + new Date(loginTime));
    }

    /**
     * Check if user session is still valid (within 30 days)
     */
    public boolean isSessionValid() {
        if (!pref.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }

        // Check if remember me is enabled
        if (!pref.getBoolean(KEY_REMEMBER_ME, false)) {
            return false;
        }

        // Check session expiration
        long loginTime = pref.getLong(KEY_LOGIN_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        if (loginTime == 0) {
            // No timestamp found, treat as expired
            return false;
        }

        long timeElapsed = currentTime - loginTime;
        boolean isValid = timeElapsed < SESSION_DURATION;

        Log.d("UserSessionManager", "Session check - Elapsed: " + (timeElapsed / (24 * 60 * 60 * 1000)) +
                " days, Valid: " + isValid);

        // Auto-logout if session expired
        if (!isValid) {
            logoutUser();
            Log.d("UserSessionManager", "Session expired after 30 days. Auto-logout.");
        }

        return isValid;
    }

    /**
     * Get days remaining until auto-logout
     */
    public int getDaysRemaining() {
        long loginTime = pref.getLong(KEY_LOGIN_TIMESTAMP, 0);
        if (loginTime == 0) return 0;

        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - loginTime;
        long timeRemaining = SESSION_DURATION - timeElapsed;

        return (int) (timeRemaining / (24 * 60 * 60 * 1000));
    }

    /**
     * Check if remember me is enabled
     */
    public boolean isRememberMeEnabled() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }
}