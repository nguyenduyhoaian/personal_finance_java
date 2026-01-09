package main.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    // Tên file cơ sở dữ liệu SQLite
    private static final String URL = "jdbc:sqlite:src/database/personal_finance.db";

    static {
        // Tự động khởi tạo các bảng khi lớp Database được load
        initializeDatabase();
    }

    // Cung cấp kết nối tới cơ sở dữ liệu
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Tạo các bảng cần thiết nếu chúng chưa tồn tại
     */
    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Tạo bảng users dựa trên AuthController và User model
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "password TEXT NOT NULL, "
                    + "email TEXT, "
                    + "full_name TEXT, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createUsersTable);

            // 2. Tạo bảng categories
            String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "type TEXT NOT NULL" // INCOME hoặc EXPENSE
                    + "budget_limit DECIMAL(15, 2) DEFAULT 0"
                    + ");";
            stmt.execute(createCategoriesTable);

            // 3. Tạo bảng transactions dựa trên Transaction model
            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER, "
                    + "category_id INTEGER, "
                    + "amount DECIMAL(15, 2) NOT NULL, "
                    + "description TEXT, "
                    + "transaction_date DATE NOT NULL, "
                    + "type TEXT NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id), "
                    + "FOREIGN KEY (category_id) REFERENCES categories(id)"
                    + ");";
            stmt.execute(createTransactionsTable);

            //Tạo bảng recurring_tasks
            String createRecurringTable = "CREATE TABLE IF NOT EXISTS recurring_tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER, "
                    + "category_id INTEGER, "
                    + "amount DECIMAL(15, 2) NOT NULL, "
                    + "description TEXT, "
                    + "day_of_month INTEGER NOT NULL, " // Ngày thực hiện (1-31)
                    + "last_executed_month TEXT, " // Lưu tháng gần nhất đã chạy (dạng "MM/yyyy")
                    + "FOREIGN KEY (user_id) REFERENCES users(id)"
                    + ");";
            stmt.execute(createRecurringTable);

            // Thêm dữ liệu mẫu cho Category nếu bảng trống
            seedCategories(stmt);

            System.out.println("Cơ sở dữ liệu đã được khởi tạo thành công.");

        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedCategories(Statement stmt) throws SQLException {
        // Kiểm tra xem đã có category nào chưa
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO categories (name, type) VALUES ('Lương', 'INCOME')");
            stmt.execute("INSERT INTO categories (name, type) VALUES ('Ăn uống', 'EXPENSE')");
            stmt.execute("INSERT INTO categories (name, type) VALUES ('Di chuyển', 'EXPENSE')");
            stmt.execute("INSERT INTO categories (name, type) VALUES ('Thưởng', 'INCOME')");
        }
    }
}