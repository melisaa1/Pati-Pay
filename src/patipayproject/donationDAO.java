
package patipayproject;


import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class donationDAO {
    
    public void addDonation(Donation donation) {
        String sql = "INSERT INTO Donation(user_id, type, date) VALUES (?, ?, ?)";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, donation.getUserId());
            pstmt.setString(2, donation.getType().toString());
            pstmt.setString(3, donation.getDate().toString());

            pstmt.executeUpdate();
            System.out.println("Bağış kaydedildi.");

        } catch (SQLException e) {
            System.out.println("Bağış eklenirken hata: " + e.getMessage());
        }
    }

    public List<Donation> getDonationsByUserId(int userId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM Donation WHERE user_id = ?";

        try (Connection conn = DBconnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                 Donation donation = new Donation(
              rs.getInt("id"),
           rs.getInt("user_id"),
             DonationFactory.createType(rs.getString("type")), // Interface yapısı
             LocalDate.parse(rs.getString("date"))
    );
    donations.add(donation);
}

        } catch (SQLException e) {
            System.out.println("Bağışlar çekilirken hata: " + e.getMessage());
        }

        return donations;
 
    }
    
    public Optional<Map.Entry<Integer, Integer>> getTopDonorOfMonth() {
    String sql = "SELECT user_id, COUNT(*) as total FROM Donation " +
                 "WHERE strftime('%Y-%m', date) = strftime('%Y-%m', 'now') " +
                 "GROUP BY user_id ORDER BY total DESC LIMIT 1";

    try (Connection conn = DBconnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            int userId = rs.getInt("user_id");
            int total = rs.getInt("total");
            return Optional.of(new AbstractMap.SimpleEntry<>(userId, total));
        }
    } catch (SQLException e) {
        System.out.println("Winner bulunurken hata: " + e.getMessage());
    }
    return Optional.empty();
}

    public List<Donation> getAllDonations() {
        List<Donation> donations = new ArrayList<>();
        try (Connection conn = DBconnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Donation")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String type = rs.getString("type");
                String date = rs.getString("date");
                   donations.add(new Donation(
    id,
    userId,
    DonationFactory.createType(type), // string → interface
    LocalDate.parse(date)             // string → LocalDate
));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
}

} 

