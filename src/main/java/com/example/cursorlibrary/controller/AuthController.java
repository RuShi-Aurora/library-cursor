package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.dto.ApiResponse;
import com.example.cursorlibrary.dto.AuthResponseDTO;
import com.example.cursorlibrary.dto.LoginDTO;
import com.example.cursorlibrary.dto.RegisterRequest;
import com.example.cursorlibrary.dto.UserDTO;
import com.example.cursorlibrary.entity.User;
import com.example.cursorlibrary.exception.UserAlreadyExistsException;
import com.example.cursorlibrary.service.UserService;
import com.example.cursorlibrary.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            // 记录请求
            System.out.println("接收到登录请求: " + loginDTO.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(loginDTO.getUsername());
            
            // 更新最后登录时间
            userService.updateLastLogin(loginDTO.getUsername());
            
            // 返回token
            AuthResponseDTO response = new AuthResponseDTO(jwt);
            System.out.println("登录成功，返回token: " + jwt);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            System.out.println("登录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            System.out.println("接收到注册请求: " + request.getUsername());
            userService.register(request);
            return ResponseEntity.ok(new ApiResponse(true, "注册成功"));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "未登录"));
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }
} 