package com.example.cursorlibrary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/maintenance")
public class DbMaintenanceController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 构造函数，用于初始化和调试
    @Autowired
    public DbMaintenanceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("=== DbMaintenanceController 已初始化 ===");
        System.out.println("=== 映射路径: /api/admin/maintenance ===");
    }

    @PostMapping("/fix-constraints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fixForeignKeyConstraints() {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("收到修复外键约束请求");

            // 尝试获取当前外键名称
            try {
                System.out.println("正在查询当前数据库外键信息...");
                List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(
                    "SELECT * FROM information_schema.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_NAME = 'borrow_records' " +
                    "AND REFERENCED_TABLE_NAME = 'books'");
                
                if (!foreignKeys.isEmpty()) {
                    System.out.println("找到外键信息:");
                    for (Map<String, Object> fk : foreignKeys) {
                        System.out.println("  - 外键名称: " + fk.get("CONSTRAINT_NAME"));
                        System.out.println("  - 列名称: " + fk.get("COLUMN_NAME"));
                        System.out.println("  - 引用表: " + fk.get("REFERENCED_TABLE_NAME"));
                        System.out.println("  - 引用列: " + fk.get("REFERENCED_COLUMN_NAME"));
                    }
                } else {
                    System.out.println("未找到外键信息");
                }
            } catch (Exception e) {
                System.err.println("查询外键信息失败: " + e.getMessage());
            }
            
            // 最彻底的解决方案：先创建临时表，然后重建主表
            try {
                System.out.println("正在执行完整的表重构...");
                
                // 第1步：创建借阅记录的临时表（不包含外键约束）
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS borrow_records_temp LIKE borrow_records");
                System.out.println("创建临时表成功");
                
                // 第2步：复制数据到临时表
                jdbcTemplate.execute(
                    "INSERT INTO borrow_records_temp SELECT * FROM borrow_records");
                System.out.println("数据复制到临时表成功");
                
                // 第3步：删除原表
                jdbcTemplate.execute("DROP TABLE IF EXISTS borrow_records");
                System.out.println("删除原表成功");
                
                // 第4步：创建新表，使用正确的外键约束
                jdbcTemplate.execute(
                    "CREATE TABLE borrow_records (" +
                    "   id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "   book_id BIGINT NOT NULL," +
                    "   user_id BIGINT NOT NULL," +
                    "   borrow_date DATETIME NOT NULL," +
                    "   due_date DATETIME NOT NULL," +
                    "   return_date DATETIME," +
                    "   status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
                    "   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "   updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "   FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "   INDEX idx_book_id (book_id)," +
                    "   INDEX idx_user_id (user_id)," +
                    "   INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                System.out.println("创建新表成功");
                
                // 第5步：复制数据回新表
                jdbcTemplate.execute(
                    "INSERT INTO borrow_records SELECT * FROM borrow_records_temp");
                System.out.println("数据复制回新表成功");
                
                // 第6步：删除临时表
                jdbcTemplate.execute("DROP TABLE IF EXISTS borrow_records_temp");
                System.out.println("删除临时表成功");
                
                result.put("success", true);
                result.put("message", "数据库表重构完成，外键约束已修复为级联删除");
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                System.err.println("表重构失败: " + e.getMessage());
                e.printStackTrace();
                System.err.println("尝试其他修复方法...");
            }

            // 尝试直接修改外键约束
            boolean success = false;
            Exception lastError = null;
            
            // 尝试多个可能的外键名称
            String[] possibleForeignKeyNames = {
                "borrow_records_ibfk_1", 
                "borrow_records_book_id_fk", 
                "fk_borrow_records_book_id",
                "borrow_records_book_fk"
            };
            
            for (String fkName : possibleForeignKeyNames) {
                try {
                    System.out.println("尝试删除外键: " + fkName);
                    jdbcTemplate.execute("ALTER TABLE borrow_records DROP FOREIGN KEY " + fkName);
                    System.out.println("成功删除外键: " + fkName);
                    success = true;
                    break;
                } catch (Exception e) {
                    System.err.println("删除外键 " + fkName + " 失败: " + e.getMessage());
                    lastError = e;
                }
            }
            
            if (!success) {
                System.err.println("所有尝试都失败，无法找到正确的外键名称");
                if (lastError != null) {
                    throw lastError;
                } else {
                    throw new RuntimeException("无法找到正确的外键名称");
                }
            }
            
            // 添加新约束
            try {
                System.out.println("正在添加新的级联删除外键约束");
                jdbcTemplate.execute(
                    "ALTER TABLE borrow_records ADD CONSTRAINT borrow_records_book_fk " +
                    "FOREIGN KEY (book_id) REFERENCES books(id) " +
                    "ON DELETE CASCADE ON UPDATE CASCADE");
                System.out.println("成功添加新的级联删除外键约束");
                
                result.put("success", true);
                result.put("message", "外键约束修复成功，已设置为级联删除");
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                System.err.println("添加新外键约束失败: " + e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "修复外键约束失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    // 添加一个测试接口，方便确认控制器是否正常工作
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("收到测试请求 - /api/admin/maintenance/test");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "系统维护控制器正常工作");
        response.put("timestamp", System.currentTimeMillis());
        System.out.println("返回测试响应: " + response);
        return ResponseEntity.ok(response);
    }
} 