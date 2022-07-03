package com.bakkcover.library.book.services.bookservice;

import com.bakkcover.library.book.repositories.BookRepository;
import com.bakkcover.library.book.entities.Book;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> getBookByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    @Override
    public List<Book> getBookByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    @Override
    public List<Book> getBookByPublisher(String publisher) {
        return bookRepository.findByPublisher(publisher);
    }

    @Override
    public void addBook(String title, String author, String publisher, String details, String listedByUser) {
        bookRepository.save(new Book(title, author, publisher, details, listedByUser));
    }
}
