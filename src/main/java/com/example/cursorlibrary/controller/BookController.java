package com.example.cursorlibrary.controller;

import com.example.cursorlibrary.dto.BookDTO;
import com.example.cursorlibrary.entity.Book;
import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.service.BookService;
import com.example.cursorlibrary.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;
    private final BorrowService borrowService;
    private final BorrowRecordRepository borrowRecordRepository;

    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.findBooks(keyword, page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@RequestBody BookDTO bookDTO) {
        System.out.println("接收到添加图书请求: " + bookDTO.getTitle());
        Book book = bookService.addBook(bookDTO);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable Long id) {
        Book book = bookService.findById(id);
        return ResponseEntity.ok(BookDTO.fromBook(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        Book book = bookService.updateBook(id, bookDTO);
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        System.out.println("接收到删除图书请求，ID: " + id);
        
        try {
            bookService.deleteBook(id);
            System.out.println("图书删除成功，ID: " + id);
            return ResponseEntity.ok().body(
                java.util.Map.of("message", "图书删除成功", "id", id)
            );
        } catch (Exception e) {
            System.err.println("删除图书失败，ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            
            // 提取更友好的错误信息
            String errorMessage = e.getMessage();
            if (errorMessage.contains("foreign key constraint")) {
                errorMessage = "该图书有关联的借阅记录，无法删除。请先使用系统维护功能修复外键约束，然后再次尝试删除。";
            } else if (errorMessage.contains("未完成的借阅记录")) {
                // 已经有明确的错误消息，保持不变
            } else {
                errorMessage = "删除图书失败: " + errorMessage;
            }
            
            return ResponseEntity.badRequest().body(
                java.util.Map.of("message", errorMessage)
            );
        }
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<?> borrowBook(@PathVariable Long id, Authentication authentication) {
        BorrowRecord record = borrowService.borrowBook(authentication.getName(), id);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/{id}/borrow-records")
    public ResponseEntity<?> getBookBorrowRecords(@PathVariable Long id) {
        Book book = bookService.findById(id);
        List<BorrowRecord> records = borrowRecordRepository.findByBookId(id);
        return ResponseEntity.ok(records);
    }
} 