package com.example.komyuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpPage3 extends AppCompatActivity {

    private TextInputEditText editAddress, editAge, editPhone;
    private SignUPData data;
    private AuthRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page3);

        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) data = new SignUPData();

        repo = new AuthRepository();

        editAddress = findViewById(R.id.editAddress);
        editAge     = findViewById(R.id.editAge);
        editPhone   = findViewById(R.id.editPhone);

        Button btnRegister = findViewById(R.id.btnNext); // same id but it's the final submit
        btnRegister.setOnClickListener(v -> submit());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void submit() {
        String address = safe(editAddress);
        String ageStr  = safe(editAge);
        String phone   = safe(editPhone);

        if (TextUtils.isEmpty(address)) { toast("Address is required"); return; }
        if (TextUtils.isEmpty(ageStr))  { toast("Age is required");     return; }

        Integer age = null;
        try { age = Integer.parseInt(ageStr); }
        catch (NumberFormatException e) { toast("Age must be a number"); return; }

        data.address = address;
        data.age     = age;
        data.phone   = phone;

        // call API
        repo.register(data, new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    APIResponse body = response.body();
                    toast(body.message != null ? body.message : "Registered!");
                    // Go back to Login page
                    startActivity(new Intent(SignUpPage3.this, LoginFirstPage.class));
                    finish();
                } else {
                    toast("Registration failed. Try again.");
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                toast("Network error: " + t.getMessage());
            }
        });
    }

    private String safe(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
