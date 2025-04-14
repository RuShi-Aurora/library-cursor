package com.example.cursorlibrary.repository;

import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    Page<BorrowRecord> findByUser(User user, Pageable pageable);
    Page<BorrowRecord> findByUserAndStatus(User user, String status, Pageable pageable);
    Page<BorrowRecord> findByStatus(String status, Pageable pageable);
    
    // 统计特定状态的借阅记录数量
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status")
    long countByStatus(@Param("status") String status);
    
    // 添加通过图书ID查询借阅记录的方法
    @Query("SELECT br FROM BorrowRecord br WHERE br.book.id = :bookId")
    List<BorrowRecord> findByBookId(@Param("bookId") Long bookId);
    
    // 统计图书相关的借阅记录数量
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.book.id = :bookId")
    long countByBookId(@Param("bookId") Long bookId);
    
    // 统计用户相关的借阅记录数量
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // 统计用户特定状态的借阅记录数量
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user.id = :userId AND br.status IN :statusList")
    long countByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statusList") List<String> statusList);
    
    // 删除用户特定状态的借阅记录
    @Modifying
    @Transactional
    @Query("DELETE FROM BorrowRecord br WHERE br.user.id = :userId AND br.status IN :statusList")
    int deleteByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statusList") List<String> statusList);

    // 添加一个简单的直接计数方法，不需要分页
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status")
    long directCountByStatus(@Param("status") String status);

    // 统计图书特定状态的借阅记录数量
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.book.id = :bookId AND br.status IN :statusList")
    long countByBookIdAndStatusIn(@Param("bookId") Long bookId, @Param("statusList") List<String> statusList);

    // 删除图书的所有已完成借阅记录（已归还或已拒绝）
    @Modifying
    @Transactional
    @Query("DELETE FROM BorrowRecord br WHERE br.book.id = :bookId AND br.status IN :statusList")
    int deleteByBookIdAndStatusIn(@Param("bookId") Long bookId, @Param("statusList") List<String> statusList);
} 