
package patipayproject;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static donationDAO donationDao = new donationDAO();

    // Giriş yap
    public static String login(String username, String password) {
        String sql = "SELECT role FROM User WHERE username = ? AND password = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            System.out.println("❌ Giriş hatası: " + e.getMessage());
        }
        return null;
    }

    // Kayıt ol
    public static boolean register(String username, String password, String role, String email) {
        String sqlCheck = "SELECT id FROM User WHERE username = ?";
        String sqlInsert = "INSERT INTO User(username, password, role, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {

            checkStmt.setString(1, username.trim());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("❌ Bu kullanıcı adı zaten mevcut.");
                return false;
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setString(1, username.trim());
                    insertStmt.setString(2, password);
                    insertStmt.setString(3, role);
                    insertStmt.setString(4, email);
                    insertStmt.executeUpdate();
                    System.out.println("✅ Kayıt başarılı!");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Kayıt hatası: " + e.getMessage());
            return false;
        }
    }

    // Bağış yap
    public static boolean makeDonation(int userId, String type, LocalDate date, double amount, String unit) {
        String sql = "INSERT INTO Donation(user_id, type, date, amount, unit) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, date.toString());
            ps.setDouble(4, amount);
            ps.setString(5, unit);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ Bağış kaydında hata: " + e.getMessage());
            return false;
        }
    }

    // Kullanıcının tüm bağışlarını getir
    public static List<Donation> getDonationsByUserId(int userId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM Donation WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DonationType donationType = DonationFactory.createType(rs.getString("type"));
                donations.add(new Donation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        donationType,
                        LocalDate.parse(rs.getString("date")),
                        rs.getDouble("amount"),
                        rs.getString("unit")
                ));
            }

        } catch (SQLException e) {
            System.out.println("❌ Bağışları alırken hata: " + e.getMessage());
        }
        return donations;
    }

    // Kullanıcının toplam puanını hesapla
    public static double getTotalScoreByUserId(int userId) {
        double totalScore = 0;
        String sql = "SELECT type, amount FROM Donation WHERE user_id = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type").toLowerCase();
                double amount = rs.getDouble("amount");

                switch (type) {
                    case "para" -> totalScore += amount;
                    case "mama" -> totalScore += amount * 0.8;
                    case "su"   -> totalScore += amount * 0.6;
                    default     -> totalScore += amount;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Toplam puan alınamadı: " + e.getMessage());
        }
        return totalScore;
    }

    // Kullanıcı adını ID'ye göre getir
    public static String getUsernameById(int userId) {
        String sql = "SELECT username FROM User WHERE id = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (SQLException e) {
            System.out.println("❌ Kullanıcı adı alınamadı: " + e.getMessage());
        }
        return "Kullanıcı";
    }

    // Kullanıcı ID'sini username ile getir
    public static int getUserId(String username) {
        String sql = "SELECT id FROM User WHERE username = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.out.println("❌ Kullanıcı ID alınamadı: " + e.getMessage());
        }
        return -1;
    }

    // Filtrelenmiş bağışları getir
    public static List<Donation> getFilteredDonations(int userId, String typeFilter, LocalDate startDate, LocalDate endDate) {
        String type = (typeFilter != null && !typeFilter.equalsIgnoreCase("Tümü")) ? typeFilter : null;
        return donationDao.getDonationsWithFiltersByUserId(userId, type, startDate, endDate);
    }

    // Kullanıcı emailini username ile getir
    public static String getUserEmail(String username) {
    String email = null;
    String sql = "SELECT email FROM User WHERE LOWER(username) = LOWER(?)";
    try (Connection conn = DBconnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, username.trim());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            email = rs.getString("email");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return email;
}


    // Şifre sıfırlama
    public static boolean resetPassword(String username, String newPassword) {
        String sql = "UPDATE User SET password = ? WHERE username = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
