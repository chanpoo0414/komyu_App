package com.example.komyuapplication;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @FormUrlEncoded
    @POST("register.php") // keep this path if your PHP is in public_html/API/register.php
    Call<APIResponse> register(
            @Field("User_name") String username,
            @Field("Email") String email,
            @Field("Password") String password,
            @Field("F_name") String firstName,
            @Field("L_name") String lastName,
            @Field("Date_Of_Birth") String dateOfBirth,
            @Field("Address") String address,
            @Field("Age") int age,
            @Field("Phone_Number") String phone
    );
    @FormUrlEncoded
    @POST("login.php")
    Call<APIResponse> login(
            @Field("User_name") String email,
            @Field("Password") String password
    );
}
