package com.example.komyuapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class SignupPage2 extends AppCompatActivity {

    private TextInputEditText editDateBirth;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_page2);

        // Go to Login
        TextView goToLogin = findViewById(R.id.textGoTologin);
        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginFirstPage.class);
            startActivity(intent);
        });

        // Go to Signup Page 3
        TextView goToSignupP3 = findViewById(R.id.btnNext);
        goToSignupP3.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpPage3.class);
            startActivity(intent);
        });

        // Initialize Date Picker
        editDateBirth = findViewById(R.id.editDateBirth);
        calendar = Calendar.getInstance();

        // Disable manual typing (optional)
        editDateBirth.setKeyListener(null);

        // Show DatePickerDialog when clicking the field
        editDateBirth.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SignupPage2.this,
                    (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                        // Format MM/DD/YY
                        String formattedDate = String.format("%02d/%02d/%02d",
                                (selectedMonth + 1),
                                selectedDay,
                                selectedYear % 100);

                        editDateBirth.setText(formattedDate);
                    },
                    year, month, day
            );

            // OPTIONAL: disallow selecting future dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            datePickerDialog.show();
        });

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
