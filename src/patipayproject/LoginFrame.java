
package patipayproject;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame{
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton userLoginButton;
    private JButton adminLoginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("Patipay Giriş");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 300);
        setLocationRelativeTo(null);

        // Layout ayarlayalım (GridBagLayout öneririm daha esnek)
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(230,240,255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Kullanıcı Adı Label + Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Kullanıcı Adı:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // Şifre Label + Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // Kullanıcı Giriş Butonu
        gbc.gridx = 0;
        gbc.gridy = 2;
        userLoginButton = new JButton("Kullanıcı Girişi");
        panel.add(userLoginButton, gbc);

        // Yönetici Giriş Butonu
        gbc.gridx = 1;
        adminLoginButton = new JButton("Yönetici Girişi");
        panel.add(adminLoginButton, gbc);

        // Kayıt Ol Butonu - Yeni satırda, ortalanmış şekilde
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Kayıt Ol");
        panel.add(registerButton, gbc);

        add(panel);

        // ActionListener ekleyelim:
        userLoginButton.addActionListener(e -> handleUserLogin(e));
        userLoginButton.setBackground(new Color(100,149,237));
        adminLoginButton.addActionListener(e -> handleAdminLogin(e));
        adminLoginButton.setBackground(new Color(60,179,113));
        registerButton.addActionListener(e -> handleRegister(e));
        registerButton.setBackground(new Color(255,165,00));
        
       

        setVisible(true);
    }
    
     private void temizle(){
         usernameField.setText("");
         passwordField.setText("");

}

private void handleUserLogin(ActionEvent e) {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    // admin kullanıcı adıyla normal girişe izin verme:
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
    temizle(); // her durumda temizle
}

private void handleAdminLogin(ActionEvent e) {
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

private void handleRegister(ActionEvent e) {
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