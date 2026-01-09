package main.personalfinance.model;

import java.sql.*;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:database/finance.db";
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                createTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void createTables() {
        String[] tables = {
                // Bảng người dùng
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE NOT NULL," +
                        "password TEXT NOT NULL," +
                        "email TEXT," +
                        "full_name TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                // Bảng danh mục
                "CREATE TABLE IF NOT EXISTS categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "type TEXT CHECK(type IN ('INCOME', 'EXPENSE')) NOT NULL," +
                        "user_id INTEGER," +
                        "FOREIGN KEY(user_id) REFERENCES users(id)" +
                        ")",

                // Bảng giao dịch
                "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "category_id INTEGER NOT NULL," +
                        "amount DECIMAL(10,2) NOT NULL," +
                        "description TEXT," +
                        "transaction_date DATE NOT NULL," +
                        "type TEXT CHECK(type IN ('INCOME', 'EXPENSE')) NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(id)," +
                        "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                        ")"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                stmt.execute(table);
            }
            // Thêm danh mục mặc định
            insertDefaultCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertDefaultCategories() {
        String[][] defaultCategories = {
                {"Lương", "INCOME"},
                {"Tiền thưởng", "INCOME"},
                {"Đầu tư", "INCOME"},
                {"Khác", "INCOME"},
                {"Ăn uống", "EXPENSE"},
                {"Di chuyển", "EXPENSE"},
                {"Nhà ở", "EXPENSE"},
                {"Giải trí", "EXPENSE"},
                {"Mua sắm", "EXPENSE"},
                {"Y tế", "EXPENSE"},
                {"Giáo dục", "EXPENSE"},
                {"Khác", "EXPENSE"}
        };

        String checkSql = "SELECT COUNT(*) FROM categories WHERE user_id IS NULL";
        String insertSql = "INSERT OR IGNORE INTO categories (name, type, user_id) VALUES (?, ?, NULL)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                for (String[] category : defaultCategories) {
                    pstmt.setString(1, category[0]);
                    pstmt.setString(2, category[1]);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}