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

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editUsername, editEmail, editPass, editComfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        editUsername     = findViewById(R.id.editUsername);
        editEmail        = findViewById(R.id.editEmail);
        editPass         = findViewById(R.id.editPass);
        editComfirmPass  = findViewById(R.id.editComfirmPass);

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            String username = safe(editUsername);
            String email    = safe(editEmail);
            String pass     = safe(editPass);
            String confirm  = safe(editComfirmPass);

            // --- validation ---
            if (TextUtils.isEmpty(username)) {
                toast("Username is required"); return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Enter a valid email"); return;
            }
            if (pass.length() < 6) {
                toast("Password must be at least 6 characters"); return;
            }
            if (!pass.equals(confirm)) {
                toast("Passwords do not match"); return;
            }

            // pack into SignUPData and go to page 2
            SignUPData data = new SignUPData();
            data.username = username;
            data.email    = email;
            data.password = pass;

            Intent i = new Intent(this, SignupPage2.class);
            i.putExtra("data", data);
            startActivity(i);
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
