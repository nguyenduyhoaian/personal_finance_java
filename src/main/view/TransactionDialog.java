package main.view;

import main.controller.AuthController;
import main.controller.TransactionController;
import main.model.Category;
import main.model.Transaction;
// import main.view.components.TransactionPanel; // Xóa dòng này để tránh phụ thuộc ngược
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionDialog extends JDialog {
    private JTextField txtAmount, txtDescription;
    private JComboBox<String> cmbType, cmbCategory;
    private JFormattedTextField txtDate;
    private JButton btnSave, btnCancel, btnAddCategory;
    private boolean success = false;
    private Transaction transaction;

    // Khởi tạo Controller để sử dụng các hàm xử lý dữ liệu
    private TransactionController controller = new TransactionController();

    public TransactionDialog(JFrame parent, Transaction transaction, String title) {
        super(parent, title, true);
        this.transaction = transaction;

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();

        // Load data nếu là chế độ Edit
        if (transaction != null) {
            loadTransactionData();
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Transaction type
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Loại giao dịch:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        String[] types = {"Thu nhập", "Chi tiêu"};
        cmbType = new JComboBox<>(types);
        cmbType.addActionListener(e -> updateCategories());
        formPanel.add(cmbType, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Danh mục:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
        cmbCategory = new JComboBox<>();
        categoryPanel.add(cmbCategory, BorderLayout.CENTER);

        btnAddCategory = new JButton("+");
        btnAddCategory.setPreferredSize(new Dimension(40, 25));
        btnAddCategory.setToolTipText("Thêm danh mục mới");
        btnAddCategory.addActionListener(e -> addNewCategory());
        categoryPanel.add(btnAddCategory, BorderLayout.EAST);

        formPanel.add(categoryPanel, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Số tiền (VND):"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        txtAmount = new JTextField();
        formPanel.add(txtAmount, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Ngày:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        txtDate = new JFormattedTextField(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        txtDate.setValue(LocalDate.now());
        formPanel.add(txtDate, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        txtDescription = new JTextField();
        formPanel.add(txtDescription, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        btnSave.setBackground(new Color(46, 204, 113)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        btnCancel.setBackground(new Color(231, 76, 60)); // Red
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        btnSave.addActionListener(e -> saveTransaction());
        btnCancel.addActionListener(e -> dispose());
        txtAmount.addActionListener(e -> saveTransaction());

        add(mainPanel);

        // Load categories for default type
        updateCategories();
    }

    private void updateCategories() {
        // Lấy loại category (INCOME/EXPENSE)
        String type = cmbType.getSelectedItem().equals("Thu nhập") ? "INCOME" : "EXPENSE";

        // GỌI CONTROLLER ĐỂ LẤY DỮ LIỆU (Thay vì gọi TransactionPanel)
        // Lưu ý: controller.getCategories() hiện đang lấy all, bạn có thể cần lọc theo type
        // ở phía client hoặc viết thêm hàm getCategoriesByType trong Controller.
        // Tạm thời lọc ở đây:
        List<Category> allCategories = controller.getCategories();

        cmbCategory.removeAllItems();
        for (Category category : allCategories) {
            if (category.getType().equals(type)) {
                cmbCategory.addItem(category.getName());
            }
        }

        if (cmbCategory.getItemCount() > 0) {
            cmbCategory.setSelectedIndex(0);
        }
    }

    private void addNewCategory() {
        String type = cmbType.getSelectedItem().equals("Thu nhập") ? "INCOME" : "EXPENSE";

        String categoryName = JOptionPane.showInputDialog(this,
                "Nhập tên danh mục mới:",
                "Thêm danh mục",
                JOptionPane.QUESTION_MESSAGE);

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            // GỌI CONTROLLER ĐỂ LƯU CATEGORY
            if (controller.addCategory(categoryName.trim(), type)) {
                updateCategories();
                cmbCategory.setSelectedItem(categoryName.trim());
                JOptionPane.showMessageDialog(this,
                        "Thêm danh mục thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Thêm danh mục thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadTransactionData() {
        if (transaction != null) {
            cmbType.setSelectedItem(transaction.getType().equals("INCOME") ? "Thu nhập" : "Chi tiêu");
            updateCategories();

            SwingUtilities.invokeLater(() -> {
                String type = transaction.getType().equals("INCOME") ? "INCOME" : "EXPENSE";
                // Dùng Controller
                List<Category> categories = controller.getCategories();

                for (Category cat : categories) {
                    if (cat.getId() == transaction.getCategoryId()) {
                        cmbCategory.setSelectedItem(cat.getName());
                        break;
                    }
                }

                txtAmount.setText(transaction.getAmount().toString());
                txtDate.setValue(transaction.getTransactionDate());
                txtDescription.setText(transaction.getDescription());
            });
        }
    }

    private void saveTransaction() {
        try {
            if (cmbCategory.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String amountStr = txtAmount.getText().trim();
            if (amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(txtDate.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                date = LocalDate.now();
            }

            String type = cmbType.getSelectedItem().equals("Thu nhập") ? "INCOME" : "EXPENSE";
            String categoryName = (String) cmbCategory.getSelectedItem();

            // Tìm ID của category đã chọn
            List<Category> categories = controller.getCategories();
            int categoryId = -1;
            for (Category cat : categories) {
                if (cat.getName().equals(categoryName) && cat.getType().equals(type)) {
                    categoryId = cat.getId();
                    break;
                }
            }

            if (categoryId == -1) {
                JOptionPane.showMessageDialog(this, "Danh mục không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userId = AuthController.getCurrentUser().getId();
            String description = txtDescription.getText().trim();

            if (transaction == null) {
                // Add new transaction
                Transaction newTransaction = new Transaction(
                        userId, categoryId, amount, description, date, type
                );

                // GỌI CONTROLLER
                if (controller.addTransaction(newTransaction)) {
                    success = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm giao dịch thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Update transaction
                transaction.setCategoryId(categoryId);
                transaction.setAmount(amount);
                transaction.setDescription(description);
                transaction.setTransactionDate(date);
                transaction.setType(type);

                // GỌI CONTROLLER
                if (controller.updateTransaction(transaction)) {
                    success = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Sửa giao dịch thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}