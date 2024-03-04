package com.gfg.librarymanagement.manager;

import com.gfg.librarymanagement.dto.BookDto;
import com.gfg.librarymanagement.dto.BorrowRequest;
import com.gfg.librarymanagement.dto.FineDto;
import com.gfg.librarymanagement.dto.UserDto;

public interface LibraryManager {
    BookDto getBook(String name);
    void addBook(BookDto bookDto);
    void addUser(UserDto userDto);
    void borrowBook(BorrowRequest borrowRequest) throws Exception;
    void returnBook(BorrowRequest borrowRequest) throws Exception;
    FineDto getFine(String email) throws Exception;
    void payFine(String email , double fine) throws Exception;
}