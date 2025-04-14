package com.example.cursorlibrary.config;

import com.example.cursorlibrary.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    
    // 不需要验证的路径
    private final List<String> excludedPaths = Arrays.asList(
        "/api/auth/login", 
        "/api/auth/register",
        "/api/public",
        "/api/stats",
        "/api/statistics",
        "/api/admin/maintenance/test"  // 添加系统维护测试接口
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("请求方法: " + method + ", 请求路径: " + path);
        
        // 检查是否是OPTIONS请求（预检请求）
        if (method.equals("OPTIONS")) {
            System.out.println("这是OPTIONS预检请求，不进行JWT验证");
            return true;
        }
        
        boolean shouldExclude = excludedPaths.stream()
            .anyMatch(excludedPath -> path.startsWith(excludedPath));
            
        // 添加调试信息
        System.out.println("JWT过滤器处理: " + (shouldExclude ? "排除" : "需要验证") + " - " + path);
        
        // 打印请求头信息，帮助调试
        System.out.println("请求头信息:");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("  " + headerName + ": " + request.getHeader(headerName));
        }
        
        return shouldExclude;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization头: " + (authHeader != null ? "存在" : "不存在"));

        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("从JWT提取的用户名: " + username);
            } catch (Exception e) {
                System.out.println("JWT解析失败: " + e.getMessage());
            }
        }

        // 如果找到token且security上下文中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT验证成功，已设置认证信息");
                } else {
                    System.out.println("JWT验证失败");
                }
            } catch (Exception e) {
                System.out.println("用户详情加载失败: " + e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }
} 