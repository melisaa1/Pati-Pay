
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

    public static boolean checkPasswordByUsername(String username, String password) {
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT password FROM User WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String dbPass = rs.getString("password");
                return dbPass.equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updatePasswordByUsername(String username, String oldpassword, String newPassword) {
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE User SET password = ? WHERE username = ?")) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
