-- 尝试方案1：使用标准名称
ALTER TABLE borrow_records
DROP FOREIGN KEY borrow_records_ibfk_1;

-- 尝试方案2：使用其他可能的名称（borrow_records + book_id + fk）
ALTER TABLE borrow_records
DROP FOREIGN KEY borrow_records_book_id_fk;

-- 添加新的外键约束，使用ON DELETE CASCADE
ALTER TABLE borrow_records
ADD CONSTRAINT borrow_records_book_fk
FOREIGN KEY (book_id) REFERENCES books(id)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- 添加一些调试信息
SELECT 'ALTER_CONSTRAINTS.SQL 脚本执行完成' AS message; 