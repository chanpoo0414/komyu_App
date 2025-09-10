package com.example.komyuapplication;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @FormUrlEncoded
    @POST("register.php") // keep this path if your PHP is in public_html/API/register.php
    Call<APIResponse> register(
            @Field("Username") String username,
            @Field("Email") String email,
            @Field("Password") String password,
            @Field("First_name") String firstName,
            @Field("Last_name") String lastName,
            @Field("Date_Of_Birth") String dateOfBirth,
            @Field("Address") String address,
            @Field("Sex") String sex,
            @Field("Type_of_Id") String typeOfIdd,
            @Field("Front_Id") String frontID,
            @Field("Back_Id") String backID,
            @Field("Categories") String categories,
            @Field("Phone_Number") String phone
    );
    @FormUrlEncoded
    @POST("login.php")
    Call<APIResponse> login(
            @Field("Username") String username,
            @Field("Password") String password
    );
}
