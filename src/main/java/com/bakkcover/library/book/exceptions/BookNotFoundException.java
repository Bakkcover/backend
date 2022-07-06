package com.bakkcover.library.book.exceptions;

public class BookNotFoundException extends AbstractBookException {
    private static final String MESSAGE = "Book not found!";

    public BookNotFoundException() {
        super(MESSAGE);
    }
}
