# 🗄️ MySQL Best Practices & Interview Guide (3 Years Experience)

**Focus:** Database Design, Query Optimization, Indexing, Transactions, Performance, and Security

This comprehensive guide contains **30+ MySQL interview questions** with production-ready examples and best practices.

---

## Table of Contents
1. [Database Design & Normalization](#1-database-design--normalization)
2. [Indexing Strategies](#2-indexing-strategies)
3. [Query Optimization](#3-query-optimization)
4. [Transactions & Locking](#4-transactions--locking)
5. [Performance Tuning](#5-performance-tuning)
6. [Security Best Practices](#6-security-best-practices)
7. [Replication & High Availability](#7-replication--high-availability)
8. [Advanced SQL Techniques](#8-advanced-sql-techniques)

---

## 1. Database Design & Normalization

### Q1: Design a scalable e-commerce database schema
**Scenario:** Design tables for users, products, orders, and payments

```sql
-- Users table
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    status ENUM('active', 'inactive', 'suspended') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Addresses table (one-to-many with users)
CREATE TABLE addresses (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    address_type ENUM('billing', 'shipping') NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_user_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Categories table
CREATE TABLE categories (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    parent_id INT UNSIGNED NULL,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent (parent_id),
    INDEX idx_slug (slug),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Products table
CREATE TABLE products (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    category_id INT UNSIGNED NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2),
    stock_quantity INT UNSIGNED DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_sku (sku),
    INDEX idx_category (category_id),
    INDEX idx_price (price),
    INDEX idx_active (is_active),
    FULLTEXT idx_fulltext (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Orders table
CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    subtotal DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) DEFAULT 0,
    shipping_cost DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    billing_address_id BIGINT UNSIGNED NOT NULL,
    shipping_address_id BIGINT UNSIGNED NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (billing_address_id) REFERENCES addresses(id),
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(id),
    INDEX idx_user (user_id),
    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Order items table (many-to-many between orders and products)
CREATE TABLE order_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity INT UNSIGNED NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Payments table
CREATE TABLE payments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    payment_method ENUM('credit_card', 'paypal', 'bank_transfer', 'cash') NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    gateway_response JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order (order_id),
    INDEX idx_transaction (transaction_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Best Practices:**
✅ Use appropriate data types (BIGINT for IDs, DECIMAL for money)  
✅ Add indexes on foreign keys and frequently queried columns  
✅ Use ENUM for fixed value sets  
✅ Implement cascading deletes where appropriate  
✅ Use utf8mb4 for full Unicode support  
✅ Add timestamps for audit trails

---

### Q2: Normalization - From 1NF to 3NF
**Scenario:** Normalize a poorly designed table

**Unnormalized Table (Bad):**
```sql
CREATE TABLE customer_orders (
    order_id INT,
    customer_name VARCHAR(100),
    customer_email VARCHAR(100),
    customer_phone VARCHAR(20),
    products VARCHAR(500), -- "Laptop,Mouse,Keyboard"
    prices VARCHAR(100),   -- "999.99,25.99,75.00"
    order_date DATE
);
```

**Problems:**
❌ Repeating customer data (violates 2NF)  
❌ Multi-valued attributes (violates 1NF)  
❌ Update anomalies

**Normalized Design (Good - 3NF):**
```sql
-- 1NF: Atomic values
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- 2NF: No partial dependencies
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    INDEX idx_customer (customer_id),
    INDEX idx_date (order_date)
) ENGINE=InnoDB;

CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    INDEX idx_name (name)
) ENGINE=InnoDB;

-- 3NF: No transitive dependencies
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;
```

---

### Q3: When to Denormalize?
**Scenario:** High-read, reporting-heavy application

```sql
-- Normalized (slower for reports)
SELECT 
    o.order_id,
    c.name,
    c.email,
    p.name as product_name,
    oi.quantity,
    oi.unit_price
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH);

-- Denormalized (faster, but requires maintenance)
CREATE TABLE order_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    customer_name VARCHAR(100),
    customer_email VARCHAR(100),
    product_name VARCHAR(100),
    quantity INT,
    unit_price DECIMAL(10, 2),
    order_date DATE,
    INDEX idx_date (order_date),
    INDEX idx_customer_email (customer_email)
) ENGINE=InnoDB;

-- Keep in sync with trigger
DELIMITER $$
CREATE TRIGGER after_order_insert
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    INSERT INTO order_summary (order_id, customer_name, customer_email, product_name, quantity, unit_price, order_date)
    SELECT 
        o.order_id,
        c.name,
        c.email,
        p.name,
        NEW.quantity,
        NEW.unit_price,
        o.order_date
    FROM orders o
    JOIN customers c ON o.customer_id = c.customer_id
    JOIN products p ON NEW.product_id = p.product_id
    WHERE o.order_id = NEW.order_id;
END$$
DELIMITER ;
```

**When to Denormalize:**
✅ Read-heavy workloads (reports, analytics)  
✅ Data warehouse / OLAP systems  
✅ Caching frequently accessed computed values  
⚠️ Requires careful maintenance with triggers or application logic

---

## 2. Indexing Strategies

### Q4: Index Types and When to Use Them

```sql
-- Primary Key (Clustered Index in InnoDB)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255)
);

-- Unique Index
CREATE UNIQUE INDEX idx_email ON users(email);
-- Or during creation:
-- email VARCHAR(255) UNIQUE

-- Regular Index (B-Tree)
CREATE INDEX idx_created_at ON users(created_at);

-- Composite Index (leftmost prefix rule)
CREATE INDEX idx_name_email ON users(last_name, first_name, email);
-- Useful for:
-- WHERE last_name = 'Smith'
-- WHERE last_name = 'Smith' AND first_name = 'John'
-- WHERE last_name = 'Smith' AND first_name = 'John' AND email = 'john@example.com'
-- NOT useful for:
-- WHERE first_name = 'John' (doesn't use leftmost)

-- Full-Text Index
CREATE TABLE articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    FULLTEXT idx_fulltext (title, content)
) ENGINE=InnoDB;

-- Usage:
SELECT * FROM articles
WHERE MATCH(title, content) AGAINST('mysql optimization' IN NATURAL LANGUAGE MODE);

-- Spatial Index (for geographic data)
CREATE TABLE locations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    coordinates POINT NOT NULL,
    SPATIAL INDEX idx_coordinates (coordinates)
) ENGINE=InnoDB;

-- Covering Index (includes all columns needed for query)
CREATE INDEX idx_covering ON orders(user_id, status, created_at, total);
-- This query can be satisfied entirely from the index:
SELECT status, created_at, total 
FROM orders 
WHERE user_id = 123;
```

---

### Q5: Analyze Query Performance with EXPLAIN

```sql
-- Check query execution plan
EXPLAIN SELECT 
    u.name,
    COUNT(o.id) as order_count,
    SUM(o.total) as total_spent
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.status = 'active'
GROUP BY u.id;

-- Better format
EXPLAIN FORMAT=JSON SELECT ...;

-- Analyze actual execution
EXPLAIN ANALYZE SELECT ...;
```

**Key EXPLAIN columns to watch:**
- **type**: ALL (bad, full table scan), index, range, ref, eq_ref, const (good)
- **possible_keys**: Indexes MySQL might use
- **key**: Index MySQL actually uses
- **rows**: Estimated rows to examine (lower is better)
- **Extra**: Using filesort (slow), Using temporary (slow), Using index (fast)

**Optimization based on EXPLAIN:**
```sql
-- Bad: type = ALL (full table scan)
SELECT * FROM orders WHERE YEAR(created_at) = 2024;

-- Good: type = range (uses index)
SELECT * FROM orders 
WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01';
```

---

### Q6: Index Maintenance and Monitoring

```sql
-- Check index usage
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'your_database'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- Find unused indexes (enable in MySQL 8.0+)
SELECT 
    object_schema,
    object_name,
    index_name
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE index_name IS NOT NULL
AND count_star = 0
AND object_schema = 'your_database'
ORDER BY object_name;

-- Find duplicate indexes
SELECT 
    a.TABLE_NAME,
    a.INDEX_NAME as index1,
    b.INDEX_NAME as index2,
    a.COLUMN_NAME
FROM information_schema.STATISTICS a
JOIN information_schema.STATISTICS b 
    ON a.TABLE_SCHEMA = b.TABLE_SCHEMA
    AND a.TABLE_NAME = b.TABLE_NAME
    AND a.COLUMN_NAME = b.COLUMN_NAME
    AND a.SEQ_IN_INDEX = b.SEQ_IN_INDEX
    AND a.INDEX_NAME < b.INDEX_NAME
WHERE a.TABLE_SCHEMA = 'your_database';

-- Rebuild fragmented indexes
OPTIMIZE TABLE orders;
-- Or
ALTER TABLE orders ENGINE=InnoDB;
```

---

## 3. Query Optimization

### Q7: Solve the N+1 Query Problem

**Bad Approach (N+1 queries):**
```sql
-- First query (1)
SELECT id, name FROM users LIMIT 10;

-- Then for each user (N queries)
SELECT * FROM orders WHERE user_id = 1;
SELECT * FROM orders WHERE user_id = 2;
-- ... 10 more queries
```

**Good Approach (2 queries):**
```sql
-- Query 1: Get users
SELECT id, name FROM users LIMIT 10;

-- Query 2: Get all orders at once
SELECT * FROM orders WHERE user_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
```

**Best Approach (1 query with JOIN):**
```sql
SELECT 
    u.id,
    u.name,
    o.id as order_id,
    o.total,
    o.status,
    o.created_at
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
ORDER BY u.id, o.created_at DESC;
```

---

### Q8: Efficient Pagination

**Bad Approach (OFFSET becomes slow with large offsets):**
```sql
-- Page 1000 with 20 items per page = OFFSET 19980
SELECT * FROM products
ORDER BY created_at DESC
LIMIT 20 OFFSET 19980; -- Scans and skips 19980 rows!
```

**Good Approach (Keyset pagination):**
```sql
-- First page
SELECT * FROM products
ORDER BY id DESC
LIMIT 20;

-- Next page (use last ID from previous page)
SELECT * FROM products
WHERE id < 19980
ORDER BY id DESC
LIMIT 20;
```

**With timestamp-based pagination:**
```sql
-- First page
SELECT * FROM products
ORDER BY created_at DESC, id DESC
LIMIT 20;

-- Next page
SELECT * FROM products
WHERE created_at < '2024-01-15 10:30:00'
   OR (created_at = '2024-01-15 10:30:00' AND id < 1234)
ORDER BY created_at DESC, id DESC
LIMIT 20;

-- Create composite index for this
CREATE INDEX idx_created_id ON products(created_at DESC, id DESC);
```

---

### Q9: Optimize Subqueries with JOINs

**Bad (Correlated Subquery - executes for each row):**
```sql
SELECT 
    u.id,
    u.name,
    (SELECT COUNT(*) FROM orders WHERE user_id = u.id) as order_count
FROM users u;
```

**Good (JOIN - single execution):**
```sql
SELECT 
    u.id,
    u.name,
    COUNT(o.id) as order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name;
```

**Another example:**
```sql
-- Bad: Subquery in WHERE
SELECT * FROM products
WHERE category_id IN (
    SELECT id FROM categories WHERE is_active = 1
);

-- Good: JOIN
SELECT p.* FROM products p
INNER JOIN categories c ON p.category_id = c.id
WHERE c.is_active = 1;
```

---

### Q10: Window Functions (MySQL 8.0+)

```sql
-- Running total
SELECT 
    order_date,
    total,
    SUM(total) OVER (ORDER BY order_date) as running_total
FROM orders;

-- Rank by revenue per category
SELECT 
    product_id,
    category_id,
    revenue,
    RANK() OVER (PARTITION BY category_id ORDER BY revenue DESC) as rank_in_category
FROM product_sales;

-- Moving average (last 7 days)
SELECT 
    date,
    revenue,
    AVG(revenue) OVER (
        ORDER BY date 
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) as moving_avg_7_days
FROM daily_sales;

-- Row number with pagination
SELECT * FROM (
    SELECT 
        *,
        ROW_NUMBER() OVER (ORDER BY created_at DESC) as row_num
    FROM products
) temp
WHERE row_num BETWEEN 21 AND 40; -- Page 2 (items 21-40)
```

---

### Q11: Common Table Expressions (CTEs)

```sql
-- Recursive CTE for hierarchical data (categories tree)
WITH RECURSIVE category_tree AS (
    -- Base case: root categories
    SELECT id, name, parent_id, 1 as level
    FROM categories
    WHERE parent_id IS NULL
    
    UNION ALL
    
    -- Recursive case: child categories
    SELECT c.id, c.name, c.parent_id, ct.level + 1
    FROM categories c
    INNER JOIN category_tree ct ON c.parent_id = ct.id
)
SELECT * FROM category_tree
ORDER BY level, name;

-- Multiple CTEs for complex queries
WITH 
active_users AS (
    SELECT id, name, email
    FROM users
    WHERE status = 'active'
),
user_orders AS (
    SELECT 
        user_id,
        COUNT(*) as order_count,
        SUM(total) as total_spent
    FROM orders
    WHERE status = 'completed'
    GROUP BY user_id
)
SELECT 
    u.name,
    u.email,
    COALESCE(uo.order_count, 0) as orders,
    COALESCE(uo.total_spent, 0) as spent
FROM active_users u
LEFT JOIN user_orders uo ON u.id = uo.user_id
ORDER BY uo.total_spent DESC;
```

---

## 4. Transactions & Locking

### Q12: ACID Properties Implementation

```sql
-- Example: Transfer money between accounts
START TRANSACTION;

-- 1. Check source account balance
SELECT balance INTO @source_balance
FROM accounts
WHERE id = 1
FOR UPDATE; -- Lock the row

-- 2. Verify sufficient funds
SET @amount = 100.00;
IF @source_balance < @amount THEN
    ROLLBACK;
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient funds';
END IF;

-- 3. Debit source account
UPDATE accounts
SET balance = balance - @amount,
    updated_at = NOW()
WHERE id = 1;

-- 4. Credit destination account
UPDATE accounts
SET balance = balance + @amount,
    updated_at = NOW()
WHERE id = 2;

-- 5. Log transaction
INSERT INTO transactions (from_account, to_account, amount, created_at)
VALUES (1, 2, @amount, NOW());

-- 6. Commit if all successful
COMMIT;

-- Or use stored procedure
DELIMITER $$
CREATE PROCEDURE transfer_money(
    IN p_from_account INT,
    IN p_to_account INT,
    IN p_amount DECIMAL(10,2)
)
BEGIN
    DECLARE v_source_balance DECIMAL(10,2);
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    SELECT balance INTO v_source_balance
    FROM accounts
    WHERE id = p_from_account
    FOR UPDATE;
    
    IF v_source_balance < p_amount THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient funds';
    END IF;
    
    UPDATE accounts SET balance = balance - p_amount WHERE id = p_from_account;
    UPDATE accounts SET balance = balance + p_amount WHERE id = p_to_account;
    
    INSERT INTO transactions (from_account, to_account, amount, created_at)
    VALUES (p_from_account, p_to_account, p_amount, NOW());
    
    COMMIT;
END$$
DELIMITER ;

-- Usage
CALL transfer_money(1, 2, 100.00);
```

---

### Q13: Transaction Isolation Levels

```sql
-- Show current isolation level
SELECT @@transaction_isolation;

-- Set isolation level
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- Different isolation levels:

-- 1. READ UNCOMMITTED (dirty reads possible)
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
SELECT * FROM accounts; -- May see uncommitted changes
COMMIT;

-- 2. READ COMMITTED (default in many systems) 
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;
SELECT * FROM accounts WHERE id = 1; -- Sees only committed data
-- Another transaction commits here
SELECT * FROM accounts WHERE id = 1; -- May see different value (non-repeatable read)
COMMIT;

-- 3. REPEATABLE READ (MySQL default, prevents non-repeatable reads)
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
START TRANSACTION;
SELECT * FROM accounts WHERE id = 1; -- Value: 100
-- Another transaction updates and commits
SELECT * FROM accounts WHERE id = 1; -- Still sees 100 (repeatable read)
COMMIT;

-- 4. SERIALIZABLE (strictest, prevents phantom reads)
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
START TRANSACTION;
SELECT * FROM accounts WHERE balance > 1000;
-- Another transaction cannot insert/update rows matching this condition
COMMIT;
```

**Isolation Level Comparison:**
| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|-------|-----------|-------------------|--------------|
| READ UNCOMMITTED | ✅ Possible | ✅ Possible | ✅ Possible |
| READ COMMITTED | ❌ No | ✅ Possible | ✅ Possible |
| REPEATABLE READ | ❌ No | ❌ No | ✅ Possible* |
| SERIALIZABLE | ❌ No | ❌ No | ❌ No |

*MySQL InnoDB prevents phantom reads at REPEATABLE READ level

---

### Q14: Deadlock Detection and Prevention

```sql
-- Show recent deadlocks
SHOW ENGINE INNODB STATUS;

-- Example: Deadlock scenario
-- Transaction 1:
START TRANSACTION;
UPDATE accounts SET balance = balance - 10 WHERE id = 1; -- Locks row 1
-- ... delay ...
UPDATE accounts SET balance = balance + 10 WHERE id = 2; -- Waits for row 2
COMMIT;

-- Transaction 2 (concurrent):
START TRANSACTION;
UPDATE accounts SET balance = balance - 10 WHERE id = 2; -- Locks row 2
-- ... delay ...
UPDATE accounts SET balance = balance + 10 WHERE id = 1; -- Waits for row 1 (DEADLOCK!)
COMMIT;

-- Prevention: Always lock in same order
-- Transaction 1 & 2 (fixed):
START TRANSACTION;
UPDATE accounts SET balance = balance - 10 WHERE id = 1; -- Always lock lower ID first
UPDATE accounts SET balance = balance + 10 WHERE id = 2;
COMMIT;

-- Or use explicit locking order
START TRANSACTION;
SELECT * FROM accounts WHERE id IN (1, 2) ORDER BY id FOR UPDATE;
-- Now perform updates
COMMIT;

-- Deadlock handling in application
DELIMITER $$
CREATE PROCEDURE safe_transfer(
    IN p_from INT,
    IN p_to INT,
    IN p_amount DECIMAL(10,2)
)
BEGIN
    DECLARE v_retries INT DEFAULT 3;
    DECLARE EXIT HANDLER FOR 1213 -- Deadlock error code
    BEGIN
        IF v_retries > 0 THEN
            SET v_retries = v_retries - 1;
            -- Retry transaction
        ELSE
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed after retries';
        END IF;
    END;
    
    -- Transaction logic here
END$$
DELIMITER ;
```

---

## 5. Performance Tuning

### Q15: Query Cache and Configuration (MySQL 5.x)

```sql
-- Note: Query cache removed in MySQL 8.0, use Redis/Memcached instead

-- MySQL 5.x configuration
SET GLOBAL query_cache_size = 67108864; -- 64MB
SET GLOBAL query_cache_type = 1;
SET GLOBAL query_cache_limit = 1048576; -- 1MB

-- Check cache statistics
SHOW STATUS LIKE 'Qcache%';

-- For MySQL 8.0+: Use application-level caching
-- Example with PHP + Redis:
/*
$redis = new Redis();
$redis->connect('127.0.0.1', 6379);

$cacheKey = 'user:' . $userId;
$userData = $redis->get($cacheKey);

if (!$userData) {
    $userData = // fetch from MySQL
    $redis->setex($cacheKey, 3600, json_encode($userData));
}
*/
```

---

### Q16: Partitioning for Large Tables

```sql
-- Range partitioning (by date)
CREATE TABLE orders_partitioned (
    id BIGINT AUTO_INCREMENT,
    user_id INT NOT NULL,
    total DECIMAL(10, 2),
    status VARCHAR(20),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id, created_at)
) ENGINE=InnoDB
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p_2021 VALUES LESS THAN (2022),
    PARTITION p_2022 VALUES LESS THAN (2023),
    PARTITION p_2023 VALUES LESS THAN (2024),
    PARTITION p_2024 VALUES LESS THAN (2025),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- List partitioning (by region)
CREATE TABLE customers_partitioned (
    id INT AUTO_INCREMENT,
    name VARCHAR(100),
    country VARCHAR(50),
    PRIMARY KEY (id, country)
) ENGINE=InnoDB
PARTITION BY LIST COLUMNS(country) (
    PARTITION p_north_america VALUES IN ('USA', 'Canada', 'Mexico'),
    PARTITION p_europe VALUES IN ('UK', 'Germany', 'France'),
    PARTITION p_asia VALUES IN ('China', 'Japan', 'India'),
    PARTITION p_other VALUES IN (DEFAULT)
);

-- Hash partitioning (distribute evenly)
CREATE TABLE sessions (
    id BIGINT AUTO_INCREMENT,
    user_id INT,
    session_data TEXT,
    PRIMARY KEY (id, user_id)
) ENGINE=InnoDB
PARTITION BY HASH(user_id)
PARTITIONS 10;

-- Partition maintenance
ALTER TABLE orders_partitioned ADD PARTITION (
    PARTITION p_2025 VALUES LESS THAN (2026)
);

ALTER TABLE orders_partitioned DROP PARTITION p_2021;

-- Check partitions
SELECT 
    TABLE_NAME,
    PARTITION_NAME,
    PARTITION_METHOD,
    TABLE_ROWS
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = 'your_database'
AND TABLE_NAME = 'orders_partitioned';
```

---

### Q17: Optimize Bulk Operations

```sql
-- Bad: Individual inserts
INSERT INTO logs (message, created_at) VALUES ('Log 1', NOW());
INSERT INTO logs (message, created_at) VALUES ('Log 2', NOW());
-- ... 10,000 more inserts

-- Good: Bulk insert
INSERT INTO logs (message, created_at) VALUES
('Log 1', NOW()),
('Log 2', NOW()),
('Log 3', NOW()),
-- ... batch of 1000
('Log 1000', NOW());

-- Best: LOAD DATA INFILE (fastest)
LOAD DATA INFILE '/tmp/logs.csv'
INTO TABLE logs
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(message, created_at);

-- Bulk updates (avoid row-by-row)
-- Bad:
UPDATE products SET price = price * 1.1 WHERE id = 1;
UPDATE products SET price = price * 1.1 WHERE id = 2;

-- Good: Single update with CASE
UPDATE products
SET price = CASE
    WHEN id = 1 THEN 100.00
    WHEN id = 2 THEN 200.00
    WHEN id = 3 THEN 150.00
    ELSE price
END
WHERE id IN (1, 2, 3);

-- Disable indexes during bulk load
ALTER TABLE logs DISABLE KEYS;
-- ... perform bulk insert ...
ALTER TABLE logs ENABLE KEYS;

-- Use INSERT IGNORE to skip duplicates
INSERT IGNORE INTO users (email, name) VALUES
('user1@example.com', 'User 1'),
('user2@example.com', 'User 2');

-- Or ON DUPLICATE KEY UPDATE
INSERT INTO users (email, name, login_count)
VALUES ('user@example.com', 'User', 1)
ON DUPLICATE KEY UPDATE 
    login_count = login_count + 1,
    last_login = NOW();
```

---

### Q18: Monitoring and Slow Query Log

```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 1;
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow-query.log';
SET GLOBAL long_query_time = 2; -- Queries taking > 2 seconds

-- Log queries not using indexes
SET GLOBAL log_queries_not_using_indexes = 1;

-- Check performance schema
SELECT 
    DIGEST_TEXT,
    COUNT_STAR,
    AVG_TIMER_WAIT / 1000000000000 as avg_sec,
    SUM_TIMER_WAIT / 1000000000000 as total_sec
FROM performance_schema.events_statements_summary_by_digest
ORDER BY SUM_TIMER_WAIT DESC
LIMIT 10;

-- Table statistics
SELECT 
    table_schema,
    table_name,
    table_rows,
    AVG_ROW_LENGTH,
    DATA_LENGTH,
    INDEX_LENGTH,
    (DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024 as size_mb
FROM information_schema.TABLES
WHERE table_schema = 'your_database'
ORDER BY size_mb DESC;

-- Connection statistics
SHOW PROCESSLIST;
SHOW STATUS LIKE 'Threads%';
SHOW STATUS LIKE 'Connections';
```

---

## 6. Security Best Practices

### Q19: SQL Injection Prevention

```sql
-- Bad: Vulnerable to SQL injection
SET @email = "admin@example.com' OR '1'='1";
SET @query = CONCAT('SELECT * FROM users WHERE email = "', @email, '"');
PREPARE stmt FROM @query;
EXECUTE stmt;
-- This would return ALL users!

-- Good: Use prepared statements (parameterized queries)
PREPARE stmt FROM 'SELECT * FROM users WHERE email = ?';
SET @email = 'admin@example.com';
EXECUTE stmt USING @email;
DEALLOCATE PREPARE stmt;

-- In stored procedures (safe)
DELIMITER $$
CREATE PROCEDURE get_user(IN p_email VARCHAR(255))
BEGIN
    SELECT * FROM users WHERE email = p_email;
END$$
DELIMITER ;

CALL get_user("admin@example.com' OR '1'='1"); -- Safe, treated as literal
```

---

### Q20: User Privileges and Least Privilege Principle

```sql
-- Create application user with minimal permissions
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'strong_password';

-- Grant only necessary permissions
GRANT SELECT, INSERT, UPDATE ON myapp.* TO 'app_user'@'localhost';

-- For read-only reporting user
CREATE USER 'reporting_user'@'%' IDENTIFIED BY 'password';
GRANT SELECT ON myapp.* TO 'reporting_user'@'%';

-- For specific tables only
GRANT SELECT, INSERT, UPDATE ON myapp.orders TO 'orders_service'@'192.168.1.%';
GRANT SELECT, INSERT, UPDATE ON myapp.order_items TO 'orders_service'@'192.168.1.%';

-- Never grant ALL or admin privileges to application users
-- Bad:
GRANT ALL PRIVILEGES ON *.* TO 'app_user'@'%'; -- NEVER DO THIS!

-- Good: Specific permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON myapp.* TO 'app_user'@'localhost';

-- Remove unused privileges
REVOKE DELETE ON myapp.* FROM 'app_user'@'localhost';

-- Check user privileges
SHOW GRANTS FOR 'app_user'@'localhost';

-- Require SSL connections
CREATE USER 'secure_user'@'%' IDENTIFIED BY 'password' REQUIRE SSL;

-- Limit connections
CREATE USER 'api_user'@'%' 
IDENTIFIED BY 'password'
WITH MAX_QUERIES_PER_HOUR 10000
     MAX_CONNECTIONS_PER_HOUR 1000
     MAX_USER_CONNECTIONS 50;
```

---

### Q21: Audit Logging with Triggers

```sql
-- Create audit table
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50),
    operation VARCHAR(10),
    old_value JSON,
    new_value JSON,
    user VARCHAR(100),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_table (table_name),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- Audit trigger for updates
DELIMITER $$
CREATE TRIGGER users_after_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, operation, old_value, new_value, user, ip_address)
    VALUES (
        'users',
        'UPDATE',
        JSON_OBJECT(
            'id', OLD.id,
            'email', OLD.email,
            'status', OLD.status
        ),
        JSON_OBJECT(
            'id', NEW.id,
            'email', NEW.email,
            'status', NEW.status
        ),
        USER(),
        COALESCE(@client_ip, 'unknown')
    );
END$$

-- Audit trigger for deletes
CREATE TRIGGER users_before_delete
BEFORE DELETE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (table_name, operation, old_value, user, ip_address)
    VALUES (
        'users',
        'DELETE',
        JSON_OBJECT(
            'id', OLD.id,
            'email', OLD.email,
            'status', OLD.status
        ),
        USER(),
        COALESCE(@client_ip, 'unknown')
    );
END$$
DELIMITER ;

-- Set client IP before operations (in application)
SET @client_ip = '192.168.1.100';
UPDATE users SET status = 'inactive' WHERE id = 123;

-- Query audit log
SELECT 
    operation,
    JSON_EXTRACT(old_value, '$.email') as old_email,
    JSON_EXTRACT(new_value, '$.email') as new_email,
    user,
    created_at
FROM audit_log
WHERE table_name = 'users'
AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY created_at DESC;
```

---

## 7. Replication & High Availability

### Q22: MySQL Replication Setup

```sql
-- On MASTER server
-- 1. Configure my.cnf
/*
[mysqld]
server-id = 1
log_bin = /var/log/mysql/mysql-bin.log
binlog_format = ROW
binlog_do_db = myapp
*/

-- 2. Create replication user
CREATE USER 'repl_user'@'%' IDENTIFIED BY 'strong_password';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';
FLUSH PRIVILEGES;

-- 3. Get master status
FLUSH TABLES WITH READ LOCK;
SHOW MASTER STATUS;
-- Note: File and Position values

-- 4. Dump database
-- mysqldump -u root -p myapp > /tmp/myapp.sql

-- 5. Unlock tables
UNLOCK TABLES;

-- On SLAVE server
-- 1. Configure my.cnf
/*
[mysqld]
server-id = 2
relay-log = /var/log/mysql/relay-bin
log_bin = /var/log/mysql/mysql-bin.log
read_only = 1
*/

-- 2. Import dump
-- mysql -u root -p myapp < /tmp/myapp.sql

-- 3. Configure replication
CHANGE MASTER TO
    MASTER_HOST = '192.168.1.10',
    MASTER_USER = 'repl_user',
    MASTER_PASSWORD = 'strong_password',
    MASTER_LOG_FILE = 'mysql-bin.000001',
    MASTER_LOG_POS = 1234;

-- 4. Start replication
START SLAVE;

-- 5. Check status
SHOW SLAVE STATUS\G

-- Monitor replication lag
SELECT 
    TIMESTAMPDIFF(SECOND, ts, NOW()) as lag_seconds
FROM (
    SELECT FROM_UNIXTIME(VARIABLE_VALUE) as ts
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Slave_last_heartbeat'
) t;
```

---

### Q23: Read/Write Splitting Strategy

```sql
-- Application logic (pseudo-code with SQL)

-- Write operations (go to MASTER)
-- INSERT, UPDATE, DELETE on master
INSERT INTO users (name, email) VALUES ('John', 'john@example.com');

-- Read operations (go to SLAVE)
-- SELECT from slave (may have slight delay)
SELECT * FROM products WHERE category_id = 5;

-- Critical reads (must be up-to-date) go to MASTER
SELECT * FROM users WHERE id = 123 FOR UPDATE;

-- Implementation example (PHP with PDO)
/*
class Database {
    private $master;
    private $slaves = [];
    
    public function __construct() {
        $this->master = new PDO('mysql:host=master;dbname=myapp', 'user', 'pass');
        $this->slaves[] = new PDO('mysql:host=slave1;dbname=myapp', 'user', 'pass');
        $this->slaves[] = new PDO('mysql:host=slave2;dbname=myapp', 'user', 'pass');
    }
    
    public function query($sql, $forceWriteDb = false) {
        if ($this->isWriteQuery($sql) || $forceWriteDb) {
            return $this->master->query($sql);
        } else {
            $slave = $this->slaves[array_rand($this->slaves)];
            return $slave->query($sql);
        }
    }
    
    private function isWriteQuery($sql) {
        return preg_match('/^(INSERT|UPDATE|DELETE|ALTER|CREATE|DROP)/i', trim($sql));
    }
}
*/
```

---

## 8. Advanced SQL Techniques

### Q24: JSON Data Type (MySQL 5.7+)

```sql
-- Create table with JSON column
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    preferences JSON,
    metadata JSON
) ENGINE=InnoDB;

-- Insert JSON data
INSERT INTO users (name, preferences, metadata) VALUES
('John', 
 JSON_OBJECT('theme', 'dark', 'language', 'en', 'notifications', true),
 JSON_OBJECT('created_from', 'mobile', 'version', '2.1')),
('Jane',
 JSON_OBJECT('theme', 'light', 'language', 'es', 'notifications', false),
 JSON_OBJECT('created_from', 'web', 'version', '2.0'));

-- Query JSON data
SELECT name,
       JSON_EXTRACT(preferences, '$.theme') as theme,
       preferences->>'$.language' as language
FROM users;

-- Search in JSON
SELECT * FROM users
WHERE JSON_EXTRACT(preferences, '$.theme') = 'dark';

-- Or with ->
SELECT * FROM users
WHERE preferences->>'$.notifications' = 'true';

-- Update JSON
UPDATE users
SET preferences = JSON_SET(preferences, '$.theme', 'auto')
WHERE id = 1;

-- Add to JSON object
UPDATE users
SET preferences = JSON_INSERT(preferences, '$.fontSize', 14)
WHERE id = 1;

-- Remove from JSON
UPDATE users
SET preferences = JSON_REMOVE(preferences, '$.fontSize')
WHERE id = 1;

-- Create index on JSON field (virtual column)
ALTER TABLE users 
ADD COLUMN theme VARCHAR(20) AS (preferences->>'$.theme') VIRTUAL,
ADD INDEX idx_theme (theme);

-- Array operations
CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    tags JSON
);

INSERT INTO posts (title, tags) VALUES
('MySQL Tips', JSON_ARRAY('mysql', 'database', 'sql')),
('PHP Tutorial', JSON_ARRAY('php', 'programming', 'web'));

-- Check if array contains value
SELECT * FROM posts
WHERE JSON_CONTAINS(tags, '"mysql"');

-- Get array length
SELECT title, JSON_LENGTH(tags) as tag_count
FROM posts;
```

---

### Q25: Full-Text Search

```sql
-- Create table with FULLTEXT index
CREATE TABLE articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT idx_fulltext (title, content)
) ENGINE=InnoDB;

-- Insert sample data
INSERT INTO articles (title, content) VALUES
('MySQL Performance Tuning', 'Learn how to optimize MySQL queries for better performance...'),
('Database Indexing Guide', 'Understanding indexes and their impact on query performance...'),
('SQL Best Practices', 'Follow these SQL best practices to write efficient queries...');

-- Natural language search
SELECT 
    id,
    title,
    MATCH(title, content) AGAINST('mysql performance') as relevance
FROM articles
WHERE MATCH(title, content) AGAINST('mysql performance' IN NATURAL LANGUAGE MODE)
ORDER BY relevance DESC;

-- Boolean mode search
SELECT * FROM articles
WHERE MATCH(title, content) AGAINST('+mysql -oracle' IN BOOLEAN MODE);
-- + means must include
-- - means must not include

-- With query expansion (finds related terms)
SELECT * FROM articles
WHERE MATCH(title, content) AGAINST('database' WITH QUERY EXPANSION);

-- Minimum word length configuration (my.cnf)
/*
[mysqld]
ft_min_word_len = 3
innodb_ft_min_token_size = 3
*/

-- Check stopwords
SELECT * FROM INFORMATION_SCHEMA.INNODB_FT_DEFAULT_STOPWORD;
```

---

### Q26: Stored Procedures and Functions

```sql
-- Stored Procedure with parameters
DELIMITER $$
CREATE PROCEDURE get_top_customers(
    IN p_limit INT,
    IN p_start_date DATE,
    OUT p_total_revenue DECIMAL(10,2)
)
BEGIN
    SELECT SUM(total) INTO p_total_revenue
    FROM orders
    WHERE created_at >= p_start_date;
    
    SELECT 
        u.id,
        u.name,
        COUNT(o.id) as order_count,
        SUM(o.total) as total_spent
    FROM users u
    JOIN orders o ON u.id = o.user_id
    WHERE o.created_at >= p_start_date
    GROUP BY u.id, u.name
    ORDER BY total_spent DESC
    LIMIT p_limit;
END$$
DELIMITER ;

-- Call procedure
CALL get_top_customers(10, '2024-01-01', @revenue);
SELECT @revenue as total_revenue;

-- Stored Function
DELIMITER $$
CREATE FUNCTION calculate_discount(
    p_total DECIMAL(10,2),
    p_user_level VARCHAR(20)
) RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE v_discount DECIMAL(5,2);
    
    SET v_discount = CASE
        WHEN p_user_level = 'VIP' THEN 0.20
        WHEN p_user_level = 'PREMIUM' THEN 0.15
        WHEN p_user_level = 'REGULAR' THEN 0.10
        ELSE 0.05
    END;
    
    RETURN p_total * (1 - v_discount);
END$$
DELIMITER ;

-- Use function in query
SELECT 
    order_id,
    total,
    calculate_discount(total, user_level) as discounted_total
FROM orders o
JOIN users u ON o.user_id = u.id;

-- Drop procedure/function
DROP PROCEDURE IF EXISTS get_top_customers;
DROP FUNCTION IF EXISTS calculate_discount;
```

---

## 🎯 MySQL Performance Checklist

### Query Optimization
✅ Use EXPLAIN to analyze query execution plans  
✅ Avoid SELECT *, select only needed columns  
✅ Use indexes on WHERE, JOIN, ORDER BY columns  
✅ Avoid functions on indexed columns in WHERE  
✅ Use LIMIT for large result sets  
✅ Use JOIN instead of subqueries when possible  
✅ Use covering indexes for better performance

### Indexing
✅ Create indexes on foreign keys  
✅ Use composite indexes for multi-column queries  
✅ Monitor and remove unused indexes  
✅ Use index hints only when necessary  
✅ Keep indexes small (use appropriate data types)

### Schema Design
✅ Use appropriate data types (INT vs BIGINT)  
✅ Use NOT NULL when possible  
✅ Normalize to 3NF (denormalize strategically)  
✅ Use ENUM for fixed value sets  
✅ Partition very large tables

### Configuration
✅ Tune InnoDB buffer pool size (70-80% of RAM)  
✅ Enable slow query log  
✅ Set appropriate connection limits  
✅ Configure query cache (MySQL 5.x)  
✅ Use connection pooling in application

### Security
✅ Use prepared statements (prevent SQL injection)  
✅ Apply least privilege principle to users  
✅ Never store passwords in plain text  
✅ Enable SSL for connections  
✅ Regular backups and backup testing

---

**Interview Tips:**
1. **Understand the "why"** - Know why you choose specific indexes or query structures
2. **Think about scale** - How does your solution perform with 1M rows? 10M?
3. **Consider trade-offs** - Indexes speed reads but slow writes
4. **Real-world experience** - Share examples from projects you've worked on
5. **Know your limits** - It's okay to say "I'd need to research X" or "I'd test both approaches"

Good luck with your MySQL interviews! 🚀
