package com.example.komyuapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class APIResponse {
    public int success;
    public String message;
    public String commuter_id;

    public User user;
    public PaymentData data;
    public RatingResponse rating_data;

    public List<RecentActivity> recent_activity;

    public List<TransactionHistory> transaction_history;
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

        @SerializedName("total_points_spent")
        public double totalPointsSpent;

        @SerializedName("points_balance")
        public double pointsBalance;

        @SerializedName("trip_count")
        public int tripCount;

        @SerializedName("account_created")
        public String accountCreated;
    }

    public static class RecentActivity implements Serializable {
        public String type;
        public String title;
        public String subtitle;
        public String amount;
        public String date;
        public String transaction_code;
        public String icon;

        // Add these new fields
        public String user_category;
        public String counter_location;
        public String driver_name;
        public String conductor_name;
        public String route;
        public String plate_number;
        public int is_reviewed; // Add this field for compatibility
    }

    public static class TransactionHistory implements Serializable {
        public String type;
        public String title;
        public String subtitle;
        public String amount;
        public String date;
        public String transaction_code;
        public String icon;
        public String status;
        public String counter_location;
        public String driver_name;
        public String conductor_name;
        public String route;
        public String plate_number;
        public String admin_name;  // ADD THIS FIELD
        public String remarks;
        public int is_reviewed;
        public String user_category;
        public boolean isRejectedTopup() {
            return "Failed".equals(status) && "topup".equals(type);
        }
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

    public static class RatingResponse {
        public int report_id;
        public String status;
        public String submitted_at;
        public int overall_rating;    // Add this
        public int driver_rating;     // Add this
        public int conductor_rating;  // Add this
        public String report_type;    // Add this
        public String remarks;        // Add this

        public RatingResponse() {}
    }

    public static class RatingData {
        public int report_id;
        public int overall_rating;
        public int driver_rating;
        public int conductor_rating;
        public String report_type;
    }

    public static class ReportDetails {
        public int report_id;
        public String commuter_name;
        public String commuter_id;
        public String employee_name;
        public String employee_id;
        public String type;
        public String status;
        public String reported_at;
        public int overall_rating;
        public int driver_rating;
        public int conductor_rating;
        public String remarks;
        public String action_taken;
        public String trip_id;
        public String bus_number;
        public String route;
        public String driver_name;
        public String conductor_name;
    }
}