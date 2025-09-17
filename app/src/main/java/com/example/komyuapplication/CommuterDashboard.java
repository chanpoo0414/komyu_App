package com.example.komyuapplication;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import android.view.View;

public class CommuterDashboard extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commuter_dashboard);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Make bottom nav respect system bars (especially gesture/nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), sys.bottom);
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

        // Load default fragment on first create
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterHomeFragment())
                    .commit();
        }
    }

    // Forward activity results (e.g., ZXing IntentIntegrator) to the fragment currently shown
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (frag instanceof ScannerFragment) {
            ((ScannerFragment) frag).handleActivityResult(requestCode, resultCode, data);
        } else if (frag != null) {
            // If some other fragment needs it, you can forward similarly.
            frag.onActivityResult(requestCode, resultCode, data);
        }
    }
}
