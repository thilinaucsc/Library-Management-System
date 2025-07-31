package com.library.service.impl;

import com.library.entity.Borrower;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import com.library.service.BorrowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class BorrowerServiceImpl implements BorrowerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;

    @Autowired
    public BorrowerServiceImpl(BorrowerRepository borrowerRepository, BookRepository bookRepository) {
        this.borrowerRepository = borrowerRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public Borrower registerBorrower(String name, String email) {
        validateBorrowerData(name, email);
        
        if (existsByEmail(email)) {
            throw new IllegalStateException("Email already exists: " + email);
        }

        Borrower borrower = new Borrower(name, email);
        return borrowerRepository.save(borrower);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Borrower> findBorrowerById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return borrowerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Borrower> findBorrowerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return borrowerRepository.findByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Borrower> searchBorrowersByName(String namePattern) {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Name pattern cannot be null or empty");
        }
        return borrowerRepository.findByNameContainingIgnoreCase(namePattern.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Borrower> getBorrowersWithBooks() {
        return borrowerRepository.findBorrowersWithBooks();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Borrower> getBorrowersWithoutBooks() {
        return borrowerRepository.findBorrowersWithoutBooks();
    }

    @Override
    public Borrower updateBorrower(Long id, String name, String email) {
        if (id == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }

        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found with ID: " + id));

        boolean updated = false;

        if (name != null && !name.trim().isEmpty()) {
            validateName(name);
            if (!name.trim().equals(borrower.getName())) {
                borrower.setName(name.trim());
                updated = true;
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            String normalizedEmail = email.trim().toLowerCase();
            validateEmail(normalizedEmail);
            
            if (!normalizedEmail.equals(borrower.getEmail())) {
                if (existsByEmail(normalizedEmail)) {
                    throw new IllegalStateException("Email already exists: " + normalizedEmail);
                }
                borrower.setEmail(normalizedEmail);
                updated = true;
            }
        }

        if (updated) {
            return borrowerRepository.save(borrower);
        }

        return borrower;
    }

    @Override
    public void deleteBorrower(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }

        if (!existsById(id)) {
            throw new IllegalArgumentException("Borrower not found with ID: " + id);
        }

        long borrowedBooksCount = getBorrowedBookCount(id);
        if (borrowedBooksCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete borrower with ID " + id + ". They have " + borrowedBooksCount + " borrowed books."
            );
        }

        borrowerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return borrowerRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return borrowerRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public long getBorrowedBookCount(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return bookRepository.findBooksByBorrowerId(borrowerId).size();
    }

    @Override
    public void validateBorrowerData(String name, String email) {
        validateName(name);
        validateEmail(email);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        String trimmedName = name.trim();
        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters long");
        }
        
        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
        
        // Check for valid characters (letters, spaces, hyphens, apostrophes)
        if (!trimmedName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException("Name can only contain letters, spaces, hyphens, and apostrophes");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        if (trimmedEmail.length() > 150) {
            throw new IllegalArgumentException("Email cannot exceed 150 characters");
        }
        
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Email format is invalid: " + email);
        }
    }
}