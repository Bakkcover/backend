package com.bakkcover.library.book.entities;

import com.bakkcover.user.entities.User;

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

    @ManyToOne
    private User listedByUser;

    @ManyToOne
    private User adoptedByUser;

    public Book() { }

    public Book(String title, String author, String publisher, String details, User listedByUser) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.details = details;
        this.listedByUser = listedByUser;
    }

    public Long getId() {
        return id;
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

    public User getListedByUser() {
        return listedByUser;
    }

    public void setListedByUser(User listedByUser) {
        this.listedByUser = listedByUser;
    }

    public User getAdoptedByUser() {
        return adoptedByUser;
    }

    public void setAdoptedByUser(User adoptedByUser) {
        this.adoptedByUser = adoptedByUser;
    }

    @Override
    public String toString() {
        return String.format("<%s | %s | %s | %s>", this.title, this.author, this.publisher, this.details);
    }
}
