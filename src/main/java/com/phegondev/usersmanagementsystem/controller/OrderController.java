package com.phegondev.usersmanagementsystem.controller;


import com.phegondev.usersmanagementsystem.dto.BookDTO;
import com.phegondev.usersmanagementsystem.dto.OrderDTO;
import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Book;
import com.phegondev.usersmanagementsystem.entity.Order;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.BookRepository;
import com.phegondev.usersmanagementsystem.repository.OrderRepository;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import com.phegondev.usersmanagementsystem.service.OrderService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UsersRepo usersRepo;

    // Get orders for the authenticated user
    @GetMapping
    public List<OrderDTO> getOrdersForAuthenticatedUser(Authentication authentication) {
        // Retrieve the current authenticated user
        OurUsers currentUser = (OurUsers) authentication.getPrincipal();

        // Fetch the orders for this user
        List<Order> orders = orderService.getOrdersByUserId(currentUser.getId());

        // Create a list to hold the transformed order DTOs
        List<OrderDTO> orderDTOList = new ArrayList<>();

        // Loop through the orders and populate the OrderDTOs
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());

            // Set book details in the BookDTO
            BookDTO bookDTO = new BookDTO();
            bookDTO.setId(order.getBook().getId());
            bookDTO.setName(order.getBook().getName());
            bookDTO.setAuthor(order.getBook().getAuthor());
            bookDTO.setPicture(order.getBook().getPicture());
            bookDTO.setIsbn(order.getBook().getIsbn());
            bookDTO.setTaken(order.getBook().getTaken());

            orderDTO.setBook(bookDTO);
            orderDTO.setOrderedBy(order.getOrderedBy());
            orderDTO.setOrderDate(order.getOrderDate());

            // Add the order DTO to the list
            orderDTOList.add(orderDTO);
        }

        return orderDTOList;
    }


    // Post a new order for a book
//    @PostMapping
//    public Order createOrder(@RequestBody Order order, Authentication authentication) {
//        OurUsers currentUser = (OurUsers) authentication.getPrincipal();
//        order.setUser(currentUser);
//        order.setOrderedBy(currentUser.getName());
//        order.setOrderDate(LocalDateTime.now());
//        return orderService.createOrder(order);
//    }
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order, Authentication authentication) {
        OurUsers currentUser = (OurUsers) authentication.getPrincipal();

        // Fetch the book to ensure it exists and isn't taken
        Book book = bookRepository.findById(order.getBook().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (book.isTaken()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This book is already taken.");
        }

        // Fetch the current user from the database
        OurUsers persistentUser = usersRepo.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Set the book as taken
        book.setTaken(true);
        bookRepository.save(book);

        // Set order details
        order.setUser(persistentUser);
        order.setOrderedBy(persistentUser.getName());
        order.setOrderDate(LocalDateTime.now());

        orderService.createOrder(order);

        // Return only the ordered book's information
        return ResponseEntity.ok(book);
    }

    // Admin: Update an order
//    @PutMapping("/{id}")
//    public Order updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
//        return orderService.updateOrder(id, updatedOrder);
//    }

//    @PutMapping("/{orderId}")
//    public ResponseEntity<?> updateOrder(
//            @PathVariable Long orderId,
//            @RequestBody Map<String, String> updatedFields,
//            Authentication authentication) {
//
//        // Debug: Check the current user
//        OurUsers currentUser = (OurUsers) authentication.getPrincipal();
//        System.out.println("Current User: " + currentUser.getUsername()); // Debug log to check if current user is correct
//
//        // Fetch the existing order
//        Order existingOrder = orderRepository.findById(orderId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
//
//        // Check if the current user is the owner or an admin
//        if (!existingOrder.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("You are not authorized to update this order.");
//        }
//
//        // Fetch the book associated with the order
//        Book book = existingOrder.getBook();
//
//        // Update fields only if provided in the request body
//        updatedFields.forEach((key, value) -> {
//            switch (key.toLowerCase()) {
//                case "name":
//                    book.setName(value);
//                    break;
//                case "author":
//                    book.setAuthor(value);
//                    break;
//                case "picture":
//                    book.setPicture(value);
//                    break;
//                case "isbn":
//                    book.setIsbn(value);
//                    break;
//                default:
//                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid field: " + key);
//            }
//        });
//
//        // Save the updated book
//        bookRepository.save(book);
//
//        // Return the updated book details
//        return ResponseEntity.ok(book);
//    }


    // Admin: Delete an order
//    @DeleteMapping("/{id}")
//    public void deleteOrder(@PathVariable Long id) {
//        orderService.deleteOrder(id);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        // Fetch the order by ID
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Get the book associated with the order and set it as available (i.e., not taken)
        Book book = order.getBook();
        book.setTaken(false);
        bookRepository.save(book);

        // Get the user associated with the order
        OurUsers user = order.getUser();

        // Remove the order from the user's list of orders
        user.getOrders().remove(order);

        // Set the user of the order to null to break the association
        order.setUser(null);

        // Delete the order
        orderRepository.delete(order);

        // Save the user to update their order list
        usersRepo.save(user);

        return ResponseEntity.ok("Order cancelled and book availability restored.");
    }


}