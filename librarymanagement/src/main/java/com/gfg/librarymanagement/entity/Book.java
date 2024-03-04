package com.gfg.librarymanagement.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bname;
    private String author;
    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Inventory inventory;
    @OneToMany
    @JoinTable(
            name = "orders",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
}