package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.repository.BookRepository;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据控制器
 * 提供图书、借阅、用户等统计数据
 */
@RestController
@RequestMapping({"/api/statistics", "/api/stats"})
@RequiredArgsConstructor
public class StatisticsController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    /**
     * 获取统计数据
     * 支持多种路径: /api/statistics, /api/statistics/, /api/stats, /api/stats/
     */
    @GetMapping({"", "/"})
    public ResponseEntity<?> getStatistics() {
        try {
            System.out.println("收到获取统计数据请求");
            Map<String, Object> statistics = new HashMap<>();
            
            // 图书总数
            long totalBooks = bookRepository.count();
            statistics.put("totalBooks", totalBooks);
            System.out.println("图书总数: " + totalBooks);
            
            // 借阅总数
            long totalBorrows = borrowRecordRepository.count();
            statistics.put("totalBorrows", totalBorrows);
            System.out.println("借阅总数: " + totalBorrows);
            
            // 用户总数
            long totalUsers = userRepository.count();
            statistics.put("totalUsers", totalUsers);
            System.out.println("用户总数: " + totalUsers);
            
            // 待处理的借阅请求
            try {
                // 尝试通过分页查询获取
                Page<BorrowRecord> pendingPage = borrowRecordRepository.findByStatus("PENDING", PageRequest.of(0, 1));
                long pendingRequests = pendingPage.getTotalElements();
                statistics.put("pendingRequests", pendingRequests);
                System.out.println("待处理请求数: " + pendingRequests);
            } catch (Exception e) {
                System.err.println("获取待处理请求数量失败: " + e.getMessage());
                statistics.put("pendingRequests", 0);
            }
            
            System.out.println("统计数据获取成功: " + statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("获取统计数据失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalBooks", 0);
            defaultStats.put("totalBorrows", 0);
            defaultStats.put("totalUsers", 0);
            defaultStats.put("pendingRequests", 0);
            return ResponseEntity.ok(defaultStats);
        }
    }
    
    /**
     * 测试端点，用于检查API可访问性
     */
    @GetMapping("/test")
    public ResponseEntity<?> testStatistics() {
        System.out.println("测试统计API可访问性");
        return ResponseEntity.ok(Map.of(
            "status", "ok", 
            "message", "统计API可访问", 
            "timestamp", System.currentTimeMillis()
        ));
    }
} 