package com.bakkcover.library.book.entities;

import java.io.Serializable;

public class BookId implements Serializable {
    private String title;
    private String author;
    private String publisher;

    public BookId(String title, String author, String publisher) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
    }
}
