package com.example.komyuapplication;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIService {

    @Multipart
    @POST("upload_image.php")
    Call<APIResponse> uploadImage(
            @Part MultipartBody.Part image,
            @Part("file_name") okhttp3.RequestBody fileName
    );

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
    @POST("getTransactionHistory.php")
    Call<APIResponse> getTransactionHistory(
            @Field("Username") String username,
            @Field("days") int days // 30 days history
    );

    @FormUrlEncoded
    @POST("getPendingTransactions.php")
    Call<APIResponse> getPendingTransactions(
            @Field("commuter_id") String commuterId
    );



    @FormUrlEncoded
    @POST("debug_payment.php")  // TEMPORARY DEBUG ENDPOINT
    Call<APIResponse> processBusPaymentDebug(
            @Field("username") String username,
            @Field("bus_number") String busNumber,
            @Field("driver_id") String driverId,
            @Field("conductor_id") String conductorId,
            @Field("fare") double fare
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

    @FormUrlEncoded
    @POST("submit_rating.php")
    Call<APIResponse> submitRating(
            @Field("commuter_id") String commuterId,
            @Field("commuter_name") String commuterName,
            @Field("transaction_id") String transactionId,
            @Field("driver_name") String driverName,
            @Field("conductor_name") String conductorName,
            @Field("bus_number") String busNumber,
            @Field("route") String route,
            @Field("overall_rating") int overallRating,
            @Field("driver_rating") int driverRating,
            @Field("conductor_rating") int conductorRating,
            @Field("report_type") String reportType,
            @Field("comments") String comments
    );

    @GET("submit_rating_get.php")
    Call<APIResponse> submitRatingGet(
            @Query("cid") String commuterId,
            @Query("cname") String commuterName,
            @Query("tid") String transactionId,
            @Query("dname") String driverName,
            @Query("cond_name") String conductorName, // Changed from "cname2" to "cond_name"
            @Query("bus") String busNumber,
            @Query("rt") String route,
            @Query("orating") int overallRating,
            @Query("drating") int driverRating,
            @Query("crating") int conductorRating,
            @Query("rtype") String reportType,
            @Query("cmt") String comments
    );

    //    kuha na ang reviewed rates
    @FormUrlEncoded
    @POST("check_rating_status.php")
    Call<APIResponse> checkRatingStatus(
            @Field("transaction_id") String transactionId,
            @Field("commuter_id") String commuterId
    );

    @FormUrlEncoded
    @POST("getReportDetails.php")
    Call<APIResponse> getReportDetails(
            @Field("report_id") String reportId
    );


    @FormUrlEncoded
    @POST("Update_profile.php")
    Call<APIResponse> updateProfile(
            @Field("commuter_id") String commuterId,
            @Field("username") String username,
            @Field("first_name") String firstName,
            @Field("middle_name") String middleName,
            @Field("last_name") String lastName,
            @Field("email") String email,
            @Field("phone_number") String phoneNumber,
            @Field("address") String address,
            @Field("date_of_birth") String dateOfBirth
    );

    @POST("getCurrentUser.php")
    Call<APIResponse> getCurrentUser();

}