package com.igirepay.lab3.service;

import com.igirepay.lab2.dao.CustomerDAO;
import com.igirepay.lab1.model.Customer;
import com.igirepay.lab3.exception.AccountLockedException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerService {

    private CustomerDAO customerDAO;

    // Exercise 1.3 compliance: Map to cache customer accounts (keyed by ID)
    private static final Map<Integer, Customer> customerCache = new ConcurrentHashMap<>();

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    // Register new customer (stores hashed PIN)
    public boolean registerCustomer(String fullName, String email, String phoneNumber, String pin) {
        // Basic validation
        if (fullName == null || fullName.trim().isEmpty()) {
            System.out.println(" Full name cannot be empty!");
            return false;
        }
        if (email == null || !email.contains("@")) {
            System.out.println(" Invalid email address!");
            return false;
        }
        if (phoneNumber == null || phoneNumber.length() < 10) {
            System.out.println(" Phone number must be at least 10 digits!");
            return false;
        }
        if (pin == null || pin.length() != 4) {
            System.out.println("PIN must be exactly 4 digits!");
            return false;
        }

        // Check if customer already exists by email
        Customer existing = customerDAO.getCustomerByEmail(email);
        if (existing != null) {
            System.out.println(" Customer with this email already exists!");
            return false;
        }

        String hashed = BCrypt.hashpw(pin, BCrypt.gensalt(12));
        Customer newCustomer = new Customer(0, fullName, email, phoneNumber, hashed, "user");
        return customerDAO.addCustomer(newCustomer);
    }

    // Login customer by email and PIN (verifies hashed PIN)
    // Demonstrates: Authentication, brute force account locking, and custom exception propagation.
    public Customer login(String email, String pin) throws AccountLockedException {
        Customer customer = customerDAO.getCustomerByEmail(email);
        if (customer == null) {
            System.out.println(" Customer not found!");
            return null;
        }

        // Check if account is locked
        if (customer.isLocked()) {
            throw new AccountLockedException("Your account is locked due to 3 failed PIN attempts. Contact Admin.");
        }

        String stored = customer.getPin();
        boolean pinValid = false;
        boolean wasLegacy = false;

        if (stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"))) {
            pinValid = BCrypt.checkpw(pin, stored);
        } else {
            // Legacy plaintext PIN verification
            pinValid = stored != null && stored.equals(pin);
            wasLegacy = pinValid;
        }

        if (pinValid) {
            // Successful login -> Reset failed attempts in database and cache
            customerDAO.resetFailedAttempts(customer.getId());
            customer.setFailedAttempts(0);
            
            if (wasLegacy) {
                // Migrate to BCrypt hash
                String hashed = BCrypt.hashpw(pin, BCrypt.gensalt(12));
                customerDAO.updatePin(customer.getId(), hashed);
                customer.setPin(hashed);
                System.out.println(" PIN migrated to secure BCrypt hash.");
            }

            // Cache the logged-in customer profile
            customerCache.put(customer.getId(), customer);
            System.out.println(" Login successful! Welcome " + customer.getFullName());
            return customer;
        } else {
            // Failed PIN attempt -> Increment failed attempts
            customerDAO.incrementFailedAttempts(customer.getId());
            int newAttempts = customer.getFailedAttempts() + 1;
            customer.setFailedAttempts(newAttempts);

            System.out.println("Invalid PIN attempt. Attempt " + newAttempts + "/3");

            if (newAttempts >= 3) {
                customerDAO.setLockedStatus(customer.getId(), true);
                customer.setLocked(true);
                throw new AccountLockedException("Your account has been locked after 3 failed PIN attempts.");
            }
            return null;
        }
    }

    // Change a customer's PIN (admin or user reset). Stores hashed PIN.
    public boolean changePin(int customerId, String newPin) {
        if (newPin == null || newPin.length() != 4) return false;
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt(12));
        boolean success = customerDAO.updatePin(customerId, hashed);
        if (success) {
            customerCache.remove(customerId); // Invalidate cache
        }
        return success;
    }

    // Migrate any plaintext PINs to hashed values (idempotent)
    public void migratePlainPins() {
        List<Customer> all = customerDAO.getAllCustomers();
        for (Customer c : all) {
            String p = c.getPin();
            if (p == null) continue;
            if (!(p.startsWith("$2a$") || p.startsWith("$2b$") || p.startsWith("$2y$"))) {
                try {
                    String hashed = BCrypt.hashpw(p, BCrypt.gensalt(12));
                    customerDAO.updatePin(c.getId(), hashed);
                    System.out.println("Migrated customer " + c.getEmail() + " PIN to hashed.");
                } catch (Exception ex) {
                    System.out.println("Failed to migrate PIN for " + c.getEmail() + ": " + ex.getMessage());
                }
            }
        }
    }

    // Get customer by ID (checks in-memory cache first)
    // Demonstrates: Java Collections (Map) caching for performance.
    public Customer getCustomerById(int id) {
        if (customerCache.containsKey(id)) {
            return customerCache.get(id);
        }
        Customer customer = customerDAO.getCustomerById(id);
        if (customer != null) {
            customerCache.put(id, customer);
        }
        return customer;
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    // Update customer details
    public boolean updateCustomer(int id, String fullName, String email, String phoneNumber) {
        Customer customer = getCustomerById(id); // hits cache if available
        if (customer == null) {
            System.out.println(" Customer not found!");
            return false;
        }

        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);

        boolean success = customerDAO.updateCustomer(customer);
        if (success) {
            customerCache.put(id, customer); // update cache
        }
        return success;
    }

    // Delete customer
    public boolean deleteCustomer(int id) {
        boolean success = customerDAO.deleteCustomer(id);
        if (success) {
            customerCache.remove(id); // remove from cache
        }
        return success;
    }

    // Unlock customer account
    // Demonstrates: Basic Authentication state modification.
    public boolean unlockCustomer(int id) {
        Customer customer = getCustomerById(id);
        if (customer == null) return false;

        customerDAO.setLockedStatus(id, false);
        customerDAO.resetFailedAttempts(id);

        customer.setLocked(false);
        customer.setFailedAttempts(0);
        customerCache.put(id, customer); // update cache
        return true;
    }
}