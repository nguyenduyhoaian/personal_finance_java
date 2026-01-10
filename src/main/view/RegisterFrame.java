package main.view;

import main.controller.AuthController;
import main.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername, txtEmail, txtFullName;
    private JPasswordField txtPassword, txtConfirmPassword;
    private final LoginFrame loginFrame;

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("Đăng ký tài khoản");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();

        // Sự kiện: Khi bấm nút X trên cửa sổ -> Quay lại Login
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrame.enableLoginFrame();
            }
        });
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN MỚI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(39, 174, 96)); // Màu xanh lá
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Helper để thêm component nhanh
        int row = 0;
        addFormField(formPanel, gbc, "Họ và tên:", txtFullName = new JTextField(20), row++);
        addFormField(formPanel, gbc, "Tên đăng nhập:", txtUsername = new JTextField(20), row++);
        addFormField(formPanel, gbc, "Email:", txtEmail = new JTextField(20), row++);
        addFormField(formPanel, gbc, "Mật khẩu:", txtPassword = new JPasswordField(20), row++);
        addFormField(formPanel, gbc, "Xác nhận mật khẩu:", txtConfirmPassword = new JPasswordField(20), row++);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnRegister = createButton("Đăng ký ngay", new Color(39, 174, 96));
        JButton btnCancel = createButton("Hủy bỏ", new Color(192, 57, 43));

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Event Handlers ---
        btnRegister.addActionListener(e -> register());

        btnCancel.addActionListener(e -> {
            dispose();
            loginFrame.enableLoginFrame();
        });

        // UX: Nhấn Enter tại ô Confirm Password sẽ kích hoạt Đăng ký luôn
        txtConfirmPassword.addActionListener(e -> register());

        add(mainPanel);
    }

    // Hàm phụ trợ để thêm trường nhập liệu vào GridBagLayout cho gọn code
    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, Component field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        //btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(130, 35));
        return btn;
    }

    private void register() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        // Validate
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (AuthController.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Thực hiện đăng ký
        User newUser = new User(username, password, email, fullName);

        if (AuthController.register(newUser)) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            loginFrame.enableLoginFrame();

            // Tự động điền username vừa tạo vào form login
            loginFrame.setUsernameFromRegister(username);

        } else {
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại! Vui lòng thử lại.", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }
}