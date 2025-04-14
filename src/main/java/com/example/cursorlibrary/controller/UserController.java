package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.dto.RegisterRequest;
import com.example.cursorlibrary.dto.UserDTO;
import com.example.cursorlibrary.entity.User;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final BorrowRecordRepository borrowRecordRepository;

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("获取用户列表请求 - 页码: " + page + ", 大小: " + size + ", 关键词: " + keyword);
        
        Page<User> userPage = userService.findUsers(keyword, PageRequest.of(page, size));
        System.out.println("查询到用户数量: " + userPage.getTotalElements());
        
        // 转换为DTO并添加借阅记录数量
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(user -> {
                    UserDTO dto = UserDTO.fromUser(user);
                    try {
                        // 获取用户的借阅记录数量 - 直接使用SQL查询确保准确
                        long borrowCount = borrowRecordRepository.countByUserId(user.getId());
                        System.out.println("用户 " + user.getUsername() + " (ID: " + user.getId() + ") 的借阅记录数量: " + borrowCount);
                        dto.setBorrowCount(borrowCount);
                    } catch (Exception e) {
                        System.err.println("获取用户 " + user.getUsername() + " 的借阅记录数量时出错: " + e.getMessage());
                        dto.setBorrowCount(0L); // 出错时默认为0
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        
        Page<UserDTO> dtoPage = new PageImpl<>(
                userDTOs, 
                userPage.getPageable(), 
                userPage.getTotalElements()
        );
        
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody RegisterRequest registerRequest) {
        // 验证角色是否有效
        if (registerRequest.getRole() != null && 
            !Arrays.asList("USER", "ADMIN").contains(registerRequest.getRole())) {
            throw new RuntimeException("无效的角色");
        }
        User user = userService.register(registerRequest);
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        // 验证角色是否有效
        if (userDTO.getRole() != null && 
            !Arrays.asList("USER", "ADMIN").contains(userDTO.getRole())) {
            throw new RuntimeException("无效的角色");
        }
        User user = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        System.out.println("接收删除用户请求 - ID: " + id);
        
        try {
            userService.deleteUser(id);
            System.out.println("用户删除成功 - ID: " + id);
            return ResponseEntity.ok().body(Map.of("message", "用户删除成功"));
        } catch (Exception e) {
            System.err.println("删除用户失败 - ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            
            // 提取更友好的错误信息
            String errorMessage = e.getMessage();
            if (errorMessage.contains("foreign key constraint fails")) {
                errorMessage = "该用户有关联的借阅记录，无法删除。请先处理这些记录。";
            }
            
            return ResponseEntity.badRequest().body(
                Map.of("message", errorMessage)
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }

    // 简单的错误响应类
    @Data
    @AllArgsConstructor
    class ErrorResponse {
        private String message;
    }
} 