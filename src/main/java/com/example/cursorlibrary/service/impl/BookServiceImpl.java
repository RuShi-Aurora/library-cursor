package com.example.cursorlibrary.service.impl;

import com.example.cursorlibrary.dto.BookDTO;
import com.example.cursorlibrary.entity.Book;
import com.example.cursorlibrary.entity.BorrowRecord;
import com.example.cursorlibrary.repository.BookRepository;
import com.example.cursorlibrary.repository.BorrowRecordRepository;
import com.example.cursorlibrary.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    public Page<Book> findBooks(String keyword, int page, int size) {
        if (keyword != null && !keyword.isEmpty()) {
            return bookRepository.findByTitleContainingOrAuthorContaining(
                    keyword, keyword, PageRequest.of(page, size));
        }
        return bookRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public Book addBook(BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setIsbn(bookDTO.getIsbn());
        book.setPublisher(bookDTO.getPublisher());
        book.setPublishDate(bookDTO.getPublishDate());
        book.setCategory(bookDTO.getCategory());
        book.setDescription(bookDTO.getDescription());
        book.setStock(bookDTO.getStock());
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        
        return bookRepository.save(book);
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
    }

    @Override
    @Transactional
    public Book updateBook(Long id, BookDTO bookDTO) {
        Book book = findById(id);
        
        if (bookDTO.getTitle() != null) {
            book.setTitle(bookDTO.getTitle());
        }
        if (bookDTO.getAuthor() != null) {
            book.setAuthor(bookDTO.getAuthor());
        }
        if (bookDTO.getIsbn() != null) {
            book.setIsbn(bookDTO.getIsbn());
        }
        if (bookDTO.getPublisher() != null) {
            book.setPublisher(bookDTO.getPublisher());
        }
        if (bookDTO.getPublishDate() != null) {
            book.setPublishDate(bookDTO.getPublishDate());
        }
        if (bookDTO.getCategory() != null) {
            book.setCategory(bookDTO.getCategory());
        }
        if (bookDTO.getDescription() != null) {
            book.setDescription(bookDTO.getDescription());
        }
        if (bookDTO.getStock() != null) {
            book.setStock(bookDTO.getStock());
        }
        
        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        System.out.println("开始删除图书，ID: " + id);
        
        // 检查图书是否存在
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        // 首先获取所有关联的借阅记录（无论状态）
        List<BorrowRecord> allBorrowRecords = borrowRecordRepository.findByBookId(id);
        System.out.println("图书关联的所有借阅记录数量: " + allBorrowRecords.size());
        
        // 检查是否有未完成的借阅记录（待审核或已借出）
        List<BorrowRecord> activeBorrows = allBorrowRecords.stream()
                .filter(br -> "PENDING".equals(br.getStatus()) || "BORROWED".equals(br.getStatus()))
                .collect(Collectors.toList());
        
        if (!activeBorrows.isEmpty()) {
            System.out.println("图书存在" + activeBorrows.size() + "条未完成的借阅记录，无法删除");
            throw new RuntimeException("该图书有" + activeBorrows.size() + "条未完成的借阅记录，请等待归还后再删除");
        }
        
        // 删除所有的借阅记录（已经确认没有未完成的记录）
        try {
            if (!allBorrowRecords.isEmpty()) {
                System.out.println("开始删除" + allBorrowRecords.size() + "条借阅记录");
                
                // 方法1: 使用Repository直接删除
                for (BorrowRecord br : allBorrowRecords) {
                    System.out.println("删除借阅记录 ID: " + br.getId() + ", 状态: " + br.getStatus());
                    borrowRecordRepository.delete(br);
                }
                System.out.println("所有借阅记录已直接删除");
                
                // 方法2: 使用自定义批量删除方法
                List<String> allStatusList = Arrays.asList("RETURNED", "REJECTED", "PENDING", "BORROWED");
                try {
                    int deletedCount = borrowRecordRepository.deleteByBookIdAndStatusIn(id, allStatusList);
                    System.out.println("通过批量删除方法删除了" + deletedCount + "条借阅记录");
                } catch (Exception e) {
                    System.err.println("批量删除记录失败，但已通过方法1处理: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("删除借阅记录失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除借阅记录失败，无法删除图书: " + e.getMessage());
        }
        
        try {
            // 最后删除图书本身
            System.out.println("开始删除图书实体");
            bookRepository.delete(book);
            System.out.println("图书删除成功，ID: " + id);
        } catch (Exception e) {
            System.err.println("删除图书失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除图书失败: " + e.getMessage());
        }
    }
} 