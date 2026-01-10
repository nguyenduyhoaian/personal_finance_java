package main.controller;

import main.dao.Database;
import main.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportController {

    /**
     * Lấy tổng thu nhập và chi tiêu theo tháng trong một khoảng thời gian
     */
    public static Map<String, BigDecimal> getMonthlySummary(int userId, LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> summary = new HashMap<>();

        String sql = "SELECT " +
                "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as total_income, " +
                "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as total_expense " +
                "FROM transactions " +
                "WHERE user_id = ? AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                summary.put("income", rs.getBigDecimal("total_income"));
                summary.put("expense", rs.getBigDecimal("total_expense"));
                summary.put("balance", rs.getBigDecimal("total_income").subtract(rs.getBigDecimal("total_expense")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * Lấy chi tiết chi tiêu theo danh mục
     */
    public static List<Map<String, Object>> getExpenseByCategory(int userId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> results = new ArrayList<>();

        String sql = "SELECT c.name as category_name, " +
                "COALESCE(SUM(t.amount), 0) as total_amount, " +
                "COUNT(t.id) as transaction_count " +
                "FROM categories c " +
                "LEFT JOIN transactions t ON c.id = t.category_id " +
                "AND t.user_id = ? AND t.type = 'EXPENSE' " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "WHERE c.type = 'EXPENSE' AND (c.user_id = ? OR c.user_id IS NULL) " +
                "GROUP BY c.id, c.name " +
                "HAVING total_amount > 0 " +
                "ORDER BY total_amount DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setInt(4, userId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category_name"));
                row.put("amount", rs.getBigDecimal("total_amount"));
                row.put("count", rs.getInt("transaction_count"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Lấy chi tiết thu nhập theo danh mục
     */
    public static List<Map<String, Object>> getIncomeByCategory(int userId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> results = new ArrayList<>();

        String sql = "SELECT c.name as category_name, " +
                "COALESCE(SUM(t.amount), 0) as total_amount, " +
                "COUNT(t.id) as transaction_count " +
                "FROM categories c " +
                "LEFT JOIN transactions t ON c.id = t.category_id " +
                "AND t.user_id = ? AND t.type = 'INCOME' " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "WHERE c.type = 'INCOME' AND (c.user_id = ? OR c.user_id IS NULL) " +
                "GROUP BY c.id, c.name " +
                "HAVING total_amount > 0 " +
                "ORDER BY total_amount DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setInt(4, userId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category_name"));
                row.put("amount", rs.getBigDecimal("total_amount"));
                row.put("count", rs.getInt("transaction_count"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Lấy dữ liệu xu hướng thu chi theo tháng
     */
    public static List<Map<String, Object>> getMonthlyTrend(int userId, int numberOfMonths) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        LocalDate endDate = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1);
        LocalDate startDate = endDate.minusMonths(numberOfMonths - 1).withDayOfMonth(1);

        String sql = "SELECT " +
                "strftime('%Y-%m', transaction_date) as month, " +
                "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as monthly_income, " +
                "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as monthly_expense, " +
                "COUNT(CASE WHEN type = 'INCOME' THEN 1 END) as income_count, " +
                "COUNT(CASE WHEN type = 'EXPENSE' THEN 1 END) as expense_count " +
                "FROM transactions " +
                "WHERE user_id = ? AND transaction_date BETWEEN ? AND ? " +
                "GROUP BY strftime('%Y-%m', transaction_date) " +
                "ORDER BY month";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", rs.getString("month"));
                monthData.put("income", rs.getBigDecimal("monthly_income"));
                monthData.put("expense", rs.getBigDecimal("monthly_expense"));
                monthData.put("balance", rs.getBigDecimal("monthly_income")
                        .subtract(rs.getBigDecimal("monthly_expense")));
                monthData.put("income_count", rs.getInt("income_count"));
                monthData.put("expense_count", rs.getInt("expense_count"));

                // Tính tỷ lệ tiết kiệm
                BigDecimal income = rs.getBigDecimal("monthly_income");
                BigDecimal expense = rs.getBigDecimal("monthly_expense");
                BigDecimal savingsRate = BigDecimal.ZERO;
                if (income.compareTo(BigDecimal.ZERO) > 0) {
                    savingsRate = income.subtract(expense)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(income, 2, BigDecimal.ROUND_HALF_UP);
                }
                monthData.put("savings_rate", savingsRate);

                trendData.add(monthData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Đảm bảo có đủ số tháng được yêu cầu
        return fillMissingMonths(trendData, startDate, numberOfMonths);
    }

    /**
     * Điền dữ liệu cho các tháng không có giao dịch
     */
    private static List<Map<String, Object>> fillMissingMonths(List<Map<String, Object>> trendData,
                                                               LocalDate startDate, int numberOfMonths) {
        List<Map<String, Object>> filledData = new ArrayList<>();
        LocalDate current = startDate.withDayOfMonth(1);

        for (int i = 0; i < numberOfMonths; i++) {
            String monthKey = current.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

            // Tìm dữ liệu cho tháng hiện tại
            Map<String, Object> monthData = findMonthData(trendData, monthKey);

            if (monthData == null) {
                // Tạo dữ liệu mặc định cho tháng không có giao dịch
                monthData = new HashMap<>();
                monthData.put("month", monthKey);
                monthData.put("income", BigDecimal.ZERO);
                monthData.put("expense", BigDecimal.ZERO);
                monthData.put("balance", BigDecimal.ZERO);
                monthData.put("income_count", 0);
                monthData.put("expense_count", 0);
                monthData.put("savings_rate", BigDecimal.ZERO);
            }

            filledData.add(monthData);
            current = current.plusMonths(1);
        }

        return filledData;
    }

    private static Map<String, Object> findMonthData(List<Map<String, Object>> trendData, String monthKey) {
        for (Map<String, Object> data : trendData) {
            if (data.get("month").equals(monthKey)) {
                return data;
            }
        }
        return null;
    }

    /**
     * Lấy thống kê chi tiêu theo ngày trong tuần
     */
    public static List<Map<String, Object>> getExpenseByDayOfWeek(int userId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> results = new ArrayList<>();

        String[] daysOfWeek = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};

        String sql = "SELECT " +
                "CAST(strftime('%w', transaction_date) as INTEGER) as day_of_week, " +
                "COALESCE(SUM(amount), 0) as total_amount, " +
                "COUNT(id) as transaction_count, " +
                "AVG(amount) as avg_amount " +
                "FROM transactions " +
                "WHERE user_id = ? AND type = 'EXPENSE' " +
                "AND transaction_date BETWEEN ? AND ? " +
                "GROUP BY strftime('%w', transaction_date) " +
                "ORDER BY day_of_week";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                int dayIndex = rs.getInt("day_of_week");
                row.put("day_of_week", daysOfWeek[dayIndex]);
                row.put("amount", rs.getBigDecimal("total_amount"));
                row.put("count", rs.getInt("transaction_count"));
                row.put("avg_amount", rs.getBigDecimal("avg_amount"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Lấy top 5 giao dịch chi tiêu lớn nhất
     */
    public static List<Map<String, Object>> getTopExpenses(int userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();

        String sql = "SELECT t.id, t.amount, t.description, t.transaction_date, c.name as category_name " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.type = 'EXPENSE' " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "ORDER BY t.amount DESC " +
                "LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setInt(4, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("amount", rs.getBigDecimal("amount"));
                row.put("description", rs.getString("description"));
                row.put("date", rs.getDate("transaction_date").toLocalDate());
                row.put("category", rs.getString("category_name"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Lấy top 5 giao dịch thu nhập lớn nhất
     */
    public static List<Map<String, Object>> getTopIncomes(int userId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();

        String sql = "SELECT t.id, t.amount, t.description, t.transaction_date, c.name as category_name " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.type = 'INCOME' " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "ORDER BY t.amount DESC " +
                "LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setInt(4, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("amount", rs.getBigDecimal("amount"));
                row.put("description", rs.getString("description"));
                row.put("date", rs.getDate("transaction_date").toLocalDate());
                row.put("category", rs.getString("category_name"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Lấy thống kê chi tiêu theo thời gian trong ngày
     */
    public static List<Map<String, Object>> getExpenseByTimeOfDay(int userId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> results = new ArrayList<>();

        String sql = "SELECT " +
                "CASE " +
                "  WHEN CAST(strftime('%H', created_at) as INTEGER) BETWEEN 0 AND 5 THEN 'Đêm khuya (0h-6h)' " +
                "  WHEN CAST(strftime('%H', created_at) as INTEGER) BETWEEN 6 AND 11 THEN 'Buổi sáng (6h-12h)' " +
                "  WHEN CAST(strftime('%H', created_at) as INTEGER) BETWEEN 12 AND 17 THEN 'Buổi chiều (12h-18h)' " +
                "  ELSE 'Buổi tối (18h-24h)' " +
                "END as time_period, " +
                "COALESCE(SUM(amount), 0) as total_amount, " +
                "COUNT(id) as transaction_count " +
                "FROM transactions " +
                "WHERE user_id = ? AND type = 'EXPENSE' " +
                "AND transaction_date BETWEEN ? AND ? " +
                "GROUP BY time_period " +
                "ORDER BY " +
                "CASE time_period " +
                "  WHEN 'Buổi sáng (6h-12h)' THEN 1 " +
                "  WHEN 'Buổi chiều (12h-18h)' THEN 2 " +
                "  WHEN 'Buổi tối (18h-24h)' THEN 3 " +
                "  WHEN 'Đêm khuya (0h-6h)' THEN 4 " +
                "END";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("time_period", rs.getString("time_period"));
                row.put("amount", rs.getBigDecimal("total_amount"));
                row.put("count", rs.getInt("transaction_count"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Lấy tổng hợp thống kê nâng cao
     */
    public static Map<String, Object> getAdvancedStatistics(int userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();

        // 1. Tổng thu, tổng chi, số dư
        Map<String, BigDecimal> summary = getMonthlySummary(userId, startDate, endDate);
        statistics.putAll(summary);

        // 2. Số lượng giao dịch
        String countSql = "SELECT " +
                "COUNT(CASE WHEN type = 'INCOME' THEN 1 END) as income_count, " +
                "COUNT(CASE WHEN type = 'EXPENSE' THEN 1 END) as expense_count " +
                "FROM transactions " +
                "WHERE user_id = ? AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(countSql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                statistics.put("income_count", rs.getInt("income_count"));
                statistics.put("expense_count", rs.getInt("expense_count"));
                statistics.put("total_count", rs.getInt("income_count") + rs.getInt("expense_count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Trung bình theo giao dịch
        BigDecimal totalIncome = summary.get("income");
        BigDecimal totalExpense = summary.get("expense");
        int incomeCount = (int) statistics.get("income_count");
        int expenseCount = (int) statistics.get("expense_count");

        BigDecimal avgIncome = incomeCount > 0 ?
                totalIncome.divide(BigDecimal.valueOf(incomeCount), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
        BigDecimal avgExpense = expenseCount > 0 ?
                totalExpense.divide(BigDecimal.valueOf(expenseCount), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        statistics.put("avg_income_per_transaction", avgIncome);
        statistics.put("avg_expense_per_transaction", avgExpense);

        // 4. Tỷ lệ tiết kiệm
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = totalIncome.subtract(totalExpense)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, BigDecimal.ROUND_HALF_UP);
        }
        statistics.put("savings_rate", savingsRate);

        // 5. Ngày có chi tiêu cao nhất/thấp nhất
        String extremeSql = "SELECT " +
                "MIN(transaction_date) as first_date, " +
                "MAX(transaction_date) as last_date, " +
                "(SELECT transaction_date FROM transactions " +
                " WHERE user_id = ? AND type = 'EXPENSE' " +
                " AND transaction_date BETWEEN ? AND ? " +
                " ORDER BY amount DESC LIMIT 1) as highest_expense_date, " +
                "(SELECT transaction_date FROM transactions " +
                " WHERE user_id = ? AND type = 'INCOME' " +
                " AND transaction_date BETWEEN ? AND ? " +
                " ORDER BY amount DESC LIMIT 1) as highest_income_date " +
                "FROM transactions " +
                "WHERE user_id = ? AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(extremeSql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            pstmt.setInt(4, userId);
            pstmt.setDate(5, Date.valueOf(startDate));
            pstmt.setDate(6, Date.valueOf(endDate));
            pstmt.setInt(7, userId);
            pstmt.setDate(8, Date.valueOf(startDate));
            pstmt.setDate(9, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                statistics.put("first_date", rs.getDate("first_date"));
                statistics.put("last_date", rs.getDate("last_date"));
                statistics.put("highest_expense_date", rs.getDate("highest_expense_date"));
                statistics.put("highest_income_date", rs.getDate("highest_income_date"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 6. Số ngày có giao dịch
        String activeDaysSql = "SELECT COUNT(DISTINCT transaction_date) as active_days " +
                "FROM transactions " +
                "WHERE user_id = ? AND transaction_date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(activeDaysSql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                statistics.put("active_days", rs.getInt("active_days"));

                // Tính số ngày trong khoảng thời gian
                long totalDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
                BigDecimal activityRate = BigDecimal.valueOf(rs.getInt("active_days"))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalDays), 2, BigDecimal.ROUND_HALF_UP);
                statistics.put("activity_rate", activityRate);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statistics;
    }

    /**
     * Lấy danh sách các tháng có dữ liệu giao dịch
     */
    public static List<String> getMonthsWithData(int userId) {
        List<String> months = new ArrayList<>();

        String sql = "SELECT DISTINCT strftime('%Y-%m', transaction_date) as month " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "ORDER BY month DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                months.add(rs.getString("month"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return months;
    }

    /**
     * Lấy tổng hợp nhanh cho dashboard
     */
    public static Map<String, Object> getQuickOverview(int userId) {
        Map<String, Object> overview = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Thống kê tháng hiện tại
        Map<String, BigDecimal> currentMonth = getMonthlySummary(userId, firstDayOfMonth, lastDayOfMonth);
        overview.put("current_month", currentMonth);

        // Thống kê tháng trước
        LocalDate firstDayOfLastMonth = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        Map<String, BigDecimal> lastMonth = getMonthlySummary(userId, firstDayOfLastMonth, lastDayOfLastMonth);
        overview.put("last_month", lastMonth);

        // Tính phần trăm thay đổi
        BigDecimal currentIncome = currentMonth.get("income");
        BigDecimal lastIncome = lastMonth.get("income");
        BigDecimal incomeChange = calculatePercentageChange(currentIncome, lastIncome);
        overview.put("income_change", incomeChange);

        BigDecimal currentExpense = currentMonth.get("expense");
        BigDecimal lastExpense = lastMonth.get("expense");
        BigDecimal expenseChange = calculatePercentageChange(currentExpense, lastExpense);
        overview.put("expense_change", expenseChange);

        // Top 5 chi tiêu tháng này
        List<Map<String, Object>> topExpenses = getTopExpenses(userId, firstDayOfMonth, lastDayOfMonth, 5);
        overview.put("top_expenses", topExpenses);

        // Top 5 thu nhập tháng này
        List<Map<String, Object>> topIncomes = getTopIncomes(userId, firstDayOfMonth, lastDayOfMonth, 5);
        overview.put("top_incomes", topIncomes);

        // Giao dịch gần đây (7 ngày)
        LocalDate weekAgo = today.minusDays(7);
        List<Transaction> recentTransactions = TransactionController.getTransactionsByUser(
                userId, weekAgo, today);
        overview.put("recent_transactions", recentTransactions);

        return overview;
    }

    /**
     * Tính phần trăm thay đổi
     */
    private static BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ?
                    BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Xuất dữ liệu báo cáo ra file CSV
     */
    public static boolean exportToCSV(int userId, LocalDate startDate, LocalDate endDate, String filePath) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Ngày,Danh mục,Mô tả,Số tiền,Loại\n");

        String sql = "SELECT t.transaction_date, c.name as category_name, " +
                "t.description, t.amount, t.type " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.transaction_date BETWEEN ? AND ? " +
                "ORDER BY t.transaction_date DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                csv.append(rs.getDate("transaction_date")).append(",");
                csv.append("\"").append(rs.getString("category_name")).append("\",");
                csv.append("\"").append(rs.getString("description") != null ?
                        rs.getString("description").replace("\"", "\"\"") : "").append("\",");
                csv.append(rs.getBigDecimal("amount")).append(",");
                csv.append(rs.getString("type").equals("INCOME") ? "Thu nhập" : "Chi tiêu").append("\n");
            }

            // Ghi ra file
            java.io.FileWriter writer = new java.io.FileWriter(filePath);
            writer.write(csv.toString());
            writer.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo báo cáo chi tiết cho in ấn
     */
    public static Map<String, Object> generatePrintReport(int userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Thông tin cơ bản
        report.put("start_date", startDate);
        report.put("end_date", endDate);
        report.put("generated_date", LocalDate.now());

        // Tổng hợp
        Map<String, Object> statistics = getAdvancedStatistics(userId, startDate, endDate);
        report.put("statistics", statistics);

        // Chi tiết theo danh mục
        List<Map<String, Object>> expenseByCategory = getExpenseByCategory(userId, startDate, endDate);
        report.put("expense_by_category", expenseByCategory);

        List<Map<String, Object>> incomeByCategory = getIncomeByCategory(userId, startDate, endDate);
        report.put("income_by_category", incomeByCategory);

        // Top giao dịch
        List<Map<String, Object>> topExpenses = getTopExpenses(userId, startDate, endDate, 10);
        report.put("top_expenses", topExpenses);

        List<Map<String, Object>> topIncomes = getTopIncomes(userId, startDate, endDate, 10);
        report.put("top_incomes", topIncomes);

        // Xu hướng
        List<Map<String, Object>> monthlyTrend = getMonthlyTrend(userId, 6);
        report.put("monthly_trend", monthlyTrend);

        // Thống kê theo ngày trong tuần
        List<Map<String, Object>> expenseByDay = getExpenseByDayOfWeek(userId, startDate, endDate);
        report.put("expense_by_day", expenseByDay);

        // Thống kê theo thời gian trong ngày
        List<Map<String, Object>> expenseByTime = getExpenseByTimeOfDay(userId, startDate, endDate);
        report.put("expense_by_time", expenseByTime);

        return report;
    }
}