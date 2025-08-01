package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "borrowing_history")
public class BorrowingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType actionType;

    @NotNull
    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActionType {
        BORROWED, RETURNED
    }

    public BorrowingHistory() {
    }

    public BorrowingHistory(Book book, Borrower borrower, ActionType actionType) {
        this.book = book;
        this.borrower = borrower;
        this.actionType = actionType;
        this.actionDate = LocalDateTime.now();
        if (actionType == ActionType.BORROWED) {
            this.dueDate = LocalDateTime.now().plusWeeks(2); // Default 2 weeks borrowing period
        }
    }

    public BorrowingHistory(Book book, Borrower borrower, ActionType actionType, LocalDateTime dueDate) {
        this.book = book;
        this.borrower = borrower;
        this.actionType = actionType;
        this.actionDate = LocalDateTime.now();
        this.dueDate = dueDate;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (actionDate == null) {
            actionDate = LocalDateTime.now();
        }
    }

    // Business methods
    public boolean isOverdue() {
        return actionType == ActionType.BORROWED && 
               dueDate != null && 
               LocalDateTime.now().isAfter(dueDate);
    }

    public long getDaysUntilDue() {
        if (actionType != ActionType.BORROWED || dueDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public void setBorrower(Borrower borrower) {
        this.borrower = borrower;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowingHistory that = (BorrowingHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BorrowingHistory{" +
                "id=" + id +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", borrower=" + (borrower != null ? borrower.getName() : "null") +
                ", actionType=" + actionType +
                ", actionDate=" + actionDate +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                '}';
    }
}