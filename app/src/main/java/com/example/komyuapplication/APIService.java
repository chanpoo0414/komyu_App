package com.example.komyuapplication;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @FormUrlEncoded
    @POST("commuterRegister.php")
    Call<APIResponse> register(
            @Field("Username") String username,
            @Field("Email") String email,
            @Field("Password") String password,
            @Field("First_name") String firstName,
            @Field("Middle_name") String middleName,
            @Field("Last_name") String lastName,
            @Field("Date_Of_Birth") String dateOfBirth,
            @Field("Address") String address,
            @Field("Sex") String sex,
            @Field("Type_of_Id") String typeOfId,
            @Field("Front_Id") String frontId,
            @Field("Back_Id") String backId,
            @Field("Categories") String categories,
            @Field("Phone_Number") String phone
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<APIResponse> login(
            @Field("Username") String username,
            @Field("Password") String password
    );

//    newly added here 09-23-25
    @FormUrlEncoded
    @POST("getUserProfile.php")
    Call<APIResponse> getUserProfile(
            @Field("Username") String username
    );

    @FormUrlEncoded
    @POST("topup.php")
    Call<APIResponse> topUpPoints(
            @Field("commuter_id") String commuterId,
            @Field("points") String points,
            @Field("counter") String counter
    );

    @FormUrlEncoded
    @POST("getRecentActivity.php")
    Call<APIResponse> getRecentActivity(
            @Field("Username") String username
    );

    @FormUrlEncoded
    @POST("getPendingTransactions.php")
    Call<APIResponse> getPendingTransactions(
            @Field("commuter_id") String commuterId
    );

    @FormUrlEncoded
    @POST("updateProfile.php")
    Call<APIResponse> updateProfile(
            @Field("username") String username,
            @Field("first_name") String firstName,
            @Field("middle_name") String middleName,
            @Field("last_name") String lastName,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("address") String address
    );

    @FormUrlEncoded
    @POST("process_payment.php")
    Call<APIResponse> processBusPayment(
            @Field("username") String username,
            @Field("bus_number") String busNumber,
            @Field("driver_id") String driverId,
            @Field("conductor_id") String conductorId,
            @Field("fare") double fare
    );

}