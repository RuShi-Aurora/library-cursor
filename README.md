# 图书馆管理系统 (Library Management System)

## 项目概述

这是一个基于Spring Boot和Vue.js的全栈图书馆管理系统，旨在提供完整的图书借阅、用户管理和统计分析功能。系统包含后端API服务和前端用户界面，支持图书管理、用户认证、借阅记录追踪等核心功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.4.4
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **数据库**: MySQL
- **API文档**: SpringDoc OpenAPI (Swagger)
- **构建工具**: Maven

### 前端
- **框架**: Vue.js
- **路由**: Vue Router
- **状态管理**: Vuex/Pinia
- **构建工具**: Vite
- **UI库**: 未明确，可能基于Bootstrap或自定义CSS

## 核心功能

### 用户管理
- 用户注册和登录
- 基于角色的权限控制(普通用户/管理员)
- 用户信息管理
- 用户状态管理

### 图书管理
- 图书信息的增删改查
- 图书库存管理
- 图书分类管理
- 图书详情展示

### 借阅管理
- 图书借阅和归还
- 借阅状态跟踪(待处理/已借阅/已归还/已拒绝)
- 到期日期管理
- 借阅历史记录

### 数据统计与分析
- 借阅统计
- 用户活跃度分析
- 图书流通统计

### 系统维护
- 数据库维护
- 系统状态监控

## 项目结构

### 后端结构
```
src/main/java/com/example/cursorlibrary/
├── config/                     # 配置类目录
│   ├── SecurityConfig.java     # Spring Security配置
│   ├── JwtConfig.java          # JWT认证配置
│   └── SwaggerConfig.java      # API文档配置
├── controller/                 # 控制器目录
│   ├── AuthController.java     # 认证相关API
│   ├── BookController.java     # 图书管理API
│   ├── BorrowController.java   # 借阅管理API
│   ├── UserController.java     # 用户管理API
│   ├── UserProfileController.java # 用户个人信息API
│   ├── StatisticsController.java # 统计分析API
│   ├── StatsController.java    # 简单统计API
│   └── DbMaintenanceController.java # 数据库维护API
├── dto/                        # 数据传输对象目录
│   ├── request/                # 请求DTO
│   └── response/               # 响应DTO
├── entity/                     # 实体类目录
│   ├── Book.java               # 图书实体
│   ├── User.java               # 用户实体
│   └── BorrowRecord.java       # 借阅记录实体
├── exception/                  # 异常处理目录
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   └── CustomExceptions.java   # 自定义异常类
├── repository/                 # 数据访问层目录
│   ├── BookRepository.java     # 图书数据访问接口
│   ├── UserRepository.java     # 用户数据访问接口
│   └── BorrowRecordRepository.java # 借阅记录数据访问接口
├── service/                    # 服务层目录
│   ├── AuthService.java        # 认证服务
│   ├── BookService.java        # 图书服务
│   ├── BorrowService.java      # 借阅服务
│   ├── UserService.java        # 用户服务
│   └── StatisticsService.java  # 统计服务
├── util/                       # 工具类目录
│   ├── JwtUtil.java            # JWT工具类
│   └── DateUtil.java           # 日期处理工具类
├── validator/                  # 验证器目录
│   └── CustomValidators.java   # 自定义验证规则
└── CursorLibraryApplication.java # 应用程序入口
```

### 前端结构
```
library-ui/src/
├── api/                        # API请求目录
│   ├── auth.js                 # 认证相关API
│   ├── book.js                 # 图书相关API
│   ├── borrow.js               # 借阅相关API
│   ├── user.js                 # 用户相关API
│   └── statistics.js           # 统计相关API
├── assets/                     # 静态资源目录
│   ├── images/                 # 图片资源
│   └── styles/                 # 样式资源
├── components/                 # 公共组件目录
│   ├── common/                 # 通用组件
│   ├── book/                   # 图书相关组件
│   ├── user/                   # 用户相关组件
│   └── borrow/                 # 借阅相关组件
├── router/                     # 路由配置目录
│   └── index.js                # 路由配置
├── store/                      # 状态管理目录
│   ├── modules/                # 状态模块
│   └── index.js                # 状态配置
├── views/                      # 页面视图目录
│   ├── admin/                  # 管理员页面
│   ├── books/                  # 图书相关页面
│   ├── borrows/                # 借阅相关页面
│   ├── users/                  # 用户相关页面
│   ├── Login.vue               # 登录页
│   ├── Home.vue                # 首页
│   ├── Profile.vue             # 个人资料页
│   ├── Books.vue               # 图书列表页
│   └── BookDetail.vue          # 图书详情页
├── App.vue                     # 根组件
└── main.js                     # 应用入口文件
```

## 数据模型

### 用户(User)
- 基本信息：用户ID、用户名、密码、邮箱
- 角色权限：用户角色
- 状态信息：账户状态、创建时间、更新时间、上次登录时间

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // 方法省略...
}
```

### 图书(Book)
- 基本信息：图书ID、标题、作者、ISBN、出版社、出版日期
- 分类信息：图书分类
- 库存信息：库存数量、状态
- 描述信息：详细描述
- 时间信息：创建时间、更新时间

```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false, unique = true)
    private String isbn;
    
    @Column(nullable = false)
    private String publisher;
    
    @Column(name = "publish_date", nullable = false)
    private LocalDate publishDate;
    
    @Column(nullable = false)
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer stock;
    
    @Column(nullable = false)
    private String status = "AVAILABLE"; // 默认可借阅
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 方法省略...
}
```

### 借阅记录(BorrowRecord)
- 关联信息：记录ID、用户ID、图书ID
- 时间信息：借阅日期、应归还日期、实际归还日期
- 状态信息：借阅状态(待处理/已借阅/已归还/已拒绝)

```java
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    
    @Column(name = "borrow_date")
    private LocalDateTime borrowDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "return_date")
    private LocalDateTime returnDate;
    
    private String status; // PENDING, BORROWED, RETURNED, REJECTED
    
    // 方法省略...
}
```

## 关键代码示例

### 后端实现

#### JWT认证配置
```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    // 生成令牌
    public String generateToken(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + expiration);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }
    
    // 验证令牌
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 从令牌中获取用户名
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
}
```

#### 图书借阅服务
```java
@Service
public class BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public BorrowService(BorrowRecordRepository borrowRecordRepository, 
                         BookRepository bookRepository, 
                         UserRepository userRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }
    
    // 借书申请
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId, int days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        
        // 检查图书是否可借
        if (book.getStock() <= 0 || !"AVAILABLE".equals(book.getStatus())) {
            throw new IllegalStateException("图书不可借阅");
        }
        
        // 检查用户借阅权限
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalStateException("用户账户不可用");
        }
        
        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(days));
        record.setStatus("BORROWED");
        
        // 更新图书库存
        book.setStock(book.getStock() - 1);
        if (book.getStock() == 0) {
            book.setStatus("UNAVAILABLE");
        }
        bookRepository.save(book);
        
        return borrowRecordRepository.save(record);
    }
    
    // 归还图书
    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        
        if (!"BORROWED".equals(record.getStatus())) {
            throw new IllegalStateException("当前状态不允许归还");
        }
        
        // 更新借阅记录
        record.setReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        
        // 更新图书库存
        Book book = record.getBook();
        book.setStock(book.getStock() + 1);
        if ("UNAVAILABLE".equals(book.getStatus()) && book.getStock() > 0) {
            book.setStatus("AVAILABLE");
        }
        bookRepository.save(book);
        
        return borrowRecordRepository.save(record);
    }
}
```

### 前端实现

#### 登录组件
```vue
<template>
  <div class="login-container">
    <div class="login-form">
      <h2>图书馆管理系统登录</h2>
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            id="username" 
            v-model="username" 
            type="text" 
            required 
            placeholder="请输入用户名" 
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            id="password" 
            v-model="password" 
            type="password" 
            required 
            placeholder="请输入密码" 
          />
        </div>
        
        <div class="form-actions">
          <button type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'

export default {
  setup() {
    const username = ref('')
    const password = ref('')
    const loading = ref(false)
    const error = ref('')
    const router = useRouter()
    
    const handleLogin = async () => {
      loading.value = true
      error.value = ''
      
      try {
        const response = await login(username.value, password.value)
        localStorage.setItem('token', response.token)
        localStorage.setItem('user', JSON.stringify(response.user))
        router.push('/')
      } catch (err) {
        error.value = err.response?.data?.message || '登录失败，请检查用户名和密码'
      } finally {
        loading.value = false
      }
    }
    
    return {
      username,
      password,
      loading,
      error,
      login: handleLogin
    }
  }
}
</script>
```

#### 图书列表组件
```vue
<template>
  <div class="books-container">
    <div class="books-header">
      <h1>图书列表</h1>
      <div class="search-bar">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索图书..." 
          @input="handleSearch"
        />
      </div>
    </div>
    
    <div class="filter-container">
      <select v-model="categoryFilter" @change="applyFilters">
        <option value="">所有分类</option>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
      
      <select v-model="statusFilter" @change="applyFilters">
        <option value="">所有状态</option>
        <option value="AVAILABLE">可借阅</option>
        <option value="UNAVAILABLE">不可借阅</option>
      </select>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="books.length === 0" class="no-results">
      没有找到符合条件的图书
    </div>
    
    <div v-else class="books-grid">
      <div 
        v-for="book in books" 
        :key="book.id" 
        class="book-card"
        @click="viewBookDetail(book.id)"
      >
        <h3>{{ book.title }}</h3>
        <p class="author">作者: {{ book.author }}</p>
        <p class="category">分类: {{ book.category }}</p>
        <p class="stock">库存: {{ book.stock }}</p>
        <div class="status" :class="book.status.toLowerCase()">
          {{ book.status === 'AVAILABLE' ? '可借阅' : '不可借阅' }}
        </div>
      </div>
    </div>
    
    <div class="pagination">
      <!-- 分页控件 -->
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getBooks, getBookCategories } from '@/api/book'

export default {
  setup() {
    const router = useRouter()
    const books = ref([])
    const loading = ref(true)
    const categories = ref([])
    const searchQuery = ref('')
    const categoryFilter = ref('')
    const statusFilter = ref('')
    
    const fetchBooks = async () => {
      loading.value = true
      try {
        const response = await getBooks({
          search: searchQuery.value,
          category: categoryFilter.value,
          status: statusFilter.value
        })
        books.value = response.data
      } catch (error) {
        console.error('获取图书列表失败', error)
      } finally {
        loading.value = false
      }
    }
    
    const fetchCategories = async () => {
      try {
        const response = await getBookCategories()
        categories.value = response.data
      } catch (error) {
        console.error('获取分类失败', error)
      }
    }
    
    onMounted(() => {
      fetchBooks()
      fetchCategories()
    })
    
    const handleSearch = () => {
      // 实现搜索延迟
      clearTimeout(window.searchTimeout)
      window.searchTimeout = setTimeout(() => {
        fetchBooks()
      }, 500)
    }
    
    const applyFilters = () => {
      fetchBooks()
    }
    
    const viewBookDetail = (bookId) => {
      router.push(`/books/${bookId}`)
    }
    
    return {
      books,
      loading,
      categories,
      searchQuery,
      categoryFilter,
      statusFilter,
      handleSearch,
      applyFilters,
      viewBookDetail
    }
  }
}
</script>
```

## API接口

系统提供了一系列RESTful API，包括：

### 认证接口
```
POST /api/auth/login         # 用户登录
POST /api/auth/register      # 用户注册
POST /api/auth/logout        # 用户注销
GET  /api/auth/me            # 获取当前用户信息
```

### 用户接口
```
GET    /api/users                  # 获取用户列表(管理员)
GET    /api/users/{id}             # 获取指定用户
POST   /api/users                  # 创建用户(管理员)
PUT    /api/users/{id}             # 更新用户
DELETE /api/users/{id}             # 删除用户(管理员)
GET    /api/users/profile          # 获取个人资料
PUT    /api/users/profile          # 更新个人资料
PUT    /api/users/change-password  # 修改密码
```

### 图书接口
```
GET    /api/books                  # 获取图书列表
GET    /api/books/{id}             # 获取指定图书
POST   /api/books                  # 添加图书(管理员)
PUT    /api/books/{id}             # 更新图书(管理员)
DELETE /api/books/{id}             # 删除图书(管理员)
GET    /api/books/categories       # 获取图书分类
```

### 借阅接口
```
GET    /api/borrows                     # 获取借阅记录
GET    /api/borrows/{id}                # 获取指定借阅记录
POST   /api/borrows                     # 创建借阅请求
PUT    /api/borrows/{id}/return         # 归还图书
PUT    /api/borrows/{id}/approve        # 批准借阅(管理员)
PUT    /api/borrows/{id}/reject         # 拒绝借阅(管理员)
GET    /api/borrows/user/{userId}       # 获取用户借阅记录
GET    /api/borrows/book/{bookId}       # 获取图书借阅记录
```

### 统计接口
```
GET    /api/stats/borrows               # 借阅统计
GET    /api/stats/users                 # 用户统计
GET    /api/stats/books                 # 图书统计
GET    /api/stats/summary               # 系统摘要信息
```

## 安全实现

系统使用Spring Security和JWT实现认证与授权：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, 
                         UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/books").permitAll()
                        .requestMatchers("/api/books/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/profile").authenticated()
                        .requestMatchers("/api/borrows/approve/**").hasRole("ADMIN")
                        .requestMatchers("/api/borrows/reject/**").hasRole("ADMIN")
                        .requestMatchers("/api/borrows/**").authenticated()
                        .requestMatchers("/api/stats/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 部署要求

### 系统要求
- JDK 17+
- MySQL 8.0+
- Node.js 16+

### 配置说明
应用的主要配置在`application.properties`文件中，包括：
- 数据库连接配置
- JWT安全配置
- 日志配置
- 连接池配置
- 数据初始化配置

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/library?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=p031011
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT配置
jwt.secret=SsGX3iYykKUxwp+jMgcuu0YzFOoampjZEgsp0HiIFiw=
jwt.expiration=86400000

# 日志配置
logging.level.root=INFO
logging.level.com.example.cursorlibrary=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.file.name=logs/library.log
```

## 快速开始

### 后端启动
1. 确保MySQL数据库已安装并运行
2. 配置`application.properties`中的数据库连接信息
3. 运行以下命令启动Spring Boot应用：
```bash
./mvnw spring-boot:run
```

### 前端启动
1. 进入前端项目目录：
```bash
cd library-ui
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

## 贡献指南

欢迎提交问题和改进建议，请遵循以下步骤：
1. Fork该仓库
2. 创建您的特性分支(`git checkout -b feature/amazing-feature`)
3. 提交您的更改(`git commit -m 'Add some amazing feature'`)
4. 推送到分支(`git push origin feature/amazing-feature`)
5. 创建一个Pull Request

## 许可证

[待定] - 请添加适当的许可证信息

## 联系方式

项目维护者: [待定] - 请添加联系信息 