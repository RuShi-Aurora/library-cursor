package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.dto.BorrowDTO;
import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {
    
    private final BorrowService borrowService;

    @PostMapping
    public ResponseEntity<?> borrowBook(@RequestBody BorrowDTO borrowDTO, Authentication authentication) {
        return ResponseEntity.ok(borrowService.borrowBook(authentication.getName(), borrowDTO.getBookId()));
    }
    
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminBorrowBook(@RequestBody BorrowDTO borrowDTO) {
        return ResponseEntity.ok(borrowService.borrowBook(borrowDTO.getUsername(), borrowDTO.getBookId()));
    }

    @GetMapping
    public ResponseEntity<?> getBorrows(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        System.out.println("获取借阅记录请求 - 用户: " + authentication.getName() + ", 状态: " + status + ", 页码: " + page + ", 大小: " + size);
        
        if (status != null && !status.isEmpty()) {
            System.out.println("状态过滤条件: " + status);
            if (!Arrays.asList("PENDING", "BORROWED", "RETURNED", "REJECTED").contains(status)) {
                System.out.println("警告: 无效的状态值: " + status);
            }
        } else {
            System.out.println("未提供状态过滤条件，将返回所有状态的记录");
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        System.out.println("用户角色是否为管理员: " + isAdmin);
        
        Page<BorrowRecord> result;
        try {
            if (isAdmin) {
                result = borrowService.getAllBorrows(status, PageRequest.of(page, size));
                System.out.println("管理员获取所有借阅记录, 总数: " + result.getTotalElements() + ", 当前页数据量: " + result.getContent().size());
            } else {
                result = borrowService.getBorrows(authentication.getName(), status, PageRequest.of(page, size));
                System.out.println("普通用户获取个人借阅记录, 总数: " + result.getTotalElements() + ", 当前页数据量: " + result.getContent().size());
            }
            
            if (!result.getContent().isEmpty()) {
                System.out.println("首条记录示例: " + result.getContent().get(0));
                System.out.println("记录状态: " + result.getContent().get(0).getStatus());
            } else {
                System.out.println("没有找到符合条件的借阅记录");
                if (status != null && !status.isEmpty()) {
                    System.out.println("可能原因: 没有状态为 " + status + " 的记录");
                }
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("获取借阅记录时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取借阅记录失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveBorrow(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.approveBorrow(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectBorrow(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.rejectBorrow(id));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id, Authentication authentication) {
        System.out.println("接收图书归还请求 - ID: " + id + ", 用户: " + authentication.getName());
        
        System.out.println("用户详情: " + authentication.getPrincipal());
        System.out.println("用户权限: " + authentication.getAuthorities());
        
        try {
            String username = authentication.getName();
            System.out.println("提交归还操作，用户: " + username);
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            BorrowRecord result;
            if (isAdmin) {
                System.out.println("使用管理员权限归还图书");
                result = borrowService.adminReturnBook(id);
            } else {
                System.out.println("使用普通用户权限归还图书");
                result = borrowService.returnBook(id, username);
            }
            
            System.out.println("归还成功，记录更新为: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("归还图书时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/admin/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminReturnBook(@PathVariable Long id) {
        System.out.println("管理员专用API：接收图书归还请求 - ID: " + id);
        System.out.println("请求路径: /api/borrows/admin/" + id + "/return");
        
        try {
            BorrowRecord result = borrowService.adminReturnBook(id);
            System.out.println("管理员归还成功，记录更新为: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("管理员归还图书时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
} 