package com.phegondev.usersmanagementsystem.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    private Long id;
    private String name;
    private String author;
    private String picture;
    private String isbn;
    private Boolean taken;

    // Constructors, Getters, and Setters
}