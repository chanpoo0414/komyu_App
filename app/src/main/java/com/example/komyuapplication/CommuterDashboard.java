package com.example.komyuapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CommuterDashboard extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commuter_dashboard);

        bottomNav = findViewById(R.id.bottom_navigation);

        username = getUsernameFromSources();
        Log.d("CommuterDashboard", "Username retrieved: " + username);
        // Remove background and ripple effects
        bottomNav.setItemBackgroundResource(android.R.color.transparent);
        bottomNav.setItemRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));

        // Make bottom nav respect system bars
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), sys.bottom);

            // Remove any background drawable
            if (v.getBackground() != null) {
                v.getBackground().setColorFilter(
                        ContextCompat.getColor(CommuterDashboard.this, R.color.whiteKomyu),
                        PorterDuff.Mode.SRC
                );
            }
            return insets;
        });

        UserSessionManager sessionManager = new UserSessionManager(this);
        if (!sessionManager.isSessionValid()) {
            // Session expired, redirect to login
            CustomToast.showError(this, "Session expired. Please login again.");
            Intent intent = new Intent(this, LoginFirstPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment frag;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                frag = new CommuterHomeFragment();
            } else if (id == R.id.nav_logs) {
                frag = new CommuterLogsFragment();
            } else if (id == R.id.nav_menu) {
                frag = new CommuterMenuFragment();
            } else {
                frag = new CommuterHomeFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commit();
            return true;
        });

        // Force refresh menu items after a short delay
        new Handler().postDelayed(() -> {
            for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                MenuItem item = bottomNav.getMenu().getItem(i);
                if (item.isChecked()) {
                    item.setChecked(true); // Force refresh
                }
            }
        }, 100);

        // Load default fragment on first create
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterHomeFragment())
                    .commit();
        }


    }
    private String getUsernameFromSources() {
        String user = "";

        // 1. Check Intent from Login
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            user = intent.getStringExtra("username");
            Log.d("CommuterDashboard", "✓ Username from Intent: " + user);
        }

        // 2. Check SharedPreferences
        if (user.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            user = prefs.getString("username", "");
            if (!user.isEmpty()) {
                Log.d("CommuterDashboard", "✓ Username from SharedPreferences: " + user);
            }
        }

        // 3. Check UserSessionManager
        if (user.isEmpty()) {
            UserSessionManager sessionManager = new UserSessionManager(this);
            if (sessionManager.isLoggedIn()) {
                user = sessionManager.getUsername();
                if (user != null && !user.isEmpty()) {
                    Log.d("CommuterDashboard", "✓ Username from UserSessionManager: " + user);
                }
            }
        }

        if (user.isEmpty()) {
            Log.e("CommuterDashboard", "✗ No username found from any source!");
        }

        return user;
    }

    // ADD THIS METHOD: Getter for username
    public String getUsername() {
        return username;
    }
    public void setBottomNavSelected(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }
}