package com.bakkcover.library.book.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bakkcover.library.book.entities.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    List<Book> findByPublisher(String publisher);
}
