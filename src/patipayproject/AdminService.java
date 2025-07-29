
package patipayproject;

public class AdminService {
    
     private String username;

        public static boolean login(String username, String password) {
        // Örnek kontrol (gerçek kontrol veritabanıyla olmalı)
        return username.equals("admin") && password.equals("admin123");
    }
    public AdminService(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
