package com.gfg.librarymanagement.dto;


import jakarta.persistence.Entity;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    private String bName;
    private String author;
    private Integer availableQty;
}