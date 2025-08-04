
package patipayproject;


import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class donationDAO {
    
   public void addDonation(Donation donation) {
    String sql = "INSERT INTO Donation(user_id, type, date, amount, unit) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DBconnection.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, donation.getUserId());
        pstmt.setString(2, donation.getTypeName());
        pstmt.setString(3, donation.getDate().toString());
        pstmt.setDouble(4, donation.getAmount());
        pstmt.setString(5, donation.getUnit());
        pstmt.executeUpdate();

        System.out.println("Bağış kaydedildi.");
    } catch (SQLException e) {
        System.out.println("Bağış eklenirken hata: " + e.getMessage());
    }
}


    public List<Donation> getDonationsByUserId(int userId) {
    List<Donation> donations = new ArrayList<>();
    String sql = "SELECT id, user_id, type, date, amount, unit FROM Donation WHERE user_id = ? ORDER BY date DESC, id ASC";

     try (Connection conn = DBconnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            donations.add(new Donation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                DonationFactory.createType(rs.getString("type")),
                LocalDate.parse(rs.getString("date")),
                rs.getDouble("amount"),
                rs.getString("unit")
            ));
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
    String sql = "SELECT id, user_id, type, date, amount, unit FROM Donation ORDER BY date DESC, id ASC";

    try (Connection conn = DBconnection.connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            donations.add(new Donation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                DonationFactory.createType(rs.getString("type")),
                LocalDate.parse(rs.getString("date")),
                rs.getDouble("amount"),
                rs.getString("unit")
            ));
        }
    } catch (SQLException e) {  e.printStackTrace();  }
  
    return donations;
}
  public Map<String, Object> getSummaryWithFilters(
            String usernameOrNull,
            String typeOrNull,
            LocalDate startOrNull,
            LocalDate endOrNull
    ) {
        StringBuilder sb = new StringBuilder(
            "SELECT COUNT(*) AS cnt, COALESCE(SUM(d.amount),0) AS total " +
            "FROM Donation d JOIN User u ON d.user_id = u.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (usernameOrNull != null && !usernameOrNull.isBlank()) {
            sb.append(" AND u.username LIKE ? ");
            params.add("%" + usernameOrNull.trim() + "%");
        }
        if (typeOrNull != null && !typeOrNull.isBlank()) {
            sb.append(" AND lower(d.type) = ? ");
            params.add(typeOrNull.toLowerCase());
        }
        if (startOrNull != null) {
            sb.append(" AND d.date >= ? ");
            params.add(startOrNull.toString());
        }
        if (endOrNull != null) {
            sb.append(" AND d.date <= ? ");
            params.add(endOrNull.toString());
        }

        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("count", rs.getInt("cnt"));
                    map.put("total", rs.getDouble("total"));
                    return map;
                }
            }
        } catch (SQLException e) {
            System.out.println("Özet hatası: " + e.getMessage());
        }
        Map<String, Object> empty = new HashMap<>();
        empty.put("count", 0);
        empty.put("total", 0.0);
        return empty;
    }

  
     public List<Map<String, Object>> getTypeBreakdown(
            String usernameOrNull,
            LocalDate startOrNull,
            LocalDate endOrNull
    ) {
        StringBuilder sb = new StringBuilder(
            "SELECT d.type, COUNT(*) AS cnt, COALESCE(SUM(d.amount),0) AS total " +
            "FROM Donation d JOIN User u ON d.user_id = u.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (usernameOrNull != null && !usernameOrNull.isBlank()) {
            sb.append(" AND u.username LIKE ? ");
            params.add("%" + usernameOrNull.trim() + "%");
        }
        if (startOrNull != null) {
            sb.append(" AND d.date >= ? ");
            params.add(startOrNull.toString());
        }
        if (endOrNull != null) {
            sb.append(" AND d.date <= ? ");
            params.add(endOrNull.toString());
        }
        sb.append(" GROUP BY d.type ORDER BY d.type ASC");

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBconnection.connect();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("type", rs.getString("type"));
                    row.put("count", rs.getInt("cnt"));
                    row.put("total", rs.getDouble("total"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Tür özeti hatası: " + e.getMessage());
        }
        return rows;
    }
 public List<Donation> getDonationsWithFilters(
        String usernameFilter,
        String typeFilter,
        LocalDate startDate,
        LocalDate endDate,
        String orderBy
) {
    List<Donation> donations = new ArrayList<>();
    String sql =
        "SELECT d.id, d.user_id, u.username, d.type, d.date, d.amount, d.unit " +
        "FROM Donation d " +
        "JOIN User u ON d.user_id = u.id " +
        "WHERE 1=1";

    if (usernameFilter != null && !usernameFilter.isBlank()) {
        sql += " AND u.username LIKE ?";
    }
    if (typeFilter != null && !typeFilter.isBlank()) {
        sql += " AND d.type = ?";
    }
    if (startDate != null) {
        sql += " AND d.date >= ?";
    }
    if (endDate != null) {
        sql += " AND d.date <= ?";
    }
    if (orderBy != null && !orderBy.isBlank()) {
        sql += " ORDER BY " + orderBy;
    }

    try (Connection conn = DBconnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        int idx = 1;
        if (usernameFilter != null && !usernameFilter.isBlank()) {
            ps.setString(idx++, "%" + usernameFilter + "%");
        }
        if (typeFilter != null && !typeFilter.isBlank()) {
            ps.setString(idx++, typeFilter);
        }
        if (startDate != null) {
            ps.setString(idx++, startDate.toString());
        }
        if (endDate != null) {
            ps.setString(idx++, endDate.toString());
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            donations.add(new Donation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("username"),
                DonationFactory.createType(rs.getString("type")),
                LocalDate.parse(rs.getString("date")),
                rs.getDouble("amount"),
                rs.getString("unit")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return donations;
}
 
 
   public static List<String> getTopDonorsByScore(){
       final double paraPuan=1;
       final double mamaPuan=0.8;
       final double suPuan=0.6;
       
       Map<String, Double > scoreMap=new HashMap<>();
       
       String query = "SELECT u.username, d.type, d.amount " +
               "FROM Donation d " +
               "JOIN User u ON d.user_id = u.id";

       
       try(Connection conn=DBconnection.connect();
               PreparedStatement stmt=conn.prepareStatement(query);
               ResultSet rs=stmt.executeQuery()){
              
           while(rs.next()){
                 String username = rs.getString("username");
                 String type = rs.getString("type");
                 double amount = rs.getDouble("amount");

                 double puan = 0;
                 
                 
                switch(type){
                    
                    case "Para":
                        puan=amount*paraPuan;
                        break;
                    case "Mama":
                        puan=amount*mamaPuan;
                        break;
                    case "Su":
                        puan=amount*suPuan;
                        break;
                        
            }
            scoreMap.put(username, scoreMap.getOrDefault(username, 0.0) + puan);
        
           }
    } catch (SQLException e) {
        e.printStackTrace();
    }
           return scoreMap.entrySet()
                   .stream()
                   .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                   .limit(3)
                   .map(e -> e.getKey() + " (" + String.format("%.2f", e.getValue()) + " puan)")
                   .collect(Collectors.toList());
}
   
   
   public static double getTotalScoreByUserId(int userId) {
    final double paraPuan = 1;
    final double mamaPuan = 0.8;
    final double suPuan = 0.6;

    double totalScore = 0;

    String query = "SELECT type, amount FROM Donation WHERE user_id = ?";

    try (Connection conn = DBconnection.connect();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String type = rs.getString("type");
            double amount = rs.getDouble("amount");

            switch (type) {
                case "Para":
                    totalScore += amount * paraPuan;
                    break;
                case "Mama":
                    totalScore += amount * mamaPuan;
                    break;
                case "Su":
                    totalScore += amount * suPuan;
                    break;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return totalScore;
}
}
               
   
