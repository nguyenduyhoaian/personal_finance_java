package main.controller;

import main.dao.Database;
import main.model.Category;
import main.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionController {

     // Hàm phụ trợ để tính tổng tiền theo loại (LƯU Ý SET Private)
    private static BigDecimal getTotalAmount(int userId, LocalDate startDate, LocalDate endDate, String type) {
        String sql = "SELECT SUM(amount) FROM transactions " +
                "WHERE user_id = ? AND type = ? AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.setDate(3, Date.valueOf(startDate));
            pstmt.setDate(4, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal amount = rs.getBigDecimal(1);
                return amount != null ? amount : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    //Tính tổng thu nhập trong khoảng thời gian
    public static BigDecimal getTotalIncome(int userId, LocalDate startDate, LocalDate endDate) {
        return getTotalAmount(userId, startDate, endDate, "INCOME");
    }

    //Tính tổng chi tiêu trong khoảng thời gian
    public static BigDecimal getTotalExpense(int userId, LocalDate startDate, LocalDate endDate) {
        return getTotalAmount(userId, startDate, endDate, "EXPENSE");
    }

    /**
     * Lấy danh sách chi tiêu gom nhóm theo danh mục (để vẽ biểu đồ/bảng)
     * Trả về List<Object[]>: [Tên danh mục, Số tiền]
     */
    public static List<Object[]> getExpenseByCategory(int userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT c.name, SUM(t.amount) as total " +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.type = 'EXPENSE' " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "GROUP BY c.name " +
                "ORDER BY total DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String categoryName = rs.getString("name");
                BigDecimal amount = rs.getBigDecimal("total");
                list.add(new Object[]{categoryName, amount});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

/**
 * Lấy danh sách giao dịch gần đây (có giới hạn số lượng) cho Dashboard
 */
public static List<Transaction> getRecentTransactions(int userId, int limit) {
    List<Transaction> list = new ArrayList<>();
    String sql = "SELECT t.*, c.name AS category_name " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.id " +
            "WHERE t.user_id = ? " +
            "ORDER BY t.transaction_date DESC, t.id DESC " +
            "LIMIT ?";

    try (Connection conn = Database.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, userId);
        pstmt.setInt(2, limit);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Transaction t = mapResultSetToTransaction(rs);
            list.add(t);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

/**
 * Lấy danh sách giao dịch trong khoảng thời gian (cho ReportPanel)
 */
public static List<Transaction> getTransactionsByUser(int userId, LocalDate startDate, LocalDate endDate) {
    List<Transaction> list = new ArrayList<>();
    String sql = "SELECT t.*, c.name AS category_name " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.id " +
            "WHERE t.user_id = ? " +
            "AND t.transaction_date BETWEEN ? AND ? " +
            "ORDER BY t.transaction_date DESC";

    try (Connection conn = Database.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, userId);
        pstmt.setDate(2, Date.valueOf(startDate));
        pstmt.setDate(3, Date.valueOf(endDate));
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Transaction t = mapResultSetToTransaction(rs);
            list.add(t);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

/**
 * Hàm phụ trợ để map ResultSet sang Object (tránh lặp code)
 */
private static Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
    Transaction t = new Transaction();
    t.setId(rs.getInt("id"));
    t.setUserId(rs.getInt("user_id"));
    t.setCategoryId(rs.getInt("category_id"));
    t.setAmount(rs.getBigDecimal("amount"));
    t.setDescription(rs.getString("description"));
    t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
    t.setType(rs.getString("type"));
    t.setCategoryName(rs.getString("category_name"));
    return t;
}

    public boolean addCategory(String name, String type) {
        String sql = "INSERT INTO categories (name, type) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, type);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách toàn bộ giao dịch của một người dùng cụ thể.
     * Có JOIN với bảng categories để lấy tên danh mục.
     */
    public List<Transaction> getAllTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        // Query kết hợp bảng transactions và categories để lấy category_name
        String sql = "SELECT t.*, c.name AS category_name " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.transaction_date DESC, t.id DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setCategoryId(rs.getInt("category_id"));
                t.setAmount(rs.getBigDecimal("amount")); //
                t.setDescription(rs.getString("description"));
                // Chuyển đổi java.sql.Date sang java.time.LocalDate
                t.setTransactionDate(rs.getDate("transaction_date").toLocalDate()); //
                t.setType(rs.getString("type"));

                // Set tên category lấy từ bảng đã JOIN
                t.setCategoryName(rs.getString("category_name")); //

                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới một giao dịch
     */
    public static boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, description, transaction_date, type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, t.getUserId());
            pstmt.setInt(2, t.getCategoryId());
            pstmt.setBigDecimal(3, t.getAmount());
            pstmt.setString(4, t.getDescription());
            // Chuyển đổi LocalDate sang java.sql.Date
            pstmt.setDate(5, java.sql.Date.valueOf(t.getTransactionDate()));
            pstmt.setString(6, t.getType());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin giao dịch
     */
    public static boolean updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET category_id = ?, amount = ?, description = ?, " +
                "transaction_date = ?, type = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, t.getCategoryId());
            pstmt.setBigDecimal(2, t.getAmount());
            pstmt.setString(3, t.getDescription());
            pstmt.setDate(4, java.sql.Date.valueOf(t.getTransactionDate()));
            pstmt.setString(5, t.getType());
            // Điều kiện WHERE
            pstmt.setInt(6, t.getId());
            pstmt.setInt(7, t.getUserId()); // Bảo mật: đảm bảo user chỉ sửa được của mình

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa giao dịch
     */
    public static boolean deleteTransaction(int transactionId) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách các danh mục (Category) để hiển thị lên ComboBox khi thêm/sửa
     * (Hỗ trợ lọc theo loại: INCOME hoặc EXPENSE nếu cần)
     */
    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Giả sử bạn có class Category với constructor tương ứng
                // Nếu chưa có file Category.java chi tiết, bạn cần tạo Model này
                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setType(rs.getString("type"));
                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}