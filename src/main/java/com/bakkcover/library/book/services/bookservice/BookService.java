package com.bakkcover.library.book.services.bookservice;

import com.bakkcover.library.book.entities.Book;
import com.bakkcover.user.entities.User;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks();

    List<Book> getBookByTitle(String title);

    List<Book> getBookByAuthor(String author);

    List<Book> getBookByPublisher(String publisher);

    void addBook(String title, String author, String publisher, String details, User listedByUser);
}
