package com.bakkcover.library.book.dto;

import com.bakkcover.library.book.entities.Book;

public class GetBookResponse {
    private Book book;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
