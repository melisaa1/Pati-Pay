
package patipayproject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    
 

    // Kullanıcı giriş metodu
    public static int login(String username, String password) {
        String sql = "SELECT id FROM User WHERE username = ? AND password = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); // Kullanıcı bulundu, ID döndür
            } else {
                return -1; // Kullanıcı bulunamadı
            }

        } catch (SQLException e) {
            System.out.println("❌ Kullanıcı bulunamadı: " + e.getMessage());
            return -1;
        }
    }
    

    // Kullanıcı kayıt metodu: Başarılıysa true döner, kullanıcı varsa false döner
    public static boolean register(String username, String password) {
        String sqlCheck = "SELECT id FROM User WHERE username = ?";
        String sqlInsert = "INSERT INTO User(username, password) VALUES (?, ?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Kullanıcı zaten var
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



    // Bağış kaydetme işlemi
    public static boolean makeDonation(String username, String type, String date) {
        String userQuery = "SELECT id FROM User WHERE username = ?";
        String insertDonation = "INSERT INTO Donation(user_id, type, date) VALUES (?, ?, ?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement getUserStmt = conn.prepareStatement(userQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertDonation)) {
            
            getUserStmt.setString(1, username);
            ResultSet rs = getUserStmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");

                insertStmt.setInt(1, userId);
                insertStmt.setString(2, type);
                insertStmt.setString(3, date);
                insertStmt.executeUpdate();

                System.out.println("✅ Bağış kaydedildi.");
                return true;
            } else {
                System.out.println("❌ Kullanıcı bulunamadı.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("❌ Bağış kaydında hata: " + e.getMessage());
            return false;
        }
    }

    // Kullanıcının tüm bağışlarını getir
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

                // DonationType interface'ine göre ilgili donation nesnesi oluşturulmalı
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

