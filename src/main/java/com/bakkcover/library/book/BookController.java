package com.bakkcover.library.book;

import com.bakkcover.library.book.dto.AddBookRequest;
import com.bakkcover.library.book.dto.GetBooksResponse;
import com.bakkcover.library.book.entities.Book;
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
@CrossOrigin(origins = "http://localhost:4200")
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

    @PostMapping("/add")
    public ResponseEntity<String> addBook(
            @RequestBody
            AddBookRequest addBookRequest,
            Authentication authentication) {
        try {
            String title = addBookRequest.getTitle();
            String author = addBookRequest.getAuthor();
            String publisher = addBookRequest.getPublisher();
            String details = addBookRequest.getDetails();
            User user = this.userService.getUser(authentication);

            bookService.addBook(title, author, publisher, details, user);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Book successfully added!");
    }
}
