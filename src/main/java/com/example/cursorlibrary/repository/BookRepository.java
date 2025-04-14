package com.example.cursorlibrary.repository;

import com.example.cursorlibrary.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingOrAuthorContaining(String title, String author, Pageable pageable);
    boolean existsByIsbn(String isbn);
} 