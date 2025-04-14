package com.example.cursorlibrary.service.impl;

import com.example.cursorlibrary.dto.RegisterRequest;
import com.example.cursorlibrary.dto.UserDTO;
import com.example.cursorlibrary.entity.User;
import com.example.cursorlibrary.exception.UserAlreadyExistsException;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.repository.UserRepository;
import com.example.cursorlibrary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("邮箱已被使用");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        // 使用请求中的角色，如果为空则默认为"USER"
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public Page<User> findUsers(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否尝试删除admin用户
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("不能删除管理员账户");
        }

        System.out.println("正在删除用户: " + user.getUsername() + " (ID: " + id + ")");
        
        // 检查用户是否有借阅记录
        long borrowCount = borrowRecordRepository.countByUserId(id);
        if (borrowCount > 0) {
            System.out.println("用户有 " + borrowCount + " 条借阅记录");
            
            // 检查是否有未完成的借阅（待审核或已借出状态）
            long activeCount = borrowRecordRepository.countByUserIdAndStatusIn(id, Arrays.asList("PENDING", "BORROWED"));
            if (activeCount > 0) {
                System.out.println("用户有 " + activeCount + " 条未完成的借阅记录");
                throw new RuntimeException("该用户有未完成的借阅记录（" + activeCount + " 条待审核或已借出），请先处理这些记录");
            }
            
            // 尝试直接使用原生SQL删除所有相关借阅记录
            try {
                System.out.println("正在删除用户的所有借阅记录...");
                // 使用JPQL直接删除借阅记录，绕过外键约束
                int deletedRecords = borrowRecordRepository.deleteByUserIdAndStatusIn(id, Arrays.asList("RETURNED", "REJECTED", "PENDING", "BORROWED"));
                System.out.println("成功删除 " + deletedRecords + " 条借阅记录");
            } catch (Exception e) {
                System.err.println("删除借阅记录失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("无法删除用户的借阅记录: " + e.getMessage());
            }
        }
        
        try {
            // 删除用户
            System.out.println("正在删除用户...");
            userRepository.deleteById(id);
            System.out.println("用户删除成功");
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除用户时出错: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateLastLogin(String username) {
        User user = findByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public void updatePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
} 