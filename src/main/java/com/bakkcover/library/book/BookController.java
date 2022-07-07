package com.bakkcover.library.book;

import com.bakkcover.library.book.dto.*;
import com.bakkcover.library.book.entities.Book;
import com.bakkcover.library.book.exceptions.BookAlreadyAdoptedException;
import com.bakkcover.library.book.exceptions.BookNotFoundException;
import com.bakkcover.library.book.services.bookservice.BookService;
import com.bakkcover.user.entities.User;
import com.bakkcover.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/books")
// @CrossOrigin(origins = {"http://localhost:4200", "http://bakkcover.s3-website-ap-southeast-1.amazonaws.com"})
public class BookController {
    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<GetBooksResponse> getAllBooks() {

        GetBooksResponse getBooksResponse = new GetBooksResponse();

        try {
            List<Book> result = this.bookService.getAllBooks();
            getBooksResponse.setList(result);

            System.out.println(result);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(getBooksResponse);
    }

    @GetMapping("/book")
    public ResponseEntity<GetBookResponse> getBook(@RequestParam Long id) {
        GetBookResponse getBookResponse = new GetBookResponse();

        try {
            Book book = this.bookService.getBookById(id);
            getBookResponse.setBook(book);
        } catch (BookNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(getBookResponse);
    }

    @GetMapping("/adopt")
    public ResponseEntity<AdoptBookResponse> adoptBook(@RequestParam Long id, Authentication authentication) {
        String SUCCESS_MESSAGE = "Successfully adopted book";

        AdoptBookResponse adoptBookResponse = new AdoptBookResponse();

        try {
            User user = this.userService.getUser(authentication);
            this.bookService.adoptBook(id, user);
            adoptBookResponse.setMessage(SUCCESS_MESSAGE);
        } catch (BookNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (BookAlreadyAdoptedException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adoptBookResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<AddBookResponse> addBook(
            @RequestBody
            AddBookRequest addBookRequest,
            Authentication authentication) {
        String SUCCESS_MESSAGE = "Successfully added book";

        AddBookResponse addBookResponse = new AddBookResponse();

        try {
            String title = addBookRequest.getTitle();
            String author = addBookRequest.getAuthor();
            String publisher = addBookRequest.getPublisher();
            String details = addBookRequest.getDetails();
            User user = this.userService.getUser(authentication);

            bookService.addBook(title, author, publisher, details, user);

            addBookResponse.setMessage(SUCCESS_MESSAGE);
        } catch (Exception e) {
            addBookResponse.setMessage(e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(addBookResponse);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(addBookResponse);
    }
}
