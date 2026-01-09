package main.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private int id;
    private int userId;
    private int categoryId;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private String type; // "INCOME" or "EXPENSE"
    private String categoryName;

    // Constructors
    public Transaction() {}

    public Transaction(int userId, int categoryId, BigDecimal amount,
                       String description, LocalDate transactionDate, String type) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}