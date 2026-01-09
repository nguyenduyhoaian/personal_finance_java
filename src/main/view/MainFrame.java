package main.view;

import main.controller.AuthController;
import main.view.components.DashboardPanel;
import main.view.components.TransactionPanel;
import main.view.components.ReportPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private JPanel mainContentPanel;
    private JButton btnDashboard, btnTransactions, btnReports, btnLogout;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("Quản lý thu chi cá nhân - " + AuthController.getCurrentUser().getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Create main layout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Sidebar
        JPanel sidebarPanel = createSidebarPanel();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Main content area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Add different panels
        mainContentPanel.add(new DashboardPanel(), "DASHBOARD");
        mainContentPanel.add(new TransactionPanel(), "TRANSACTIONS");
        mainContentPanel.add(new ReportPanel(), "REPORTS");

        mainPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        // App title
        JLabel lblTitle = new JLabel("QUẢN LÝ THU CHI CÁ NHÂN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        // User info
        String welcomeText = "Xin chào, " + AuthController.getCurrentUser().getFullName();
        JLabel lblUser = new JLabel(welcomeText);
        lblUser.setForeground(Color.WHITE);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(240, 240, 240));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));

        // Create buttons
        btnDashboard = createMenuButton("📊 Dashboard");
        btnTransactions = createMenuButton("💰 Giao dịch");
        btnReports = createMenuButton("📈 Báo cáo");
        btnLogout = createMenuButton("🚪 Đăng xuất");

        btnDashboard.setBackground(new Color(0, 102, 204));
        btnDashboard.setForeground(Color.WHITE);

        // Add buttons to sidebar
        sidebarPanel.add(btnDashboard);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnTransactions);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnReports);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);

        // Add event listeners
        btnDashboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtonColors();
                btnDashboard.setBackground(new Color(0, 102, 204));
                btnDashboard.setForeground(Color.WHITE);
                cardLayout.show(mainContentPanel, "DASHBOARD");
                ((DashboardPanel) mainContentPanel.getComponent(0)).refreshData();
            }
        });

        btnTransactions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtonColors();
                btnTransactions.setBackground(new Color(0, 102, 204));
                btnTransactions.setForeground(Color.WHITE);
                cardLayout.show(mainContentPanel, "TRANSACTIONS");
                ((TransactionPanel) mainContentPanel.getComponent(1)).refreshTable();
            }
        });

        btnReports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtonColors();
                btnReports.setBackground(new Color(0, 102, 204));
                btnReports.setForeground(Color.WHITE);
                cardLayout.show(mainContentPanel, "REPORTS");
                ((ReportPanel) mainContentPanel.getComponent(2)).refreshCharts();
            }
        });

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Bạn có chắc chắn muốn đăng xuất?",
                        "Xác nhận đăng xuất",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    AuthController.setCurrentUser(null);
                    new LoginFrame().setVisible(true);
                    dispose();
                }
            }
        });

        return sidebarPanel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setBackground(Color.WHITE);

        return button;
    }

    private void resetButtonColors() {
        btnDashboard.setBackground(Color.WHITE);
        btnDashboard.setForeground(Color.BLACK);
        btnTransactions.setBackground(Color.WHITE);
        btnTransactions.setForeground(Color.BLACK);
        btnReports.setBackground(Color.WHITE);
        btnReports.setForeground(Color.BLACK);
    }
}