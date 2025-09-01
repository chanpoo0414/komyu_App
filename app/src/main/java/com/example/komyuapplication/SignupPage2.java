package com.example.komyuapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class SignupPage2 extends AppCompatActivity {

    private TextInputEditText editFirstName, editLastName, editDateBirth;
    private SignUPData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_page2);

        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) data = new SignUPData();

        editFirstName = findViewById(R.id.editFirstName);
        editLastName  = findViewById(R.id.editLastName);
        editDateBirth = findViewById(R.id.editDateBirth);

        // Date picker for DOB
        editDateBirth.setKeyListener(null); // make it non-typing to force picker
        editDateBirth.setOnClickListener(v -> showDatePicker());

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            String fname = safe(editFirstName);
            String lname = safe(editLastName);
            String dob   = safe(editDateBirth); // can be yyyy-MM-dd (we format it in picker)

            if (fname.isEmpty()) { toast("First name is required"); return; }
            if (lname.isEmpty()) { toast("Last name is required");  return; }
            if (dob.isEmpty())   { toast("Date of birth is required"); return; }

            data.firstName   = fname;
            data.lastName    = lname;
            data.dateOfBirth = dob;

            Intent i = new Intent(this, SignUpPage3.class);
            i.putExtra("data", data);
            startActivity(i);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Format as yyyy-MM-dd
            String mm = String.format("%02d", month + 1);
            String dd = String.format("%02d", dayOfMonth);
            editDateBirth.setText(year + "-" + mm + "-" + dd);
        }, y, m, d);

        dp.getDatePicker().setMaxDate(System.currentTimeMillis());
        dp.show();
    }

    private String safe(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
