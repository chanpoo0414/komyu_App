package com.example.komyuapplication;

import retrofit2.Call;
import retrofit2.Callback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AuthRepository {
    private final APIService api;

    public AuthRepository() {
        api = APIClient.getInstance().create(APIService.class);
    }

    public void register(SignUPData d, Callback<APIResponse> cb) {
        // If DOB was MM/dd/yy, convert; if already yyyy-MM-dd, convertDob() returns same string
        String dob = convertDob(d.dateOfBirth);
        int age = d.age == null ? 0 : d.age;

        Call<APIResponse> call = api.register(
                d.username,
                d.email,
                d.password,
                d.firstName,
                d.lastName,
                dob,
                d.address,
                age,
                d.phone == null ? "" : d.phone
        );
        call.enqueue(cb);
    }

    private String convertDob(String mmddyy) {
        if (mmddyy == null || mmddyy.isEmpty()) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("MM/dd/yy", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return out.format(in.parse(mmddyy));
        } catch (ParseException e) {
            // If it wasn't MM/dd/yy, assume it's already OK (e.g., yyyy-MM-dd)
            return mmddyy;
        }
    }
}
