package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.repository.BookRepository;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 简化版统计控制器，专门用于处理/api/stats路径
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @GetMapping({"", "/"})
    public ResponseEntity<?> getStats() {
        try {
            System.out.println("【StatsController】收到获取统计数据请求");
            Map<String, Object> statistics = new HashMap<>();
            
            // 图书总数
            long totalBooks = bookRepository.count();
            statistics.put("totalBooks", totalBooks);
            System.out.println("【StatsController】图书总数: " + totalBooks);
            
            // 借阅总数
            long totalBorrows = borrowRecordRepository.count();
            statistics.put("totalBorrows", totalBorrows);
            System.out.println("【StatsController】借阅总数: " + totalBorrows);
            
            // 用户总数
            long totalUsers = userRepository.count();
            statistics.put("totalUsers", totalUsers);
            System.out.println("【StatsController】用户总数: " + totalUsers);
            
            // 待处理的借阅请求
            long pendingRequests = borrowRecordRepository.directCountByStatus("PENDING");
            statistics.put("pendingRequests", pendingRequests);
            System.out.println("【StatsController】待处理请求数: " + pendingRequests);
            
            System.out.println("【StatsController】统计数据获取成功: " + statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("【StatsController】获取统计数据失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalBooks", 0);
            defaultStats.put("totalBorrows", 0);
            defaultStats.put("totalUsers", 0);
            defaultStats.put("pendingRequests", 0);
            return ResponseEntity.ok(defaultStats);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Stats API is working",
            "timestamp", System.currentTimeMillis()
        ));
    }
} 