-- Migration: add role column to customers
ALTER TABLE customers ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'user';

-- Ensure admin user has admin role
UPDATE customers SET role = 'admin' WHERE email = 'admin@igirepay.com';
