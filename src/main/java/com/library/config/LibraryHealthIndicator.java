package com.library.config;

import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for the Library Management System.
 * Provides detailed health information about the application's core functionality.
 */
@Component
public class LibraryHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;

    @Autowired
    public LibraryHealthIndicator(BookRepository bookRepository, BorrowerRepository borrowerRepository) {
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
    }

    @Override
    public Health health() {
        try {
            // Check database connectivity by counting records
            long bookCount = bookRepository.count();
            long borrowerCount = borrowerRepository.count();
            long availableBooks = bookRepository.findAvailableBooks().size();
            long borrowedBooks = bookRepository.findBorrowedBooks().size();

            return Health.up()
                    .withDetail("status", "Library system is operational")
                    .withDetail("database", "Connected")
                    .withDetail("total-books", bookCount)
                    .withDetail("total-borrowers", borrowerCount)
                    .withDetail("available-books", availableBooks)
                    .withDetail("borrowed-books", borrowedBooks)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Library system is not operational")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}