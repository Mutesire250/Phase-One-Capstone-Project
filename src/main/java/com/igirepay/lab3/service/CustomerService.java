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
    private static final Map<Integer, Customer> customerCache = new ConcurrentHashMap<>();

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public boolean registerCustomer(String fullName, String email, String phoneNumber, String pin) {
        if (fullName == null || fullName.trim().isEmpty()) return false;
        if (email == null || !email.contains("@")) return false;
        if (phoneNumber == null || phoneNumber.length() < 10) return false;
        if (pin == null || pin.length() != 4) return false;
        if (customerDAO.getCustomerByEmail(email) != null) return false;
        String hashed = BCrypt.hashpw(pin, BCrypt.gensalt(12));
        return customerDAO.addCustomer(new Customer(0, fullName, email, phoneNumber, hashed, "user"));
    }

    public Customer login(String email, String pin) throws AccountLockedException {
        Customer customer = customerDAO.getCustomerByEmail(email);
        if (customer == null) return null;
        if (customer.isLocked()) throw new AccountLockedException("Your account is locked due to 3 failed PIN attempts. Contact Admin.");

        String stored = customer.getPin();
        boolean pinValid;
        boolean wasLegacy = false;

        if (stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"))) {
            pinValid = BCrypt.checkpw(pin, stored);
        } else {
            pinValid = stored != null && stored.equals(pin);
            wasLegacy = pinValid;
        }

        if (pinValid) {
            customerDAO.resetFailedAttempts(customer.getId());
            customer.setFailedAttempts(0);
            if (wasLegacy) {
                String hashed = BCrypt.hashpw(pin, BCrypt.gensalt(12));
                customerDAO.updatePin(customer.getId(), hashed);
                customer.setPin(hashed);
            }
            customerCache.put(customer.getId(), customer);
            return customer;
        } else {
            customerDAO.incrementFailedAttempts(customer.getId());
            int newAttempts = customer.getFailedAttempts() + 1;
            customer.setFailedAttempts(newAttempts);
            if (newAttempts >= 3) {
                customerDAO.setLockedStatus(customer.getId(), true);
                customer.setLocked(true);
                throw new AccountLockedException("Your account has been locked after 3 failed PIN attempts.");
            }
            return null;
        }
    }

    public boolean changePin(int customerId, String newPin) {
        if (newPin == null || newPin.length() != 4) return false;
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt(12));
        boolean success = customerDAO.updatePin(customerId, hashed);
        if (success) customerCache.remove(customerId);
        return success;
    }

    public void migratePlainPins() {
        for (Customer c : customerDAO.getAllCustomers()) {
            String p = c.getPin();
            if (p == null) continue;
            if (!(p.startsWith("$2a$") || p.startsWith("$2b$") || p.startsWith("$2y$"))) {
                try {
                    customerDAO.updatePin(c.getId(), BCrypt.hashpw(p, BCrypt.gensalt(12)));
                } catch (Exception ex) {
                    System.out.println("Failed to migrate PIN for " + c.getEmail() + ": " + ex.getMessage());
                }
            }
        }
    }

    public Customer getCustomerById(int id) {
        if (customerCache.containsKey(id)) return customerCache.get(id);
        Customer customer = customerDAO.getCustomerById(id);
        if (customer != null) customerCache.put(id, customer);
        return customer;
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public boolean updateCustomer(int id, String fullName, String email, String phoneNumber) {
        Customer customer = getCustomerById(id);
        if (customer == null) return false;
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        boolean success = customerDAO.updateCustomer(customer);
        if (success) customerCache.put(id, customer);
        return success;
    }

    public boolean deleteCustomer(int id) {
        boolean success = customerDAO.deleteCustomer(id);
        if (success) customerCache.remove(id);
        return success;
    }

    public boolean unlockCustomer(int id) {
        Customer customer = getCustomerById(id);
        if (customer == null) return false;
        customerDAO.setLockedStatus(id, false);
        customerDAO.resetFailedAttempts(id);
        customer.setLocked(false);
        customer.setFailedAttempts(0);
        customerCache.put(id, customer);
        return true;
    }
}
