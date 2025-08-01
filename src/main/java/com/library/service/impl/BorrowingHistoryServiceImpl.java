package com.library.service.impl;

import com.library.entity.BorrowingHistory;
import com.library.repository.BorrowingHistoryRepository;
import com.library.service.BorrowingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BorrowingHistoryServiceImpl implements BorrowingHistoryService {

    private final BorrowingHistoryRepository borrowingHistoryRepository;

    @Autowired
    public BorrowingHistoryServiceImpl(BorrowingHistoryRepository borrowingHistoryRepository) {
        this.borrowingHistoryRepository = borrowingHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getHistoryByBookId(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        return borrowingHistoryRepository.findByBookIdOrderByActionDateDesc(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getHistoryByBorrowerId(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return borrowingHistoryRepository.findByBorrowerIdOrderByActionDateDesc(borrowerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingHistory> getHistoryByBorrowerId(Long borrowerId, Pageable pageable) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return borrowingHistoryRepository.findByBorrowerIdOrderByActionDateDesc(borrowerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowingHistory> getHistoryByBookId(Long bookId, Pageable pageable) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return borrowingHistoryRepository.findByBookIdOrderByActionDateDesc(bookId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getCurrentlyBorrowedBooksByBorrower(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return borrowingHistoryRepository.findCurrentlyBorrowedBooksByBorrower(borrowerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getOverdueBooksByBorrower(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return borrowingHistoryRepository.findOverdueBorrowingsByBorrower(borrowerId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getAllOverdueBooks() {
        return borrowingHistoryRepository.findAllOverdueBorrowings(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BorrowingHistory> getMostRecentHistoryForBook(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        return borrowingHistoryRepository.findFirstByBookIdOrderByActionDateDesc(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return borrowingHistoryRepository.findByActionDateBetweenOrderByActionDateDesc(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getHistoryByBorrowerAndDateRange(Long borrowerId, LocalDateTime startDate, LocalDateTime endDate) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return borrowingHistoryRepository.findByBorrowerIdAndActionDateBetweenOrderByActionDateDesc(borrowerId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingHistory> getHistoryByBookAndDateRange(Long bookId, LocalDateTime startDate, LocalDateTime endDate) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return borrowingHistoryRepository.findByBookIdAndActionDateBetweenOrderByActionDateDesc(bookId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalBorrowingsByBorrower(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return borrowingHistoryRepository.countBorrowingsByBorrower(borrowerId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalBorrowingsForBook(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        return borrowingHistoryRepository.countBorrowingsForBook(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostPopularBooks(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return borrowingHistoryRepository.findMostPopularBooks(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostActiveBorrowers(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return borrowingHistoryRepository.findMostActiveBorrowers(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverdueBooks(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return !borrowingHistoryRepository.findOverdueBorrowingsByBorrower(borrowerId, LocalDateTime.now()).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCurrentBorrowingCount(Long borrowerId) {
        if (borrowerId == null) {
            throw new IllegalArgumentException("Borrower ID cannot be null");
        }
        return borrowingHistoryRepository.findCurrentlyBorrowedBooksByBorrower(borrowerId).size();
    }
}