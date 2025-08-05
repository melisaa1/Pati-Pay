
package patipayproject;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    
    public static int login(String username, String password) {
        String sql = "SELECT id FROM User WHERE username = ? AND password = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); 
            } else {
                return -1; 
            }

        } catch (SQLException e) {
            System.out.println("❌ Kullanıcı bulunamadı: " + e.getMessage());
            return -1;
        }
    }
    

    public static boolean register(String username, String password) {
        String sqlCheck = "SELECT id FROM User WHERE username = ?";
        String sqlInsert = "INSERT INTO User(username, password) VALUES (?, ?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false;
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password);
                    insertStmt.executeUpdate();
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Kayıt hatası: " + e.getMessage());
            return false;
        }
    }



  public static boolean makeDonation(int userId, String type, LocalDate date, double amount, String unit) {
    String sql = "INSERT INTO Donation(user_id, type, date, amount, unit) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DBconnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);
        ps.setString(2, type);
        ps.setString(3, date.toString());
        ps.setDouble(4, amount);
        ps.setString(5, unit);
        ps.executeUpdate();
        System.out.println("✅ Bağış kaydedildi.");
        return true;
    } catch (SQLException e) {
        System.out.println("❌ Bağış kaydında hata: " + e.getMessage());
        return false;
    }
}



    public static List<Donation> getUserDonations(String username) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT d.type, d.date FROM Donation d " +
                     "JOIN User u ON d.user_id = u.id WHERE u.username = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                String date = rs.getString("date");

                DonationType donationType = DonationFactory.createType(type);
                donations.add(new Donation(donationType, date));
            }

        } catch (SQLException e) {
            System.out.println("❌ Bağışları getirirken hata: " + e.getMessage());
        }

        return donations;
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
}

