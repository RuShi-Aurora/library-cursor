package com.example.cursorlibrary.service;

import com.example.cursorlibrary.dto.BookDTO;
import com.example.cursorlibrary.entity.Book;
import org.springframework.data.domain.Page;

public interface BookService {
    Page<Book> findBooks(String keyword, int page, int size);
    Book addBook(BookDTO bookDTO);
    Book findById(Long id);
    Book updateBook(Long id, BookDTO bookDTO);
    void deleteBook(Long id);
} 