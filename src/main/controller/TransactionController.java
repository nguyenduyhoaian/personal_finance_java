package main.controller;

import main.personalfinance.model.Transaction;
import main.personalfinance.model.Database;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionController {

    public static boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, description, transaction_date, type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transaction.getUserId());
            pstmt.setInt(2, transaction.getCategoryId());
            pstmt.setBigDecimal(3, transaction.getAmount());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            pstmt.setString(6, transaction.getType());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET category_id = ?, amount = ?, description = ?, " +
                "transaction_date = ?, type = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transaction.getCategoryId());
            pstmt.setBigDecimal(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setDate(4, Date.valueOf(transaction.getTransactionDate()));
            pstmt.setString(5, transaction.getType());
            pstmt.setInt(6, transaction.getId());
            pstmt.setInt(7, transaction.getUserId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteTransaction(int transactionId, int userId) {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Transaction> getTransactionsByUser(int userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.transaction_date BETWEEN ? AND ? " +
                "ORDER BY t.transaction_date DESC, t.created_at DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("id"));
                transaction.setUserId(rs.getInt("user_id"));
                transaction.setCategoryId(rs.getInt("category_id"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                transaction.setType(rs.getString("type"));
                transaction.setCategoryName(rs.getString("category_name"));

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public static List<Transaction> getRecentTransactions(int userId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.transaction_date DESC, t.created_at DESC " +
                "LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("id"));
                transaction.setUserId(rs.getInt("user_id"));
                transaction.setCategoryId(rs.getInt("category_id"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                transaction.setType(rs.getString("type"));
                transaction.setCategoryName(rs.getString("category_name"));

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public static BigDecimal getTotalIncome(int userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE user_id = ? AND type = 'INCOME' " +
                "AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public static BigDecimal getTotalExpense(int userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE user_id = ? AND type = 'EXPENSE' " +
                "AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public static List<Object[]> getExpenseByCategory(int userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT c.name, COALESCE(SUM(t.amount), 0) as total " +
                "FROM categories c " +
                "LEFT JOIN transactions t ON c.id = t.category_id " +
                "AND t.user_id = ? AND t.type = 'EXPENSE' " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "WHERE c.type = 'EXPENSE' AND (c.user_id = ? OR c.user_id IS NULL) " +
                "GROUP BY c.id, c.name " +
                "HAVING total > 0 " +
                "ORDER BY total DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setInt(4, userId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("name");
                row[1] = rs.getBigDecimal("total");
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
}