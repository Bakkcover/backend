package com.bakkcover.library.book.dto;

import com.bakkcover.library.book.entities.Book;

import java.util.List;

public class GetBooksResponse {
     private List<Book> list;

     public void setList(List<Book> list) {
        this.list = list;
     }

     public List<Book> getList() {
        return this.list;
     }
}
