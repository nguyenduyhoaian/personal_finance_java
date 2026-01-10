package main.view.components;

import main.controller.AuthController;
import main.controller.TransactionController;
import main.model.Transaction;
import main.view.TransactionDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TransactionPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TransactionController controller;
    private List<Transaction> currentList; // Lưu danh sách hiện tại để lấy ID khi sửa/xóa

    public TransactionPanel() {
        controller = new TransactionController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        initComponents();
        refreshTable(); // Load dữ liệu lần đầu
    }

    private void initComponents() {
        // 1. Toolbar (Nút Thêm, Sửa, Xóa)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Thêm mới", new Color(46, 204, 113));
        JButton btnEdit = createButton("Sửa", new Color(52, 152, 219));
        JButton btnDelete = createButton("Xóa", new Color(231, 76, 60));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);

        add(toolbar, BorderLayout.NORTH);

        // 2. Bảng dữ liệu (Table)
        String[] columnNames = {"ID", "Ngày", "Danh mục", "Loại", "Số tiền", "Mô tả"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // Ẩn cột ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Format cột số tiền sang bên phải
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- Xử lý sự kiện ---

        // Nút Thêm
        btnAdd.addActionListener(e -> {
            TransactionDialog dialog = new TransactionDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, "Thêm giao dịch mới");
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                refreshTable();
            }
        });

        // Nút Sửa
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Transaction t = currentList.get(selectedRow); // Lấy object từ list dựa trên index
                TransactionDialog dialog = new TransactionDialog((JFrame) SwingUtilities.getWindowAncestor(this), t, "Sửa giao dịch");
                dialog.setVisible(true);
                if (dialog.isSuccess()) {
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn giao dịch cần sửa!");
            }
        });

        // Nút Xóa
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa giao dịch này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int id = (int) table.getValueAt(selectedRow, 0);
                    if (controller.deleteTransaction(id)) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn giao dịch cần xóa!");
            }
        });
    }

    // Hàm được gọi từ MainFrame khi chuyển tab
    public void refreshTable() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        int userId = AuthController.getCurrentUser().getId();
        currentList = controller.getAllTransactions(userId);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (Transaction t : currentList) {
            Object[] row = {
                    t.getId(),
                    t.getTransactionDate().format(dateFormatter),
                    t.getCategoryName(),
                    t.getType().equals("INCOME") ? "Thu nhập" : "Chi tiêu",
                    currencyFormatter.format(t.getAmount()),
                    t.getDescription()
            };
            tableModel.addRow(row);
        }
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        //btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}