-- Database Schema untuk Aplikasi Bank

CREATE DATABASE IF NOT EXISTS bank_app;
USE bank_app;

-- Tabel User
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Account (Parent class untuk semua jenis akun)
CREATE TABLE accounts (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type ENUM('SAVINGS', 'CHECKING', 'BUSINESS') NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Tabel untuk Savings Account (tambahan atribut khusus)
CREATE TABLE savings_accounts (
    account_id INT PRIMARY KEY,
    interest_rate DECIMAL(5, 2) DEFAULT 2.5,
    minimum_balance DECIMAL(15, 2) DEFAULT 1000.00,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- Tabel untuk Checking Account (tambahan atribut khusus)
CREATE TABLE checking_accounts (
    account_id INT PRIMARY KEY,
    overdraft_limit DECIMAL(15, 2) DEFAULT 0.00,
    monthly_fee DECIMAL(10, 2) DEFAULT 5.00,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- Tabel untuk Business Account (tambahan atribut khusus)
CREATE TABLE business_accounts (
    account_id INT PRIMARY KEY,
    business_name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(50),
    transaction_limit DECIMAL(15, 2) DEFAULT 100000.00,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- Tabel Transaction
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    target_account_id INT,
    description VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') DEFAULT 'SUCCESS',
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (target_account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

-- Index untuk performa
CREATE INDEX idx_user_accounts ON accounts(user_id);
CREATE INDEX idx_account_transactions ON transactions(account_id);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_username ON users(username);