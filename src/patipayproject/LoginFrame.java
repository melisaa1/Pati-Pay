
package patipayproject;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("PatiPay Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 400);
        setLocationRelativeTo(null);

        // İç sınıf: Arka plan paneli
        class BackgroundPanel extends JPanel {
            private Image backgroundImage;
            public BackgroundPanel(String imagePath) {
                backgroundImage = new ImageIcon(imagePath).getImage();
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        }

        BackgroundPanel mainPanel = new BackgroundPanel("src/assets/background.png");
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // Üst başlık paneli
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255, 255, 255, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon logoIcon = new ImageIcon("src/assets/paw_logo.png");
        Image scaledImage = logoIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        JLabel logoLabel2 = new JLabel(new ImageIcon(scaledImage));

        JLabel titleLabel = new JLabel("PatiPay Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(logoLabel2, BorderLayout.EAST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255, 180));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Kullanıcı Adı
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("👤 Kullanıcı Adı:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        // Şifre + göz ikonu
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("🔒 Şifre:"), gbc);
        gbc.gridx = 1;
        JLayeredPane passwordLayer = new JLayeredPane();
        passwordLayer.setPreferredSize(new Dimension(200, 30));
        passwordField = new JPasswordField(15);
        passwordField.setBounds(0, 0, 200, 30);

        JLabel eyeLabel = new JLabel("👁️");
        eyeLabel.setBounds(175, 5, 20, 20);
        eyeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeLabel.addMouseListener(new MouseAdapter() {
            private boolean isVisible = false;
            @Override
            public void mouseClicked(MouseEvent e) {
                isVisible = !isVisible;
                passwordField.setEchoChar(isVisible ? (char) 0 : '•');
            }
        });

        passwordLayer.add(passwordField, Integer.valueOf(1));
        passwordLayer.add(eyeLabel, Integer.valueOf(2));
        formPanel.add(passwordLayer, gbc);

        // Buton boyutu
        Dimension buttonSize = new Dimension(200, 40);

        // Giriş Butonu
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Giriş Yap");
        styleButton(loginButton, new Color(0, 120, 215));
        loginButton.setPreferredSize(buttonSize);
        formPanel.add(loginButton, gbc);

        // Kayıt Ol Butonu
        gbc.gridy = 3;
        registerButton = new JButton("Kayıt Ol");
        styleButton(registerButton, new Color(0, 120, 215));
        registerButton.setPreferredSize(buttonSize);
        formPanel.add(registerButton, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buton olayları
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());

        setVisible(true);
    }

    private void styleButton(JButton button, Color borderColor) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(borderColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(borderColor);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(borderColor);
            }
        });
    }

    private void temizle() {
        usernameField.setText("");
        passwordField.setText("");
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen kullanıcı adı ve şifre girin.");
            return;
        }

        String role = UserService.login(username, password);

        if (role == null) {
            JOptionPane.showMessageDialog(this, "❌ Geçersiz kullanıcı adı veya şifre.");
            temizle();
            return;
        }

        JOptionPane.showMessageDialog(this, "✅ Giriş başarılı!");

        if (role.equals("admin")) {
            new AdminPanel().setVisible(true);
        } else {
            int userId = UserService.getUserId(username);
            new UserPanel(userId).setVisible(true);
        }

        dispose();
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen kullanıcı adı ve şifre girin.");
            return;
        }

        // ❗ Şifre sadece rakam olmalı
        if (!password.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "❌ Şifre sadece rakamlardan oluşmalıdır.");
            temizle();
            return;
        }

        // Rol seçimi
        String[] roles = {"user", "admin"};
        String role = (String) JOptionPane.showInputDialog(
                this,
                "Kullanıcı rolünü seçin:",
                "Rol Seçimi",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[0]
        );

        if (role == null) return;

        boolean success = UserService.register(username, password, role);

        if (success) {
            JOptionPane.showMessageDialog(this, "✅ Kayıt başarılı!");
            if (role.equals("admin")) {
                new AdminPanel().setVisible(true);
            } else {
                int userId = UserService.getUserId(username);
                new UserPanel(userId).setVisible(true);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Bu kullanıcı adı zaten var.");
        }

        temizle();
    }
}
