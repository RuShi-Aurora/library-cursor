package com.example.cursorlibrary.service;

import com.example.cursorlibrary.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BorrowService {
    BorrowRecord borrowBook(String username, Long bookId);
    Page<BorrowRecord> getBorrows(String username, String status, Pageable pageable);
    Page<BorrowRecord> getAllBorrows(String status, Pageable pageable);
    BorrowRecord approveBorrow(Long id);
    BorrowRecord rejectBorrow(Long id);
    BorrowRecord returnBook(Long id, String username);
    BorrowRecord adminReturnBook(Long id);
    Page<BorrowRecord> getUserBorrowRecords(String username, Pageable pageable);
} 