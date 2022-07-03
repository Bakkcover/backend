package com.bakkcover.library.book.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    private String author;

    private String publisher;

    @Column(columnDefinition="TEXT")
    private String details;

    // this attribute refers to the attribute "sub" that uniquely identifies a user in a Cognito User-Pool
    private String listedByUser;

    public Book() { }

    public Book(String title, String author, String publisher, String details, String listedByUser) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.details = details;
        this.listedByUser = listedByUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getListedByUser() {
        return listedByUser;
    }

    public void setListedByUser(String listedByUser) {
        this.listedByUser = listedByUser;
    }

    @Override
    public String toString() {
        return String.format("<%s | %s | %s | %s>", this.title, this.author, this.publisher, this.details);
    }
}
