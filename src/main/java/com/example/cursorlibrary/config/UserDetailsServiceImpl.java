package com.example.cursorlibrary.config;

import com.example.cursorlibrary.entity.User;
import com.example.cursorlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

            System.out.println("找到用户: " + user.getUsername() + ", 角色: " + user.getRole());
            System.out.println("用户ID: " + user.getId() + ", 状态: " + user.getStatus());
            
            // 确保角色名称正确(加上ROLE_前缀)
            String roleWithPrefix = "ROLE_" + user.getRole();
            System.out.println("授予用户的完整角色: " + roleWithPrefix);
            
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleWithPrefix);
            System.out.println("创建权限对象: " + authority);
            
            var userDetails = new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    Collections.singletonList(authority)
            );
            
            System.out.println("用户详情已创建，权限列表: " + userDetails.getAuthorities());
            
            return userDetails;
        } catch (Exception e) {
            System.out.println("加载用户时出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 