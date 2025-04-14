-- 先删除旧表（如果存在外键约束，需要按顺序删除）
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- 重新创建数据库（如果需要）
CREATE DATABASE IF NOT EXISTS library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library;

-- 创建用户表
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       last_login DATETIME,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_username (username),
                       INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建图书表（已包含status字段）
CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(100) NOT NULL,
                       author VARCHAR(100) NOT NULL,
                       isbn VARCHAR(20) NOT NULL UNIQUE,
                       publisher VARCHAR(100),
                       publish_date DATE,
                       category VARCHAR(50) NOT NULL,
                       description TEXT,
                       stock INT NOT NULL DEFAULT 0,
                       status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_isbn (isbn),
                       INDEX idx_title (title),
                       INDEX idx_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建借阅记录表
CREATE TABLE borrow_records (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                book_id BIGINT NOT NULL,
                                user_id BIGINT NOT NULL,
                                borrow_date DATETIME NOT NULL,
                                due_date DATETIME NOT NULL,
                                return_date DATETIME,
                                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                                INDEX idx_book_id (book_id),
                                INDEX idx_user_id (user_id),
                                INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建管理员账号（密码：123456）
-- 创建管理员账号（密码：123456）
INSERT INTO users (username, password, email, role, status, created_at, updated_at)
VALUES (
           'admin',
           '123456',  -- 明文密码"123456"
           'admin@example.com',
           'ADMIN',
           'ACTIVE',
           NOW(),
           NOW()
       );

-- 创建测试用户账号（密码：123456）
INSERT INTO users (username, password, email, role, status, created_at, updated_at)
VALUES (
           'user',
           '123456',
           'user@example.com',
           'USER',
           'ACTIVE',
           NOW(),
           NOW()
       );

-- 添加一些测试图书数据
INSERT INTO books (title, author, isbn, publisher, publish_date, category, description, stock, status)
VALUES
    ('Java编程思想', 'Bruce Eckel', '978-7111213826', '机械工业出版社', '2007-06-01', 'TECHNOLOGY', 'Java编程经典著作', 5, 'AVAILABLE'),
    ('深入理解Java虚拟机', '周志明', '978-7111641247', '机械工业出版社', '2019-12-01', 'TECHNOLOGY', 'JVM进阶必读', 3, 'AVAILABLE'),
    ('算法导论', 'Thomas H.Cormen', '978-7111407010', '机械工业出版社', '2012-09-01', 'TECHNOLOGY', '计算机科学经典教材', 2, 'AVAILABLE');

UPDATE users SET password = '123456' WHERE username = 'admin';
UPDATE users SET password = '123456' WHERE username = 'user';
-- 创建管理员账号
INSERT INTO users (username, password, email, role, status, created_at, updated_at)
VALUES
    ('admin', '123456', 'admin@example.com', 'ADMIN', 'ACTIVE', NOW(), NOW()),
    ('user', '123456', 'user@example.com', 'USER', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();