package com.phegondev.usersmanagementsystem.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long bookId;
    private String orderedBy;
    private LocalDateTime orderDate;
    private BookDTO book;
}
