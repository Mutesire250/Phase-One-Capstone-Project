-- Migration: expand pin column to hold bcrypt hashes
ALTER TABLE customers ALTER COLUMN pin TYPE VARCHAR(128);
