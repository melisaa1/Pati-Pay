
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
        setSize(300, 250);
        setLocationRelativeTo(null);

        // Layout ayarlayalım (GridBagLayout öneririm daha esnek)
        JPanel panel = new JPanel(new GridBagLayout());
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
        userLoginButton.addActionListener(this::handleUserLogin);
        adminLoginButton.addActionListener(this::handleAdminLogin);
        registerButton.addActionListener(this::handleRegister);

        setVisible(true);
    }

   private void handleUserLogin(ActionEvent e) {
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());

    int userId = UserService.login(username, password);
    if (userId != -1) {
        JOptionPane.showMessageDialog(this, "✅ Kullanıcı girişi başarılı!");
        new UserPanel(userId);  // UserPanel kullanıcı ID'si ile açılır
        dispose();
    } else {
        JOptionPane.showMessageDialog(this, "❌ Kullanıcı bilgileri hatalı!");
    }
}


    private void handleAdminLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (AdminService.login(username, password)) {
            JOptionPane.showMessageDialog(this, "✅ Yönetici girişi başarılı!");
            new AdminPanel();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Yönetici bilgileri hatalı!");
        }
    }

  private void handleRegister(ActionEvent e) {
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());

    if (UserService.register(username, password)) {
        JOptionPane.showMessageDialog(this, "✅ Kayıt başarılı!");
    } else {
        JOptionPane.showMessageDialog(this, "❌ Bu kullanıcı adı zaten var.");
    }

}
}