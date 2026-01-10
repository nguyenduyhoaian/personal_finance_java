package main.controller;

import main.dao.Database;
import main.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RecurringService {

    /**
     * Hàm này kiểm tra và tự động sinh giao dịch nếu đến hạn
     */
    public static void checkAndExecuteTasks(int userId) {
        LocalDate today = LocalDate.now();
        int currentDay = today.getDayOfMonth();
        String currentMonthYear = today.format(DateTimeFormatter.ofPattern("MM/yyyy"));

        String sql = "SELECT * FROM recurring_tasks WHERE user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            TransactionController transController = new TransactionController();

            while (rs.next()) {
                int taskId = rs.getInt("id");
                int runDay = rs.getInt("day_of_month");
                String lastRun = rs.getString("last_executed_month");

                // Logic:
                // 1. Nếu tháng hiện tại KHÁC tháng chạy gần nhất (nghĩa là tháng này chưa chạy)
                // 2. VÀ hôm nay đã vượt qua hoặc bằng ngày cài đặt
                if (!currentMonthYear.equals(lastRun) && currentDay >= runDay) {

                    // -> TỰ ĐỘNG THÊM GIAO DỊCH
                    Transaction t = new Transaction();
                    t.setUserId(userId);
                    t.setCategoryId(rs.getInt("category_id"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setDescription("[Tự động] " + rs.getString("description"));
                    t.setTransactionDate(today); // Ngày giao dịch là hôm nay
                    t.setType("EXPENSE"); // Mặc định là chi tiêu (hoặc lưu type trong DB nếu cần)

                    transController.addTransaction(t);

                    // -> CẬP NHẬT LẠI recurring_tasks ĐỂ KHÔNG CHẠY LẠI TRONG THÁNG NÀY
                    updateLastExecution(taskId, currentMonthYear);

                    System.out.println("Đã chạy giao dịch tự động: " + t.getDescription());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateLastExecution(int taskId, String monthYear) {
        String sql = "UPDATE recurring_tasks SET last_executed_month = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, monthYear);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}