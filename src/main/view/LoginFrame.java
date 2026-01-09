package main.view;

import main.controller.AuthController;
import main.model.User;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister, btnQuit;

    public LoginFrame() {
        setTitle("Đăng nhập - Quản lý thu chi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 320); // Tăng chiều cao xíu cho thoáng
        setLocationRelativeTo(null);
        setResizable(false);

        // Icon ứng dụng (nếu có sau này)
        // setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(Color.WHITE); // Nền trắng cho sạch sẽ

        // Header
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(41, 128, 185)); // Màu xanh chủ đạo
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); // Tăng khoảng cách giữa các dòng
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên đăng nhập:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtUsername = new JTextField(20);
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtPassword = new JPasswordField(20);
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnLogin = createButton("Đăng nhập", new Color(46, 204, 113));
        btnRegister = createButton("Đăng ký", new Color(52, 152, 219));
        btnQuit = createButton("Thoát", new Color(231, 76, 60));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnQuit);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Xử lý sự kiện (Event Handlers) ---

        // Nhấn nút Đăng nhập
        btnLogin.addActionListener(e -> login());

        // Mở form Đăng ký
        btnRegister.addActionListener(e -> openRegisterFrame());

        // Thoát
        btnQuit.addActionListener(e -> System.exit(0));

        // UX: Nhấn Enter ở ô Password hoặc Username đều kích hoạt đăng nhập
        txtPassword.addActionListener(e -> login());
        txtUsername.addActionListener(e -> login());

        add(mainPanel);
    }

    // Hàm tạo nút chung để đồng bộ giao diện
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        //btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(100, 35));
        return btn;
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = AuthController.login(username, password);
        if (user != null) {
            // Đăng nhập thành công -> Mở MainFrame
            new MainFrame().setVisible(true);
            this.dispose(); // Đóng hẳn LoginFrame
        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterFrame() {
        new RegisterFrame(this).setVisible(true);
        this.setVisible(false);
    }

    public void enableLoginFrame() {
        this.setVisible(true);
        // Xóa trắng mật khẩu khi quay lại để bảo mật
        txtPassword.setText("");
    }
}