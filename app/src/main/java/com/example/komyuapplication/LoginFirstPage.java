package com.example.komyuapplication;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginFirstPage extends AppCompatActivity {

    // Hardcoded test credentials
    private final String correctUsername = "testuser";
    private final String correctPassword = "password123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_first_page);

        // Fields
        TextInputEditText editUsername = findViewById(R.id.editUsername);
        TextInputEditText editPassword = findViewById(R.id.editPassword);
        Button signIn = findViewById(R.id.btnSignIn);
        TextView forgotPassword = findViewById(R.id.textForgotPassword);
        TextView signup = findViewById(R.id.textGoToSignup);

        // Forgot Password redirect
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Signup redirect
        signup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });

        // Login button logic
        signIn.setOnClickListener(v -> {
            String enteredUsername = editUsername.getText().toString().trim();
            String enteredPassword = editPassword.getText().toString().trim();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            } else if (enteredUsername.equals(correctUsername) && enteredPassword.equals(correctPassword)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish(); // optional: prevents going back to login
            } else {
                Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
            }
        });

        // Insets handler
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}