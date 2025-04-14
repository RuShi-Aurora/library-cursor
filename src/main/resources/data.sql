-- 清除现有数据（如果需要）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE borrow_records;
TRUNCATE TABLE books;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据，使用ON DUPLICATE KEY UPDATE避免主键冲突
INSERT INTO users (username, password, email, role, status, created_at, updated_at)
VALUES
    ('admin', '123456', 'admin@example.com', 'ADMIN', 'ACTIVE', NOW(), NOW()),
    ('librarian', '123456', 'librarian@example.com', 'ADMIN', 'ACTIVE', NOW(), NOW()),
    ('user1', '123456', 'user1@example.com', 'USER', 'ACTIVE', NOW(), NOW()),
    ('user2', '123456', 'user2@example.com', 'USER', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
                     password = VALUES(password),
                     email = VALUES(email),
                     role = VALUES(role),
                     status = VALUES(status),
                     updated_at = NOW();

-- 添加示例图书，使用ON DUPLICATE KEY UPDATE避免ISBN冲突
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
('资本论', '卡尔·马克思', '9787010009735', '经济', 3, 'AVAILABLE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
                     title = VALUES(title),
                     author = VALUES(author),
                     category = VALUES(category),
                     stock = VALUES(stock),
                     status = VALUES(status),
                     updated_at = NOW();

-- 删除所有现有借阅记录后再添加新的
DELETE FROM borrow_records;

-- 添加一些示例借阅记录
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