package main.view.components;

import main.controller.AuthController;
import main.controller.TransactionController;
import main.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class DashboardPanel extends JPanel {
    private JLabel lblTotalIncome, lblTotalExpense, lblBalance;
    private JTable recentTransactionsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthComboBox, yearComboBox;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Header with date filters
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Tổng quan tài chính");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Month filter
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        // Year filter
        String[] years = new String[5];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(String.valueOf(currentYear));

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshData());

        filterPanel.add(new JLabel("Tháng:"));
        filterPanel.add(monthComboBox);
        filterPanel.add(new JLabel("Năm:"));
        filterPanel.add(yearComboBox);
        filterPanel.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Statistics cards
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);

        // Recent transactions
        JPanel recentPanel = createRecentTransactionsPanel();
        add(recentPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Income card
        JPanel incomeCard = createStatCard("Tổng thu nhập", "0 VND", new Color(46, 204, 113));
        lblTotalIncome = (JLabel) ((JPanel) incomeCard.getComponent(1)).getComponent(0);

        // Expense card
        JPanel expenseCard = createStatCard("Tổng chi tiêu", "0 VND", new Color(231, 76, 60));
        lblTotalExpense = (JLabel) ((JPanel) expenseCard.getComponent(1)).getComponent(0);

        // Balance card
        JPanel balanceCard = createStatCard("Số dư", "0 VND", new Color(52, 152, 219));
        lblBalance = (JLabel) ((JPanel) balanceCard.getComponent(1)).getComponent(0);

        statsPanel.add(incomeCard);
        statsPanel.add(expenseCard);
        statsPanel.add(balanceCard);

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.DARK_GRAY);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valuePanel.setBackground(Color.WHITE);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valuePanel.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Giao dịch gần đây"));

        // Table
        String[] columns = {"Ngày", "Danh mục", "Mô tả", "Số tiền", "Loại"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTransactionsTable = new JTable(tableModel);
        recentTransactionsTable.setRowHeight(30);
        recentTransactionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        scrollPane.setPreferredSize(new Dimension(0, 200));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshData() {
        int selectedMonth = monthComboBox.getSelectedIndex() + 1;
        int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        int userId = AuthController.getCurrentUser().getId();

        // Get totals
        BigDecimal totalIncome = TransactionController.getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpense = TransactionController.getTotalExpense(userId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Update labels
        lblTotalIncome.setText(formatCurrency(totalIncome));
        lblTotalExpense.setText(formatCurrency(totalExpense));
        lblBalance.setText(formatCurrency(balance));

        // Update recent transactions
        updateRecentTransactions(userId);
    }

    private void updateRecentTransactions(int userId) {
        // Clear table
        tableModel.setRowCount(0);

        // Get recent transactions
        List<Transaction> transactions = TransactionController.getRecentTransactions(userId, 10);

        // Add rows to table
        for (Transaction transaction : transactions) {
            Object[] row = {
                    transaction.getTransactionDate().toString(),
                    transaction.getCategoryName(),
                    transaction.getDescription() != null ? transaction.getDescription() : "",
                    formatCurrency(transaction.getAmount()),
                    transaction.getType().equals("INCOME") ? "Thu nhập" : "Chi tiêu"
            };
            tableModel.addRow(row);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d VND", amount.longValue());
    }
}