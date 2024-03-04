package com.gfg.librarymanagement.repo;

import com.gfg.librarymanagement.entity.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository  extends CrudRepository<Book, Long> {
    List<Book> findAllByBname(String name);
}