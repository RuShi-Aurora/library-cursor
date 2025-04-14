package com.example.cursorlibrary.service;

import com.example.cursorlibrary.dto.RegisterRequest;
import com.example.cursorlibrary.dto.UserDTO;
import com.example.cursorlibrary.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    // 用户管理相关方法
    Page<User> findUsers(String keyword, Pageable pageable);
    User register(RegisterRequest request);
    User updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    
    // 其他必要的方法
    User findByUsername(String username);
    User findById(Long id);
    void updateLastLogin(String username);
    void updatePassword(String username, String currentPassword, String newPassword);
} 