package com.bakkcover.library.book.services.bookservice;

import com.bakkcover.library.book.entities.Book;
import com.bakkcover.library.book.exceptions.BookAlreadyAdoptedException;
import com.bakkcover.library.book.exceptions.BookNotFoundException;
import com.bakkcover.user.entities.User;

import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> getAllBooks();

    Book getBookById(Long id) throws BookNotFoundException;

    List<Book> getBookByTitle(String title);

    List<Book> getBookByAuthor(String author);

    List<Book> getBookByPublisher(String publisher);

    void addBook(String title, String author, String publisher, String details, User listedByUser);

    void adoptBook(Long id, User user) throws BookNotFoundException, BookAlreadyAdoptedException;
}
