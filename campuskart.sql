-- =====================================================================
-- CampusKart — MySQL 8.x schema and seed data
-- Local application: MySQL runs on the same machine as the Java server.
--
--   Apply with:   mysql -u root -p < campuskart.sql
--
-- Needs MySQL 8.0.16 or newer (older versions parse but IGNORE CHECK).
-- 3NF: every non-key column depends on the key, the whole key and nothing
-- but the key — category names are NOT stored inside items (only an id-free
-- plain column, kept simple for this mini build), seller details live only
-- in users, and a request stores only what depends on the (item, buyer) pair.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS campuskart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campuskart;

-- the application connects with this account (see dao/DBConnection.java)
CREATE USER IF NOT EXISTS 'campus'@'localhost' IDENTIFIED BY 'campus_pw';
GRANT ALL PRIVILEGES ON campuskart.* TO 'campus'@'localhost';
FLUSH PRIVILEGES;

DROP TABLE IF EXISTS purchase_requests;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------- users
CREATE TABLE users (
    user_id             INT AUTO_INCREMENT PRIMARY KEY,
    full_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(120) NOT NULL UNIQUE,
    password            VARCHAR(100) NOT NULL,           -- plain text in the course build (see README limits)
    role                ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT',
    enrollment_no       VARCHAR(30) NULL UNIQUE,         -- NULL for the admin
    department          VARCHAR(60) NULL,
    verification_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------- items
CREATE TABLE items (
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    seller_id      INT NOT NULL,
    category       VARCHAR(50) NOT NULL,                -- Books / Electronics / Stationery
    title          VARCHAR(120) NOT NULL,
    description    TEXT,
    item_condition ENUM('NEW','LIKE_NEW','GOOD','FAIR') NOT NULL,
    price          DECIMAL(10,2) NOT NULL CHECK (price > 0),
    status         ENUM('AVAILABLE','RESERVED','SOLD') DEFAULT 'AVAILABLE',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_seller_title (seller_id, title),      -- one listing per seller per title
    FOREIGN KEY (seller_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- --------------------------------------------------- purchase_requests
CREATE TABLE purchase_requests (
    request_id    INT AUTO_INCREMENT PRIMARY KEY,
    item_id       INT NOT NULL,
    buyer_id      INT NOT NULL,
    offered_price DECIMAL(10,2) NOT NULL CHECK (offered_price > 0),
    message       VARCHAR(300),
    status        ENUM('PENDING','ACCEPTED','REJECTED','COMPLETED') DEFAULT 'PENDING',
    requested_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at  TIMESTAMP NULL,                       -- set inside the completion TRANSACTION
    UNIQUE KEY uq_item_buyer (item_id, buyer_id),       -- one request per buyer per item
    FOREIGN KEY (item_id) REFERENCES items(item_id),    -- RESTRICT: an item with history cannot be deleted
    FOREIGN KEY (buyer_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- =====================================================================
-- Seed data (campus-flavoured). Everything below is also the demo path:
-- login as admin and approve Kabir, log in as Meera, accept a request,
-- complete it, and read the generated invoice file.
-- =====================================================================

INSERT INTO users (full_name, email, password, role, enrollment_no, department, verification_status) VALUES
 ('Admin SIT',   'admin@sitpune.edu', 'admin123', 'ADMIN',   NULL,          'CSE',        'APPROVED'),
 ('Meera Joshi', 'meera@sitpune.edu', 'pass1234', 'STUDENT', 'SIT2024001',  'CSE',        'APPROVED'),
 ('Aarav Patil', 'aarav@sitpune.edu', 'pass1234', 'STUDENT', 'SIT2024002',  'ENTC',       'APPROVED'),
 ('Kabir Rao',   'kabir@sitpune.edu', 'pass1234', 'STUDENT', 'SIT2024003',  'Mechanical', 'PENDING'),
 ('Sana Sheikh', 'sana@sitpune.edu',  'pass1234', 'STUDENT', 'SIT2024004',  'Civil',      'REJECTED');

INSERT INTO items (seller_id, category, title, description, item_condition, price, status) VALUES
 (2, 'Books',       'Core Java Volume 1',    'Fundamentals, 10th edition, solved examples',        'GOOD',     450.00,  'AVAILABLE'),
 (2, 'Electronics', 'Casio fx-991EX',        'Scientific calculator, barely used, bill available', 'LIKE_NEW', 850.00,  'AVAILABLE'),
 (3, 'Electronics', 'Arduino Uno Starter Kit','Board, sensors, jumper wires, carrying box',        'GOOD',     1200.00, 'AVAILABLE'),
 (3, 'Stationery',  'Engineering Drafter',   'Mini drafter with spare leads',                     'FAIR',     250.00,  'AVAILABLE'),
 (2, 'Books',       'Digital Logic Design',  'Previous year question bank, fully solved',         'GOOD',     300.00,  'SOLD');

-- one live PENDING request so the Requests screen has data on first run
INSERT INTO purchase_requests (item_id, buyer_id, offered_price, message, status) VALUES
 (1, 3, 400.00, 'Can you do 400? Collection from Hostel B.', 'PENDING');
