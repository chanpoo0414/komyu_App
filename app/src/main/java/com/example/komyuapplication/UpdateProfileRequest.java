package com.example.komyuapplication;

public class UpdateProfileRequest {
    private String currentUsername;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String address;

    public UpdateProfileRequest(String currentUsername, String fullName, String username,
                                String email, String phone, String address) {
        this.currentUsername = currentUsername;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Add getters and setters here
    public String getCurrentUsername() { return currentUsername; }
    public void setCurrentUsername(String currentUsername) { this.currentUsername = currentUsername; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}