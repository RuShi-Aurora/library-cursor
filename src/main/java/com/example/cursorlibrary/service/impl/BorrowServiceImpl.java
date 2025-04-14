package com.example.cursorlibrary.service.impl;

import com.example.cursorlibrary.entity.Book;
import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.entity.User;
import com.example.cursorlibrary.repository.BookRepository;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.repository.UserRepository;
import com.example.cursorlibrary.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BorrowRecord borrowBook(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        if (book.getStock() <= 0) {
            throw new RuntimeException("图书库存不足");
        }

        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        
        LocalDateTime now = LocalDateTime.now();
        borrowRecord.setBorrowDate(now);
        borrowRecord.setDueDate(now.plusDays(14));
        borrowRecord.setStatus("PENDING");

        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        return borrowRecordRepository.save(borrowRecord);
    }

    @Override
    public Page<BorrowRecord> getBorrows(String username, String status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getRole().equals("ADMIN")) {
            return status != null ? 
                borrowRecordRepository.findByStatus(status, pageable) :
                borrowRecordRepository.findAll(pageable);
        } else {
            return status != null ?
                borrowRecordRepository.findByUserAndStatus(user, status, pageable) :
                borrowRecordRepository.findByUser(user, pageable);
        }
    }

    @Override
    @Transactional
    public BorrowRecord approveBorrow(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));

        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("借阅状态不正确");
        }

        record.setStatus("BORROWED");
        return borrowRecordRepository.save(record);
    }

    @Override
    @Transactional
    public BorrowRecord rejectBorrow(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));

        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("借阅状态不正确");
        }

        Book book = record.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        record.setStatus("REJECTED");
        return borrowRecordRepository.save(record);
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long id, String username) {
        System.out.println("执行returnBook方法 - 记录ID: " + id + ", 用户名: " + username);
        
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        System.out.println("借阅记录信息: " + record);
        System.out.println("记录关联的用户名: " + record.getUser().getUsername());
        System.out.println("当前操作用户名: " + username);

        if (!"BORROWED".equals(record.getStatus())) {
            System.out.println("借阅状态不正确，当前状态: " + record.getStatus());
            throw new RuntimeException("借阅状态不正确，当前状态: " + record.getStatus());
        }

        // 严格检查：普通用户只能归还自己的图书
        if (!record.getUser().getUsername().equals(username)) {
            System.out.println("权限检查失败 - 用户无权操作此借阅记录");
            throw new RuntimeException("无权操作此借阅记录");
        }

        Book book = record.getBook();
        if (book == null) {
            System.out.println("借阅记录关联的图书不存在");
            throw new RuntimeException("借阅记录关联的图书不存在");
        }
        
        System.out.println("图书信息: " + book);
        System.out.println("当前库存: " + book.getStock() + ", 归还后将增加到: " + (book.getStock() + 1));
        
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        record.setReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        
        System.out.println("归还成功，更新后的借阅记录: " + record);
        
        return borrowRecordRepository.save(record);
    }

    @Override
    public Page<BorrowRecord> getUserBorrowRecords(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return borrowRecordRepository.findByUser(user, pageable);
    }

    @Override
    public Page<BorrowRecord> getAllBorrows(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return borrowRecordRepository.findByStatus(status, pageable);
        }
        return borrowRecordRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public BorrowRecord adminReturnBook(Long id) {
        System.out.println("管理员专用方法：执行归还操作，借阅记录ID: " + id);
        
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        System.out.println("找到借阅记录: " + record);
        System.out.println("记录关联的用户名: " + record.getUser().getUsername());

        if (!"BORROWED".equals(record.getStatus())) {
            System.out.println("借阅状态不正确，当前状态: " + record.getStatus());
            throw new RuntimeException("借阅状态不正确，当前状态: " + record.getStatus() + "，只能归还处于借阅中状态的图书");
        }

        Book book = record.getBook();
        if (book == null) {
            System.out.println("借阅记录关联的图书不存在");
            throw new RuntimeException("借阅记录关联的图书不存在");
        }
        
        System.out.println("图书信息: " + book);
        System.out.println("当前库存: " + book.getStock() + ", 归还后将增加到: " + (book.getStock() + 1));
        
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        record.setReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        
        System.out.println("管理员归还成功，更新后的借阅记录: " + record);
        
        return borrowRecordRepository.save(record);
    }
} 