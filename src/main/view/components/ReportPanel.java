package main.view.components;

import main.controller.AuthController;
import main.controller.TransactionController;
import main.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportPanel extends JPanel {
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> monthCombo, yearCombo;
    private JButton btnGenerate, btnExport;
    private JPanel chartPanel;
    private CardLayout chartCardLayout;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshCharts();
    }

    private void initComponents() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Báo cáo & Thống kê");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // Report type
        String[] reportTypes = {"Phân bổ chi tiêu", "Thu chi theo tháng", "Xu hướng thu chi"};
        reportTypeCombo = new JComboBox<>(reportTypes);

        // Month and year selectors
        monthCombo = new JComboBox<>(new String[]{
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        });
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        String[] years = new String[5];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(String.valueOf(currentYear));

        // Buttons
        btnGenerate = createButton("Tạo báo cáo", new Color(52, 152, 219));
        btnExport = createButton("Xuất báo cáo", new Color(46, 204, 113));

        controlPanel.add(new JLabel("Loại báo cáo:"));
        controlPanel.add(reportTypeCombo);
        controlPanel.add(new JLabel("Tháng:"));
        controlPanel.add(monthCombo);
        controlPanel.add(new JLabel("Năm:"));
        controlPanel.add(yearCombo);
        controlPanel.add(btnGenerate);
        controlPanel.add(btnExport);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Chart panel with CardLayout
        chartCardLayout = new CardLayout();
        chartPanel = new JPanel(chartCardLayout);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Initialize all chart panels
        chartPanel.add(createExpenseTablePanel(), "TABLE");
        chartPanel.add(createIncomeExpenseSummaryPanel(), "SUMMARY");
        chartPanel.add(createTrendTablePanel(), "TREND");

        add(chartPanel, BorderLayout.CENTER);

        // Summary statistics panel
        JPanel statsPanel = createStatisticsPanel();
        add(statsPanel, BorderLayout.SOUTH);

        // Event listeners
        setupEventListeners();
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        //button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Arial", Font.PLAIN, 12));

        return button;
    }

    private JPanel createExpenseTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Phân bổ chi tiêu theo danh mục", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create table
        String[] columns = {"Danh mục", "Số tiền (VND)", "Tỷ lệ %"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);

        // Add some sample data
        updateExpenseTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateExpenseTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        int userId = AuthController.getCurrentUser().getId();
        List<Object[]> expenseByCategory = TransactionController.getExpenseByCategory(
                userId, startDate, endDate);

        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Object[] row : expenseByCategory) {
            BigDecimal amount = (BigDecimal) row[1];
            totalExpense = totalExpense.add(amount);
        }

        // Add rows to table
        for (Object[] row : expenseByCategory) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0 ?
                    amount.multiply(BigDecimal.valueOf(100)).divide(totalExpense, 1, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            Object[] tableRow = {
                    category,
                    formatCurrency(amount),
                    percentage.toString() + "%"
            };
            tableModel.addRow(tableRow);
        }

        // Add total row
        if (expenseByCategory.size() > 0) {
            Object[] totalRow = {
                    "<html><b>TỔNG CỘNG</b></html>",
                    "<html><b>" + formatCurrency(totalExpense) + "</b></html>",
                    "<html><b>100%</b></html>"
            };
            tableModel.addRow(totalRow);
        }
    }

    private JPanel createIncomeExpenseSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("So sánh thu nhập và chi tiêu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create a simple visualization using JLabels and JProgressBar
        JPanel visualizationPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        updateIncomeExpenseVisualization(visualizationPanel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(visualizationPanel, BorderLayout.CENTER);

        return panel;
    }

    private void updateIncomeExpenseVisualization(JPanel panel) {
        panel.removeAll();

        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        int userId = AuthController.getCurrentUser().getId();
        BigDecimal totalIncome = TransactionController.getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpense = TransactionController.getTotalExpense(userId, startDate, endDate);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Income section
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblIncome = new JLabel("Thu nhập:");
        lblIncome.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblIncome, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        JLabel lblIncomeValue = new JLabel(formatCurrency(totalIncome));
        lblIncomeValue.setFont(new Font("Arial", Font.BOLD, 14));
        lblIncomeValue.setForeground(new Color(46, 204, 113));
        panel.add(lblIncomeValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JProgressBar incomeBar = new JProgressBar(0, totalIncome.add(totalExpense).intValue());
        incomeBar.setValue(totalIncome.intValue());
        incomeBar.setForeground(new Color(46, 204, 113));
        incomeBar.setString(formatCurrency(totalIncome));
        incomeBar.setStringPainted(true);
        panel.add(incomeBar, gbc);

        // Expense section
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblExpense = new JLabel("Chi tiêu:");
        lblExpense.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblExpense, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        JLabel lblExpenseValue = new JLabel(formatCurrency(totalExpense));
        lblExpenseValue.setFont(new Font("Arial", Font.BOLD, 14));
        lblExpenseValue.setForeground(new Color(231, 76, 60));
        panel.add(lblExpenseValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JProgressBar expenseBar = new JProgressBar(0, totalIncome.add(totalExpense).intValue());
        expenseBar.setValue(totalExpense.intValue());
        expenseBar.setForeground(new Color(231, 76, 60));
        expenseBar.setString(formatCurrency(totalExpense));
        expenseBar.setStringPainted(true);
        panel.add(expenseBar, gbc);

        // Balance section
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblBalance = new JLabel("Số dư:");
        lblBalance.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblBalance, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        BigDecimal balance = totalIncome.subtract(totalExpense);
        JLabel lblBalanceValue = new JLabel(formatCurrency(balance));
        lblBalanceValue.setFont(new Font("Arial", Font.BOLD, 14));
        lblBalanceValue.setForeground(new Color(52, 152, 219));
        panel.add(lblBalanceValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JProgressBar balanceBar = new JProgressBar(0, 100);
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            int balancePercent = balance.multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 0, RoundingMode.HALF_UP).intValue();
            balanceBar.setValue(Math.max(0, balancePercent));
        }
        balanceBar.setForeground(new Color(52, 152, 219));
        balanceBar.setString("Tỷ lệ tiết kiệm: " + (totalIncome.compareTo(BigDecimal.ZERO) > 0 ?
                balance.multiply(BigDecimal.valueOf(100))
                        .divide(totalIncome, 1, RoundingMode.HALF_UP) + "%" : "0%"));
        balanceBar.setStringPainted(true);
        panel.add(balanceBar, gbc);

        panel.revalidate();
        panel.repaint();
    }

    private JPanel createTrendTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Xu hướng thu chi 6 tháng gần đây", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create table
        String[] columns = {"Tháng", "Thu nhập", "Chi tiêu", "Số dư", "Tỷ lệ tiết kiệm"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);

        // Add data
        updateTrendTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateTrendTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        int userId = AuthController.getCurrentUser().getId();
        LocalDate endDate = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1);
        LocalDate startDate = endDate.minusMonths(6).withDayOfMonth(1);

        // Create monthly data
        LocalDate currentMonth = startDate.withDayOfMonth(1);

        BigDecimal grandTotalIncome = BigDecimal.ZERO;
        BigDecimal grandTotalExpense = BigDecimal.ZERO;

        while (!currentMonth.isAfter(endDate)) {
            YearMonth yearMonth = YearMonth.from(currentMonth);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();

            BigDecimal monthlyIncome = TransactionController.getTotalIncome(userId, monthStart, monthEnd);
            BigDecimal monthlyExpense = TransactionController.getTotalExpense(userId, monthStart, monthEnd);
            BigDecimal monthlyBalance = monthlyIncome.subtract(monthlyExpense);

            String savingsRate = "0%";
            if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal rate = monthlyBalance.multiply(BigDecimal.valueOf(100))
                        .divide(monthlyIncome, 1, RoundingMode.HALF_UP);
                savingsRate = rate + "%";
            }

            Object[] row = {
                    currentMonth.format(DateTimeFormatter.ofPattern("MM/yyyy")),
                    formatCurrency(monthlyIncome),
                    formatCurrency(monthlyExpense),
                    formatCurrency(monthlyBalance),
                    savingsRate
            };
            tableModel.addRow(row);

            grandTotalIncome = grandTotalIncome.add(monthlyIncome);
            grandTotalExpense = grandTotalExpense.add(monthlyExpense);

            currentMonth = currentMonth.plusMonths(1);
        }

        // Add total row
        BigDecimal grandTotalBalance = grandTotalIncome.subtract(grandTotalExpense);
        String grandTotalSavingsRate = "0%";
        if (grandTotalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = grandTotalBalance.multiply(BigDecimal.valueOf(100))
                    .divide(grandTotalIncome, 1, RoundingMode.HALF_UP);
            grandTotalSavingsRate = rate + "%";
        }

        Object[] totalRow = {
                "<html><b>TỔNG 6 THÁNG</b></html>",
                "<html><b>" + formatCurrency(grandTotalIncome) + "</b></html>",
                "<html><b>" + formatCurrency(grandTotalExpense) + "</b></html>",
                "<html><b>" + formatCurrency(grandTotalBalance) + "</b></html>",
                "<html><b>" + grandTotalSavingsRate + "</b></html>"
        };
        tableModel.addRow(totalRow);
    }

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê tổng quan"));
        statsPanel.setPreferredSize(new Dimension(getWidth(), 100));

        // These will be updated in refreshCharts()
        JPanel avgIncomePanel = createStatCard("Thu nhập TB/tháng", "0 VND", new Color(46, 204, 113));
        JPanel avgExpensePanel = createStatCard("Chi tiêu TB/tháng", "0 VND", new Color(231, 76, 60));
        JPanel highestIncomePanel = createStatCard("Thu nhập cao nhất", "0 VND", new Color(52, 152, 219));
        JPanel highestExpensePanel = createStatCard("Chi tiêu cao nhất", "0 VND", new Color(155, 89, 182));

        statsPanel.add(avgIncomePanel);
        statsPanel.add(avgExpensePanel);
        statsPanel.add(highestIncomePanel);
        statsPanel.add(highestExpensePanel);

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.DARK_GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void setupEventListeners() {
        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCharts();
            }
        });

        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });

        reportTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchReportType();
            }
        });
    }

    private void switchReportType() {
        String selectedType = (String) reportTypeCombo.getSelectedItem();

        switch (selectedType) {
            case "Phân bổ chi tiêu":
                chartCardLayout.show(chartPanel, "TABLE");
                break;
            case "Thu chi theo tháng":
                chartCardLayout.show(chartPanel, "SUMMARY");
                break;
            case "Xu hướng thu chi":
                chartCardLayout.show(chartPanel, "TREND");
                break;
        }
    }

    public void refreshCharts() {
        // Update expense table
        JPanel tablePanel = (JPanel) chartPanel.getComponent(0);
        JScrollPane scrollPane = (JScrollPane) tablePanel.getComponent(1);
        JTable table = (JTable) scrollPane.getViewport().getView();
        updateExpenseTable((DefaultTableModel) table.getModel());

        // Update income/expense visualization
        JPanel summaryPanel = (JPanel) chartPanel.getComponent(1);
        JPanel visualizationPanel = (JPanel) summaryPanel.getComponent(1);
        updateIncomeExpenseVisualization(visualizationPanel);

        // Update trend table
        JPanel trendPanel = (JPanel) chartPanel.getComponent(2);
        JScrollPane trendScrollPane = (JScrollPane) trendPanel.getComponent(1);
        JTable trendTable = (JTable) trendScrollPane.getViewport().getView();
        updateTrendTable((DefaultTableModel) trendTable.getModel());

        // Update statistics
        updateStatistics();

        // Repaint
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void updateStatistics() {
        int userId = AuthController.getCurrentUser().getId();
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());

        // Calculate statistics for the last 6 months
        LocalDate endDate = LocalDate.of(selectedYear, selectedMonth, 1).plusMonths(1).minusDays(1);
        LocalDate startDate = endDate.minusMonths(6).withDayOfMonth(1);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal highestIncome = BigDecimal.ZERO;
        BigDecimal highestExpense = BigDecimal.ZERO;

        // Get transactions for the period
        List<Transaction> transactions =
                TransactionController.getTransactionsByUser(userId, startDate, endDate);

        for (Transaction t : transactions) {
            if (t.getType().equals("INCOME")) {
                totalIncome = totalIncome.add(t.getAmount());
                if (t.getAmount().compareTo(highestIncome) > 0) {
                    highestIncome = t.getAmount();
                }
            } else {
                totalExpense = totalExpense.add(t.getAmount());
                if (t.getAmount().compareTo(highestExpense) > 0) {
                    highestExpense = t.getAmount();
                }
            }
        }

        // Calculate averages (per month)
        BigDecimal avgIncome = totalIncome.divide(BigDecimal.valueOf(6), RoundingMode.HALF_UP);
        BigDecimal avgExpense = totalExpense.divide(BigDecimal.valueOf(6), RoundingMode.HALF_UP);

        // Update stat cards
        updateStatCard(0, formatCurrency(avgIncome));
        updateStatCard(1, formatCurrency(avgExpense));
        updateStatCard(2, formatCurrency(highestIncome));
        updateStatCard(3, formatCurrency(highestExpense));
    }

    private void updateStatCard(int index, String value) {
        JPanel statsPanel = (JPanel) getComponent(2);
        JPanel card = (JPanel) statsPanel.getComponent(index);
        JLabel valueLabel = (JLabel) card.getComponent(1);
        valueLabel.setText(value);
    }

    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Xuất báo cáo");
        fileChooser.setSelectedFile(new java.io.File("bao_cao_thu_chi.html"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                generateHTMLReport(file);
                JOptionPane.showMessageDialog(this,
                        "Xuất báo cáo thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất báo cáo: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void generateHTMLReport(java.io.File file) throws Exception {
        int userId = AuthController.getCurrentUser().getId();
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());

        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal totalIncome = TransactionController.getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpense = TransactionController.getTotalExpense(userId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);
        List<Object[]> expenseByCategory = TransactionController.getExpenseByCategory(
                userId, startDate, endDate);

        String monthName = monthCombo.getSelectedItem().toString();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("    <meta charset='UTF-8'>\n")
                .append("    <title>Báo cáo thu chi ").append(monthName).append(" ").append(selectedYear).append("</title>\n")
                .append("    <style>\n")
                .append("        body { font-family: Arial, sans-serif; margin: 40px; }\n")
                .append("        h1 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n")
                .append("        .summary { background: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 30px; }\n")
                .append("        .stat { display: inline-block; margin-right: 30px; }\n")
                .append("        .income { color: #2ecc71; font-weight: bold; }\n")
                .append("        .expense { color: #e74c3c; font-weight: bold; }\n")
                .append("        .balance { color: #3498db; font-weight: bold; }\n")
                .append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
                .append("        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n")
                .append("        th { background-color: #3498db; color: white; }\n")
                .append("        tr:nth-child(even) { background-color: #f2f2f2; }\n")
                .append("        .footer { margin-top: 50px; color: #7f8c8d; font-size: 12px; }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("    <h1>📊 Báo cáo thu chi cá nhân</h1>\n")
                .append("    <div class='summary'>\n")
                .append("        <h2>Tổng quan tháng ").append(monthName).append(" ").append(selectedYear).append("</h2>\n")
                .append("        <div class='stat'>\n")
                .append("            <span class='income'>Tổng thu nhập: ").append(formatCurrency(totalIncome)).append("</span>\n")
                .append("        </div>\n")
                .append("        <div class='stat'>\n")
                .append("            <span class='expense'>Tổng chi tiêu: ").append(formatCurrency(totalExpense)).append("</span>\n")
                .append("        </div>\n")
                .append("        <div class='stat'>\n")
                .append("            <span class='balance'>Số dư: ").append(formatCurrency(balance)).append("</span>\n")
                .append("        </div>\n")
                .append("    </div>\n")
                .append("    \n")
                .append("    <h2>📋 Phân bổ chi tiêu theo danh mục</h2>\n")
                .append("    <table>\n")
                .append("        <tr>\n")
                .append("            <th>STT</th>\n")
                .append("            <th>Danh mục</th>\n")
                .append("            <th>Số tiền</th>\n")
                .append("            <th>Tỷ lệ</th>\n")
                .append("        </tr>\n");

        BigDecimal totalExpenseForCategories = totalExpense.compareTo(BigDecimal.ZERO) > 0 ?
                totalExpense : BigDecimal.ONE;

        int counter = 1;
        for (Object[] row : expenseByCategory) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            BigDecimal percentage = amount.multiply(BigDecimal.valueOf(100))
                    .divide(totalExpenseForCategories, 1, RoundingMode.HALF_UP);

            html.append("        <tr>\n")
                    .append("            <td>").append(counter++).append("</td>\n")
                    .append("            <td>").append(category).append("</td>\n")
                    .append("            <td class='expense'>").append(formatCurrency(amount)).append("</td>\n")
                    .append("            <td>").append(percentage).append("%</td>\n")
                    .append("        </tr>\n");
        }

        html.append("    </table>\n")
                .append("    \n")
                .append("    <div class='footer'>\n")
                .append("        <p>Báo cáo được tạo tự động bởi ứng dụng Quản lý thu chi cá nhân</p>\n")
                .append("        <p>Ngày tạo: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>\n")
                .append("        <p>Người dùng: ").append(AuthController.getCurrentUser().getFullName()).append("</p>\n")
                .append("    </div>\n")
                .append("</body>\n")
                .append("</html>");

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write(html.toString());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d VND", amount.intValue());
    }
}