-- =========================================================
-- HealthFirst PIMS (Pharmacy Inventory Management System)
-- Database creation, table structure, and sample seed data
-- =========================================================

CREATE DATABASE IF NOT EXISTS pims_db;
USE pims_db;

-- ---------------------------------------------------------
-- Table: users
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

-- ---------------------------------------------------------
-- Table: suppliers
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

-- ---------------------------------------------------------
-- Table: medicines
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100),
    medicine_type VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 0,
    expiry_date DATE,
    supplier_id INT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

-- ---------------------------------------------------------
-- Table: sales
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ---------------------------------------------------------
-- Table: sale_items
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT,
    medicine_id INT,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

-- ---------------------------------------------------------
-- Sample data
-- ---------------------------------------------------------

-- Default login credentials (also documented in README):
--   Admin:   admin / admin123
--   Cashier: cashier / cash123
INSERT INTO users (username, password, role, full_name) VALUES
    ('admin', 'admin123', 'Admin', 'System Administrator'),
    ('cashier', 'cash123', 'Cashier', 'Front Counter Cashier');

INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
    ('MediCorp Distributors', 'Thandi Nkosi', '011-555-0101', 'sales@medicorp.co.za', '12 Main Rd, Johannesburg'),
    ('PharmaPlus Supplies', 'Johan van der Merwe', '021-555-0202', 'orders@pharmaplus.co.za', '45 Long St, Cape Town');

INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
    ('Paracetamol 500mg', 'Adcock Ingram', 'Tablet', 25.99, 150, 30, '2027-03-15', 1),
    ('Amoxicillin 250mg', 'Aspen Pharmacare', 'Capsule', 89.50, 40, 20, '2026-11-01', 1),
    ('Cough Syrup 100ml', 'Pharma Dynamics', 'Syrup', 45.00, 25, 15, '2026-10-05', 2),
    ('Insulin Injection', 'Novo Nordisk', 'Injection', 320.00, 12, 10, '2026-09-30', 2),
    ('Hydrocortisone Cream', 'GSK', 'Cream', 60.75, 8, 10, '2026-12-20', 1);