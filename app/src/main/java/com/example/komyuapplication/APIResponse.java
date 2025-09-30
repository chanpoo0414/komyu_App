package com.example.komyuapplication;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class APIResponse {
    public int success;
    public String message;
    public String commuter_id;

//    added 9-23-25 down
    public User user;
    public List<RecentActivity> recent_activity;


    public static class User {
        public int id;

        @SerializedName("commuter_id")
        public String commuterId;

        public String username;
        public String email;

        @SerializedName("first_name")
        public String firstName;

        @SerializedName("middle_name")
        public String middleName;

        @SerializedName("last_name")
        public String lastName;

        @SerializedName("full_name")
        public String fullName;

        @SerializedName("date_of_birth")
        public String dateOfBirth;

        public String address;
        public String sex;

        @SerializedName("type_of_id")
        public String typeOfId;

        @SerializedName("front_id")
        public String frontId;

        @SerializedName("back_id")
        public String backId;

        public String categories;

        @SerializedName("phone_number")
        public String phoneNumber;

        public double balance;

        @SerializedName("profile_picture")
        public String profilePicture;



//        comment out these cuz they no longer exist in the CommutersDB(table)
        @SerializedName("total_points_spent")
        public double totalPointsSpent;

        @SerializedName("points_balance")  // Changed from "balance"
        public double pointsBalance;
        @SerializedName("trip_count")
        public int tripCount;

        @SerializedName("account_created")
        public String accountCreated;
    }

    public static class RecentActivity {
        public String type;
        public String title;
        public String subtitle;
        public String amount;
        public String date;
        public String transaction_code;
        public String icon; // "bus" or "points"
    }

    public static class BusQRData {
        public String bus;
        public String driver_id;
        public String conductor_id;
        public String driver_name;
        public String conductor_name;
        public String type;
        public FareData fares;
        public String timestamp;
    }

    public static class FareData {
        public Double regular;
        public Double student;
        @SerializedName("Senior Citizen")
        public Double senior_citizen;
        public Double pwd;
    }


    public static class PaymentData {
        public int trip_id;
        public double fare_paid;
        public double new_balance;
        public String user_category;
        public String bus_number;
        public String driver_name;
        public String conductor_name;
    }

}