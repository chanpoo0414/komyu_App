package com.example.komyuapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class SignUpPage3 extends AppCompatActivity {

    private TextInputEditText editAddress, editDateBirth;
    private AutoCompleteTextView editSex;
    private SignUPData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page3);

        // ✅ Always bind views FIRST
        editDateBirth = findViewById(R.id.editDateBirth);
        editAddress   = findViewById(R.id.editAddress);
        editSex       = findViewById(R.id.dropDownSex);

        // ✅ Now setup dropdown choices
        String[] sexOptions = getResources().getStringArray(R.array.sex_options);
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sexOptions
        );
        editSex.setAdapter(sexAdapter);
        editSex.setOnClickListener(v -> editSex.showDropDown());

        // ✅ restore SignUPData
        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) data = new SignUPData();

        // ✅ disable keyboard on DOB, use DatePicker instead
        editDateBirth.setKeyListener(null);
        editDateBirth.setOnClickListener(v -> showDatePicker());

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> goNext());

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
            String mm = String.format("%02d", month + 1);
            String dd = String.format("%02d", dayOfMonth);
            String dob = year + "-" + mm + "-" + dd;
            editDateBirth.setText(dob);

        }, y, m, d);

        dp.getDatePicker().setMaxDate(System.currentTimeMillis());
        dp.show();
    }

    private void goNext() {
        String address = safe(editAddress);
        String sex     = safe(editSex);
        String dob     = safe(editDateBirth);

        if (TextUtils.isEmpty(dob))     { toast("Date of birth is required"); return; }
        if (TextUtils.isEmpty(sex))     { toast("Sex is required"); return; }
        if (TextUtils.isEmpty(address)) { toast("Address is required"); return; }

        data.address     = address;
        data.sex         = sex;
        data.dateOfBirth = dob;

        // move to SignupPage4 (not final submit yet)
        Intent i = new Intent(this, SignUpPage4.class);
        i.putExtra("data", data);
        startActivity(i);
    }

    private String safe(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private String safe(AutoCompleteTextView t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
