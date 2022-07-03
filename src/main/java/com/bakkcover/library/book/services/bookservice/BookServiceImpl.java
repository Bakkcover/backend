package com.bakkcover.library.book.services.bookservice;

import com.bakkcover.library.book.daos.BookDao;
import com.bakkcover.library.book.entities.Book;

import java.util.List;

public class BookServiceImpl implements BookService {
    private final BookDao bookDao;

    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookDao.findAll();
    }

    @Override
    public List<Book> getBookByTitle(String title) {
        return bookDao.findByTitle(title);
    }

    @Override
    public List<Book> getBookByAuthor(String author) {
        return bookDao.findByAuthor(author);
    }

    @Override
    public List<Book> getBookByPublisher(String publisher) {
        return bookDao.findByPublisher(publisher);
    }
}
