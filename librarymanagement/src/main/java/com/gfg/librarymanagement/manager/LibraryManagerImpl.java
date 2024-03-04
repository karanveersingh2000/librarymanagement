package com.gfg.librarymanagement.manager;


import com.gfg.librarymanagement.dto.BookDto;
import com.gfg.librarymanagement.dto.BorrowRequest;
import com.gfg.librarymanagement.dto.FineDto;
import com.gfg.librarymanagement.dto.UserDto;
import com.gfg.librarymanagement.entity.*;
import com.gfg.librarymanagement.repo.BookRepository;
import com.gfg.librarymanagement.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LibraryManagerImpl implements LibraryManager{
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Long daysAllowedForBorrow = 5l;

    @Override
    public BookDto getBook(String name) {
        List<Book> books = bookRepository.findAllByBname(name);
        if (books.isEmpty()){
            return BookDto.builder().build();
        }
        Book book = books.get(0);
        BookDto bookDto = BookDto.builder()
                .author(book.getAuthor())
                .bName(book.getBname())
                .availableQty(book.getInventory().getQty()).build();
        return bookDto;
    }

    @Override
    public void addBook(BookDto bookDto) {
        Inventory inventory  = Inventory.
                builder().
                qty(bookDto.getAvailableQty())
                .build();
        Book book = Book.builder()
                .bname(bookDto.getBName())
                .author(bookDto.getAuthor())
                .inventory(inventory).build();
        inventory.setBook(book);
        bookRepository.save(book);
    }

    @Override
    public void addUser(UserDto userDto) {
        FineAmount fineAmount = FineAmount.builder().fine(0d).build();
        User user = User.builder()
                .email(userDto.getEmail())
                .fineAmount(fineAmount)
                .build();
        fineAmount.setUser(user);
        userRepository.save(user);

    }

    @Override
    public void borrowBook(BorrowRequest borrowRequest) throws Exception {
        List<Book> books = bookRepository.findAllByBname(borrowRequest.getBookName());
        Optional<User> userOpt = userRepository.findByEmail(borrowRequest.getEmail());
        if(books.isEmpty()){
            throw new Exception("No such book is present");
        }
        if(userOpt.isEmpty()){
            throw new Exception("No such user is present");
        }
        User user = userOpt.get();
        if(user.getFineAmount().getFine()>0d){
            throw new Exception("User has fine to pay");
        }
        if(user.getBook() != null){
            throw new Exception("User has already borrowed a book");
        }
        if(user.getBorrowHistory() != null){
            throw new Exception("User has already borrowed a book or didn't pay fine");
        }
        Book book = books.get(0);
        if(book.getInventory().getQty() <1) {
            throw new Exception("Book is not available for borrowing.");
        }
        Integer qty = book.getInventory().getQty();
        book.getInventory().setQty(qty -1);
        if (book.getUsers() == null){
            book.setUsers(Arrays.asList(user));
        }else{
            book.getUsers().add(user);
        }
        user.setBook(book);
        BorrowHistory borrowHistory = BorrowHistory.builder().
                dateTime(Date.valueOf(LocalDate.now().minusDays(10)))
                .user(user)
                .build();
        user.setBorrowHistory(borrowHistory);

        bookRepository.save(book);
        userRepository.save(user);
    }

    @Override
    public void returnBook(BorrowRequest borrowRequest) throws Exception {
        List<Book> books = bookRepository.findAllByBname(borrowRequest.getBookName());
        Optional<User> userOpt = userRepository.findByEmail(borrowRequest.getEmail());
        if(books.isEmpty()){
            throw new Exception("No such book is present");
        }
        if(userOpt.isEmpty()){
            throw new Exception("No such user is present");
        }
        User user = userOpt.get();
        user.setBook(null);
        BorrowHistory borrowHistory = user.getBorrowHistory();
        LocalDate borrowedDate = borrowHistory.getDateTime().toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(borrowedDate, LocalDate.now());
        double fine = 0l;
        if(daysBetween> daysAllowedForBorrow){
            fine = calculateFine(daysBetween-daysAllowedForBorrow);
        }
        if(fine == 0){
            user.setBorrowHistory(null);
            borrowHistory.setUser(null);
        }else {
            user.getFineAmount().setFine(fine);
        }

        Book book = books.get(0);
        book.getUsers().remove(user);
        int qty = book.getInventory().getQty();
        book.getInventory().setQty(qty+1);

        bookRepository.save(book);
        userRepository.save(user);
    }

    private double calculateFine(long l) {
        return l*5;
    }

    @Override
    public FineDto getFine(String email) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isEmpty()){
            throw new Exception("No such user is present");
        }
        User user = userOpt.get();
        return
                FineDto.builder().fine(user.getFineAmount().getFine()).build();

    }

    @Override
    public void payFine(String email, double finePayment) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isEmpty()){
            throw new Exception("No such user is present");
        }
        User user = userOpt.get();
        FineAmount fineAmount = user.getFineAmount();
        fineAmount.setFine(fineAmount.getFine()-finePayment);
        if (fineAmount.getFine() <=0 ){
            BorrowHistory borrowHistory = user.getBorrowHistory();
            user.setBorrowHistory(null);
            borrowHistory.setUser(null);
        }
        userRepository.save(user);
    }
}