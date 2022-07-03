package com.bakkcover.library.book.entities;

import javax.persistence.*;

@Entity
@IdClass(BookId.class)
public class Book {
    // attributes of composite primary-key
    @Id
    private String title;
    @Id
    private String author;
    @Id
    private String publisher;


}
