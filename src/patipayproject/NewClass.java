/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patipayproject;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author user
 */
public class NewClass {
    
     public static void main(String[] args) {
        try {
            String url = "jdbc:sqlite:/Users/user/NetBeansProjects/PatiPayProject/src/patipayproject/DBconnection.db";
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                System.out.println("✅ Bağlantı başarılı!");
            }
        } catch (Exception e) {
            System.out.println("❌ Bağlantı hatası: " + e.getMessage());
        }
    }
}
    

