
package patipayproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    
    private static final String DB_URL = "jdbc:sqlite:/Users/user/NetBeansProjects/PatiPayProject/src/patipayproject/DBconnection.db";
    
     public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}

