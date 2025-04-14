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

-- 创建图书表
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
                                FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                INDEX idx_book_id (book_id),
                                INDEX idx_user_id (user_id),
                                INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入用户数据
INSERT INTO users (username, password, email, role, status, created_at, updated_at)
VALUES
    ('admin', '123456', 'admin@example.com', 'ADMIN', 'ACTIVE', NOW(), NOW()),
    ('librarian', '123456', 'librarian@example.com', 'ADMIN', 'ACTIVE', NOW(), NOW()),
    ('user1', '123456', 'user1@example.com', 'USER', 'ACTIVE', NOW(), NOW()),
    ('user2', '123456', 'user2@example.com', 'USER', 'ACTIVE', NOW(), NOW()),
    ('cursor', '111111', 'cursor@example.com', 'USER', 'ACTIVE', NOW(), NOW());

-- 添加图书数据
INSERT INTO books (title, author, isbn, category, stock, status, created_at, updated_at)
VALUES
-- 文学类
('活着', '余华', '9787506365437', '文学', 10, 'AVAILABLE', NOW(), NOW()),
('百年孤独', '加西亚·马尔克斯', '9787544253994', '文学', 5, 'AVAILABLE', NOW(), NOW()),
('红楼梦', '曹雪芹', '9787020002207', '文学', 8, 'AVAILABLE', NOW(), NOW()),
('1984', '乔治·奥威尔', '9787532751631', '文学', 6, 'AVAILABLE', NOW(), NOW()),

-- 计算机类
('深入理解Java虚拟机', '周志明', '9787111641247', '计算机', 5, 'AVAILABLE', NOW(), NOW()),
('算法导论', 'Thomas H.Cormen', '9787111407010', '计算机', 3, 'AVAILABLE', NOW(), NOW()),
('Spring实战（第5版）', 'Craig Walls', '9787115527929', '计算机', 7, 'AVAILABLE', NOW(), NOW()),
('Vue.js设计与实现', '霍春阳', '9787115583864', '计算机', 4, 'AVAILABLE', NOW(), NOW()),

-- 科学类
('时间简史', '史蒂芬·霍金', '9787535732309', '科学', 6, 'AVAILABLE', NOW(), NOW()),
('人类简史', '尤瓦尔·赫拉利', '9787508647357', '科学', 8, 'AVAILABLE', NOW(), NOW()),
('未来简史', '尤瓦尔·赫拉利', '9787508672069', '科学', 7, 'AVAILABLE', NOW(), NOW()),
('三体', '刘慈欣', '9787536692930', '科学', 10, 'AVAILABLE', NOW(), NOW()),

-- 经济类
('经济学原理', '曼昆', '9787301139431', '经济', 5, 'AVAILABLE', NOW(), NOW()),
('货币金融学', '弗雷德里克·米什金', '9787300078939', '经济', 4, 'AVAILABLE', NOW(), NOW()),
('国富论', '亚当·斯密', '9787100084161', '经济', 6, 'AVAILABLE', NOW(), NOW()),
('资本论', '卡尔·马克思', '9787010009735', '经济', 3, 'AVAILABLE', NOW(), NOW());

-- 添加借阅记录
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, status)
SELECT
    u.id as user_id,
    b.id as book_id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as borrow_date,
    DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY), INTERVAL 14 DAY) as due_date,
    CASE
        WHEN RAND() < 0.3 THEN NULL
        ELSE DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY), INTERVAL FLOOR(RAND() * 14) DAY)
        END as return_date,
    CASE
        WHEN RAND() < 0.2 THEN 'PENDING'
        WHEN RAND() < 0.4 THEN 'BORROWED'
        WHEN RAND() < 0.6 THEN 'RETURNED'
        ELSE 'REJECTED'
        END as status
FROM
    users u
        CROSS JOIN books b
WHERE
    u.role = 'USER'
  AND RAND() < 0.3
    LIMIT 20;

-- 更新图书库存
UPDATE books b
SET b.stock = GREATEST(0, b.stock - (
    SELECT COUNT(*)
    FROM borrow_records br
    WHERE br.book_id = b.id
      AND br.status IN ('PENDING', 'BORROWED')
));

-- 更新图书状态
UPDATE books
SET status = CASE
                 WHEN stock > 0 THEN 'AVAILABLE'
                 ELSE 'UNAVAILABLE'
    END;