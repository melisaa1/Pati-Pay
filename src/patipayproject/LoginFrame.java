
package patipayproject;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton userLoginButton;
    private JButton adminLoginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("PatiPay Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 400); // Biraz daha yükseklik verildi checkbox için
        setLocationRelativeTo(null);

        // ✅ İç sınıf: Arka plan paneli
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

        // ✅ Arka plan paneli
        BackgroundPanel mainPanel = new BackgroundPanel("src/assets/background.png");
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // ✅ Üst panel (logo + başlık)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255, 255, 255, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon logoIcon = new ImageIcon("src/assets/paw_logo.png");
        Image scaledImage = logoIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));

        JLabel titleLabel = new JLabel("PatiPay Application");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ✅ Orta form paneli
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

        // Şifre
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("🔒 Şifre:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        // ✅ Şifre Göster Checkbox
        gbc.gridx = 1; gbc.gridy = 2;
        JCheckBox showPassword = new JCheckBox("👀");
        showPassword.setOpaque(false);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        showPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(showPassword, gbc);

        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0); // Göster
            } else {
                passwordField.setEchoChar('•'); // Gizle
            }
        });

        // Kullanıcı Girişi
        gbc.gridx = 0; gbc.gridy = 3;
        userLoginButton = new JButton("Kullanıcı Girişi");
        styleButton(userLoginButton, new Color(0, 120, 215));
        formPanel.add(userLoginButton, gbc);

        // Yönetici Girişi
        gbc.gridx = 1;
        adminLoginButton = new JButton("Yönetici Girişi");
        styleButton(adminLoginButton, new Color(0, 153, 102));
        formPanel.add(adminLoginButton, gbc);

        // Kayıt Ol Butonu
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Kayıt Ol");
        styleButton(registerButton, new Color(255, 102, 0));
        formPanel.add(registerButton, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ✅ Butonların olayları
        userLoginButton.addActionListener(e -> handleUserLogin());
        adminLoginButton.addActionListener(e -> handleAdminLogin());
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
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(borderColor);
                button.setForeground(Color.WHITE);
            }

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

    private void handleUserLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (isAdminUsername(username)) {
            JOptionPane.showMessageDialog(this, "⚠️ Bu kullanıcı adı yönetici içindir. Lütfen 'Yönetici Girişi'ni kullanın.");
            temizle();
            return;
        }

        int userId = UserService.login(username, password);
        if (userId != -1) {
            JOptionPane.showMessageDialog(this, "✅ Kullanıcı girişi başarılı!");
            new UserPanel(userId);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Kullanıcı bilgileri hatalı!");
        }
        temizle();
    }

    private void handleAdminLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!isAdminUsername(username)) {
            JOptionPane.showMessageDialog(this, "⚠️ Bu bölüm yalnızca yöneticiler içindir.");
            temizle();
            return;
        }

        if (AdminService.login(username, password)) {
            JOptionPane.showMessageDialog(this, "✅ Yönetici girişi başarılı!");
            new AdminPanel();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Yönetici bilgileri hatalı!");
        }
        temizle();
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (isAdminUsername(username)) {
            JOptionPane.showMessageDialog(this, "⚠️ Bu yönetici kullanıcı adıdır. Yönetici kayıt olamaz; yalnızca 'Yönetici Girişi' yapılabilir.");
            temizle();
            return;
        }
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen kullanıcı adı ve şifre girin.");
            return;
        }

        if (UserService.register(username, password)) {
            JOptionPane.showMessageDialog(this, "✅ Kayıt başarılı!");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Bu kullanıcı adı zaten var.");
        }
        temizle();
    }

    private boolean isAdminUsername(String username) {
        return "admin".equalsIgnoreCase(username);
    }
}
