package com.bakkcover.library.book.exceptions;

public class BookAlreadyAdoptedException extends AbstractBookException {
    private static final String MESSAGE = "Book already adopted!";

    public BookAlreadyAdoptedException() {
        super(MESSAGE);
    }
}
