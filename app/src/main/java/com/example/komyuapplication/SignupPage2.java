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

public class SignupPage2 extends AppCompatActivity {

    private TextInputEditText editFirstName, editLastName, editPhone;
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

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            String fname = safe(editFirstName);
            String lname = safe(editLastName);
            String phone = safe(editPhone);

            if (fname.isEmpty()) { toast("First name is required"); return; }
            if (lname.isEmpty()) { toast("Last name is required");  return; }
            if (phone.isEmpty()) { toast("Phone number is required"); return; }

            data.firstName   = fname;
            data.lastName    = lname;
            data.phone = phone;

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

    private String safe(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
