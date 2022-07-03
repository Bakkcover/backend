package com.bakkcover.library.book.daos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bakkcover.library.book.entities.Book;
import com.bakkcover.library.book.entities.BookId;

import java.util.List;

public interface BookDao extends JpaRepository<Book, BookId> {
    List<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    List<Book> findByPublisher(String publisher);
}
