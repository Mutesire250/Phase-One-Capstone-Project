package com.igirepay.lab1.model;

public class Customer {

    private int id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String pin;
    private String role; // 'user' or 'admin'
    private int failedAttempts;
    private boolean locked;

    // Constructor
    public Customer(int id, String fullName, String email, String phoneNumber, String pin) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.role = "user";
        this.failedAttempts = 0;
        this.locked = false;
    }

    // Constructor with role
    public Customer(int id, String fullName, String email, String phoneNumber, String pin, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.role = role == null ? "user" : role;
        this.failedAttempts = 0;
        this.locked = false;
    }

    // Full constructor
    public Customer(int id, String fullName, String email, String phoneNumber, String pin, String role, int failedAttempts, boolean locked) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.role = role == null ? "user" : role;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}