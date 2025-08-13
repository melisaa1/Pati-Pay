
package patipayproject;

import java.sql.*;

public class AdminService {
    
     private String username;

        public static boolean login(String username, String password) {
        return username.equals("admin") && password.equals("admin123");
    }
    public AdminService(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
 
  public static boolean checkPassword(int userId, String password) {
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT password FROM User WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String dbPass = rs.getString("password");
                return dbPass.equals(password); // basit string karşılaştırma, hash kullanabilirsin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kullanıcının şifresini günceller
    public static boolean updatePassword(int userId, String newPassword) {
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE User SET password = ? WHERE id = ?")) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
