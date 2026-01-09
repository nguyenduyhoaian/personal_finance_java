package main.view.components;

import main.controller.AuthController;
import main.controller.TransactionController;
import main.personalfinance.model.Category;
import main.personalfinance.model.Transaction;
import main.view.TransactionDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionPanel extends JPanel {
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JComboBox<String> filterType, filterMonth, filterYear;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public TransactionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Quản lý giao dịch");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnAdd = createButton("Thêm mới", new Color(46, 204, 113));
        btnEdit = createButton("Sửa", new Color(52, 152, 219));
        btnDelete = createButton("Xóa", new Color(231, 76, 60));
        btnRefresh = createButton("Làm mới", new Color(149, 165, 166));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.CENTER);

        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.SOUTH);

        // Add action listeners
        setupEventListeners();
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Arial", Font.PLAIN, 12));

        return button;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc"));

        // Search field
        filterPanel.add(new JLabel("Tìm kiếm:"));
        searchField = new JTextField(20);
        filterPanel.add(searchField);

        // Type filter
        filterPanel.add(new JLabel("Loại:"));
        String[] types = {"Tất cả", "Thu nhập", "Chi tiêu"};
        filterType = new JComboBox<>(types);
        filterPanel.add(filterType);

        // Month filter
        filterPanel.add(new JLabel("Tháng:"));
        String[] months = {"Tất cả", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        filterMonth = new JComboBox<>(months);
        filterPanel.add(filterMonth);

        // Year filter
        filterPanel.add(new JLabel("Năm:"));
        String[] years = new String[6];
        int currentYear = LocalDate.now().getYear();
        years[0] = "Tất cả";
        for (int i = 1; i < 6; i++) {
            years[i] = String.valueOf(currentYear - 5 + i);
        }
        filterYear = new JComboBox<>(years);
        filterYear.setSelectedItem(String.valueOf(currentYear));
        filterPanel.add(filterYear);

        // Apply filter button
        JButton btnApplyFilter = createButton("Áp dụng", new Color(52, 152, 219));
        btnApplyFilter.addActionListener(e -> applyFilters());
        filterPanel.add(btnApplyFilter);

        return filterPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create table model
        String[] columns = {"ID", "Ngày", "Danh mục", "Mô tả", "Số tiền", "Loại"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return BigDecimal.class;
                return String.class;
            }
        };

        transactionsTable = new JTable(tableModel);
        transactionsTable.setRowHeight(30);
        transactionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        transactionsTable.getColumnModel().getColumn(0).setWidth(0);

        // Style table header
        JTableHeader header = transactionsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(new Color(52, 152, 219));
        header.setForeground(Color.WHITE);

        // Enable sorting
        sorter = new TableRowSorter<>(tableModel);
        transactionsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        // Summary panel
        JPanel summaryPanel = createSummaryPanel();

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(summaryPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Summary labels will be updated in refreshTable()
        return summaryPanel;
    }

    private void setupEventListeners() {
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTransaction();
            }
        });

        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTransaction();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTransaction();
            }
        });

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        // Double-click to edit
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editTransaction();
                }
            }
        });
    }

    private void addTransaction() {
        TransactionDialog dialog = new TransactionDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, "Thêm giao dịch mới");
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            refreshTable();
            JOptionPane.showMessageDialog(this,
                    "Thêm giao dịch thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một giao dịch để sửa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
        int transactionId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

        // Get transaction details from database
        Transaction transaction = getTransactionById(transactionId);
        if (transaction != null) {
            TransactionDialog dialog = new TransactionDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    transaction, "Sửa giao dịch");
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                refreshTable();
                JOptionPane.showMessageDialog(this,
                        "Sửa giao dịch thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void deleteTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một giao dịch để xóa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa giao dịch này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
            int transactionId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
            int userId = AuthController.getCurrentUser().getId();

            if (TransactionController.deleteTransaction(transactionId, userId)) {
                refreshTable();
                JOptionPane.showMessageDialog(this,
                        "Xóa giao dịch thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Xóa giao dịch thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Transaction getTransactionById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/finance.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("id"));
                transaction.setUserId(rs.getInt("user_id"));
                transaction.setCategoryId(rs.getInt("category_id"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                transaction.setType(rs.getString("type"));
                return transaction;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String typeFilter = (String) filterType.getSelectedItem();
        int monthFilter = filterMonth.getSelectedIndex(); // 0 = Tất cả
        String yearFilter = (String) filterYear.getSelectedItem();

        // Create row filter
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Search filter (columns: date, category, description, amount, type)
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 1, 2, 3, 4));
        }

        // Type filter
        if (!typeFilter.equals("Tất cả")) {
            String typeValue = typeFilter.equals("Thu nhập") ? "INCOME" : "EXPENSE";
            filters.add(RowFilter.regexFilter("(?i)" + typeValue, 5));
        }

        // Date filter
        if (monthFilter != 0 || !yearFilter.equals("Tất cả")) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    String dateStr = (String) entry.getValue(1);
                    LocalDate date = LocalDate.parse(dateStr);

                    if (monthFilter != 0 && date.getMonthValue() != monthFilter) {
                        return false;
                    }

                    if (!yearFilter.equals("Tất cả") && date.getYear() != Integer.parseInt(yearFilter)) {
                        return false;
                    }

                    return true;
                }
            });
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    public void refreshTable() {
        // Clear table
        tableModel.setRowCount(0);

        // Get current user ID
        int userId = AuthController.getCurrentUser().getId();

        // Get all transactions for current user
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusYears(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1);

        List<Transaction> transactions = TransactionController.getTransactionsByUser(
                userId, startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        // Add rows to table
        for (Transaction transaction : transactions) {
            String typeText = transaction.getType().equals("INCOME") ? "Thu nhập" : "Chi tiêu";
            String amountFormatted = String.format("%,d VND", transaction.getAmount().intValue());

            Object[] row = {
                    transaction.getId(),
                    transaction.getTransactionDate().format(formatter),
                    transaction.getCategoryName(),
                    transaction.getDescription() != null ? transaction.getDescription() : "",
                    amountFormatted,
                    typeText
            };
            tableModel.addRow(row);

            // Calculate totals
            if (transaction.getType().equals("INCOME")) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else {
                totalExpense = totalExpense.add(transaction.getAmount());
            }
        }

        // Update summary
        updateSummary(totalIncome, totalExpense);
    }

    private void updateSummary(BigDecimal totalIncome, BigDecimal totalExpense) {
        JPanel summaryPanel = (JPanel) ((JPanel) getComponent(2)).getComponent(1);
        summaryPanel.removeAll();

        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Create summary labels
        JLabel lblIncome = new JLabel("Tổng thu: " + formatCurrency(totalIncome));
        lblIncome.setForeground(new Color(46, 204, 113));
        lblIncome.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblExpense = new JLabel("Tổng chi: " + formatCurrency(totalExpense));
        lblExpense.setForeground(new Color(231, 76, 60));
        lblExpense.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblBalance = new JLabel("Số dư: " + formatCurrency(balance));
        lblBalance.setForeground(new Color(52, 152, 219));
        lblBalance.setFont(new Font("Arial", Font.BOLD, 12));

        summaryPanel.add(lblIncome);
        summaryPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryPanel.add(lblExpense);
        summaryPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        summaryPanel.add(lblBalance);

        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d VND", amount.intValue());
    }

    // Helper method to get categories for TransactionDialog
    public static List<Category> getCategories(String type) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type = ? AND (user_id = ? OR user_id IS NULL)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/finance.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
            pstmt.setInt(2, AuthController.getCurrentUser().getId());

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getString("type"));
                categories.add(category);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    // Helper method to add new category
    public static boolean addCategory(String name, String type) {
        String sql = "INSERT INTO categories (name, type, user_id) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/finance.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setInt(3, AuthController.getCurrentUser().getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}