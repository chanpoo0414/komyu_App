package com.example.komyuapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CommuterDashboard extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commuter_dashboard);

        bottomNav = findViewById(R.id.bottom_navigation);

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
    public void setBottomNavSelected(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }
}