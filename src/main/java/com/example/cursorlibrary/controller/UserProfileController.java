package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.dto.PasswordUpdateDTO;
import com.example.cursorlibrary.dto.UserDTO;
import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.entity.User;
import com.example.cursorlibrary.service.BorrowService;
import com.example.cursorlibrary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final UserService userService;
    private final BorrowService borrowService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody UserDTO userDTO,
            Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        User updatedUser = userService.updateUser(user.getId(), userDTO);
        return ResponseEntity.ok(UserDTO.fromUser(updatedUser));
    }
    
    @PutMapping("/profile/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateDTO passwordDTO,
            Authentication authentication) {
        userService.updatePassword(
                authentication.getName(), 
                passwordDTO.getCurrentPassword(), 
                passwordDTO.getNewPassword()
        );
        return ResponseEntity.ok(Map.of("message", "密码更新成功"));
    }
    
    @GetMapping("/borrow-records")
    public ResponseEntity<?> getBorrowRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Page<BorrowRecord> records = borrowService.getUserBorrowRecords(
                authentication.getName(),
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(records);
    }
} 