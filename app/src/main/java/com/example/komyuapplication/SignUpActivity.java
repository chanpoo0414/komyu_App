package com.example.komyuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editUsername, editEmail, editPass, editComfirmPass;

    private TextInputLayout LayoutUsername, LayoutEmail, LayoutPass, LayoutconfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        editUsername     = findViewById(R.id.editUsername);
        editEmail        = findViewById(R.id.editEmail);
        editPass         = findViewById(R.id.editPass);
        editComfirmPass  = findViewById(R.id.editComfirmPass);

        LayoutUsername = findViewById(R.id.LayoutUsername);
        LayoutEmail = findViewById(R.id.LayoutEmail);
        LayoutPass = findViewById(R.id.LayoutPass);
        LayoutconfirmPass = findViewById(R.id.LayoutconfirmPass);



        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            String username = safe(editUsername);
            String email    = safe(editEmail);
            String pass     = safe(editPass);
            String confirm  = safe(editComfirmPass);

            boolean valid = true;

            LayoutUsername.setError(null);
            LayoutEmail.setError(null);
            LayoutPass.setError(null);
            LayoutconfirmPass.setError(null);

            // --- validation ---
            // 🔹 Username validation
            if (TextUtils.isEmpty(username)) {
                LayoutUsername.setError("Username is required");
                LayoutUsername.setErrorIconDrawable(R.drawable.error);
                valid = false;
            }

            // 🔹 Email validation
            if (TextUtils.isEmpty(email)) {
                LayoutEmail.setError("Email is required");
                LayoutEmail.setErrorIconDrawable(R.drawable.error);
                valid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                LayoutEmail.setError("Enter a valid email");
                LayoutEmail.setErrorIconDrawable(R.drawable.error);
                valid = false;
            }

            // 🔹 Password validation
            if (TextUtils.isEmpty(pass)) {
                LayoutPass.setError("Password is required");
                LayoutPass.setErrorIconDrawable(R.drawable.error);
                valid = false;
            } else if (pass.length() < 6) {
                LayoutPass.setError("Password must be at least 6 characters");
                LayoutPass.setErrorIconDrawable(R.drawable.error);
                valid = false;
            } else {
                // 🔹 Confirm Password validation (only runs if password is valid length)
                if (TextUtils.isEmpty(confirm)) {
                    LayoutconfirmPass.setError("Confirm Password is required");
                    valid = false;
                } else if (!pass.equals(confirm)) {
                    LayoutconfirmPass.setError("Passwords do not match");
                    valid = false;
                }
            }

            // pack into SignUPData and go to page 2
            if (valid) {
                SignUPData data = new SignUPData();
                data.username = username;
                data.email    = email;
                data.password = pass;

                Intent i = new Intent(this, SignupPage2.class);
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
