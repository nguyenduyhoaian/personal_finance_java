package main.view;

import main.controller.AuthController;
import main.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername, txtEmail, txtFullName;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister, btnCancel;
    private LoginFrame loginFrame;

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("Đăng ký tài khoản");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrame.enableLoginFrame();
            }
        });
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0, 102, 204));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Full name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Họ và tên:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtFullName = new JTextField(20);
        formPanel.add(txtFullName, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tên đăng nhập:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtUsername = new JTextField(20);
        formPanel.add(txtUsername, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        txtPassword = new JPasswordField(20);
        formPanel.add(txtPassword, gbc);

        // Confirm password
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        txtConfirmPassword = new JPasswordField(20);
        formPanel.add(txtConfirmPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegister = new JButton("Đăng ký");
        btnCancel = new JButton("Hủy");

        btnRegister.setBackground(new Color(0, 153, 76));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);

        btnCancel.setBackground(new Color(204, 0, 0));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                loginFrame.enableLoginFrame();
            }
        });

        add(mainPanel);
    }

    private void register() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        // Validation
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng điền đầy đủ thông tin!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Mật khẩu xác nhận không khớp!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Mật khẩu phải có ít nhất 6 ký tự!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (AuthController.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập đã tồn tại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create new user
        User newUser = new User(username, password, email, fullName);

        if (AuthController.register(newUser)) {
            JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công! Vui lòng đăng nhập.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();
            loginFrame.enableLoginFrame();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Đăng ký thất bại! Vui lòng thử lại.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}