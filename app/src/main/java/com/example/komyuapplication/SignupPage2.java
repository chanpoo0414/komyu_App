package com.example.komyuapplication;

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
import com.google.android.material.textfield.TextInputLayout;

public class SignupPage2 extends AppCompatActivity {

    private TextInputEditText editFirstName, editLastName, editPhone;

    private TextInputLayout layoutFirstName, layoutLastName, layoutPhone;
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
        editPhone     = findViewById(R.id.editPhone);

       layoutFirstName = findViewById(R.id.layoutFirstName);
       layoutLastName  = findViewById(R.id.layoutLastName);
       layoutPhone     = findViewById(R.id.layoutPhone);


        Button btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(v -> {
            String fname = safe(editFirstName);
            String lname = safe(editLastName);
            String phone = safe(editPhone);

            boolean valid = true;


            layoutFirstName.setError(null);
            layoutLastName.setError(null);
            layoutPhone.setError(null);

            // 🔹 Validate first name
            // 🔹 Validate first name
            if (fname.isEmpty()) {
                layoutFirstName.setError("First name is required");
                layoutFirstName.setErrorIconDrawable(R.drawable.error);
                valid = false;
            } else if (!fname.matches("^[a-zA-Z]+$")) {  // only letters allowed
                layoutFirstName.setError("First name must contain only letters");
                layoutFirstName.setErrorIconDrawable(R.drawable.error);
                valid = false;
            }

            // 🔹 Validate last name
            if (lname.isEmpty()) {
                layoutLastName.setError("Last name is required");
                layoutLastName.setErrorIconDrawable(R.drawable.error);
                valid = false;
            } else if (!lname.matches("^[a-zA-Z]+$")) {  // only letters allowed
                layoutLastName.setError("Last name must contain only letters");
                layoutLastName.setErrorIconDrawable(R.drawable.error);
                valid = false;
            }

            // 🔹 Validate phone
            if (phone.isEmpty()) {
                layoutPhone.setError("Phone number is required");
                layoutPhone.setErrorIconDrawable(R.drawable.error);
                valid = false;
            } else if (!phone.matches("^09\\d{9}$")) {  // must start with 09 + 9 more digits
                layoutPhone.setError("Invalid phone number (must start with 09 and be 11 digits)");
                layoutPhone.setErrorIconDrawable(R.drawable.error);
                valid = false;
            }

            if (valid) {
                data.firstName = fname;
                data.lastName = lname;
                data.phone = phone;

                Intent i = new Intent(this, SignUpPage3.class);
                i.putExtra("data", data);
                startActivity(i);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private String safe(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
