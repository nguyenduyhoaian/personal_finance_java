package main.model;

import java.math.BigDecimal;

public class Category {
    private int id;
    private String name;
    private String type; // "INCOME" or "EXPENSE"
    private Integer userId;
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    // Constructors
    public Category() {}

    public Category(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }
}
