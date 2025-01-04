package com.phegondev.usersmanagementsystem.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String author;
    private String picture;

    @Column(unique = true)
    private String isbn; // Unique identifier for the book
    private Boolean taken = false; // Indicates if the book is currently ordered
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private OurUsers user;
    // Getters and Setters
    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }
}