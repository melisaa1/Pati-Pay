
package patipayproject;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public static String login(String username, String password) {
        String sql = "SELECT role FROM User WHERE username = ? AND password = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
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

    public static boolean register(String username, String password, String role) {
        String sqlCheck = "SELECT id FROM User WHERE username = ?";
        String sqlInsert = "INSERT INTO User(username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("❌ Bu kullanıcı adı zaten mevcut.");
                return false;
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password);
                    insertStmt.setString(3, role);
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

    // Bağış yap (puan hesaplama basit: miktar puan olarak sayılıyor)
    public static boolean makeDonation(int userId, String type, LocalDate date, double amount, String unit) {
        String sql = "INSERT INTO Donation(user_id, type, date, amount, unit) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, date.toString());
            ps.setDouble(4, amount);
            ps.setString(5, unit);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.out.println("❌ Bağış kaydında hata: " + e.getMessage());
            return false;
        }
    }

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

    public static double getTotalScoreByUserId(int userId) {
        double totalScore = 0;
        String sql = "SELECT SUM(amount) as total FROM Donation WHERE user_id = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                totalScore = rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.out.println("❌ Toplam puan alınamadı: " + e.getMessage());
        }
        return totalScore;
    }

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

    public static int getUserId(String username) {
        String sql = "SELECT id FROM User WHERE username = ?";
        try (Connection conn = DBconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.out.println("❌ Kullanıcı ID alınamadı: " + e.getMessage());
        }
        return -1;
    }
    


    private static donationDAO donationDao = new donationDAO();

    public static List<Donation> getFilteredDonations(int userId, String typeFilter, LocalDate startDate, LocalDate endDate) {
        // Eğer "Tümü" seçildiyse filtre türünü boş yap
        String type = (typeFilter != null && !typeFilter.equalsIgnoreCase("Tümü")) ? typeFilter : null;

        // donationDAO'da username yerine userId bazlı bir filtreleme yapılacak şekilde getDonationsWithFilters metodunu kullanabiliriz.
        // Eğer böyle bir metod yoksa donationDAO'da aşağıdaki gibi bir metod oluşturmalısın.
        
        return donationDao.getDonationsWithFiltersByUserId(userId, type, startDate, endDate);
    }
}


