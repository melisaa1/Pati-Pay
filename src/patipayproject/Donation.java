
package patipayproject;

import java.time.LocalDate;

public class Donation {
    
    private int id; // Veritabanı için
    private int userId; // Kullanıcı ID'si
    private DonationType type;
    private LocalDate date;

    public Donation(int id, int userId, DonationType type, LocalDate date) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.date = date;
    }

    public Donation(int userId, DonationType type, LocalDate date) {
        this.userId = userId;
        this.type = type;
        this.date = date;
    }
    
    public Donation(DonationType type, String date) {
    this.type = type;
    this.date = LocalDate.parse(date);
}


    // Getterlar
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public DonationType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    // Veritabanına yazarken kolaylık sağlaması için
    public String getTypeName() {
        return type.getTypeName();
    }

    
    @Override
    public String toString() {
         return "Bağış: " + type + " - Tarih: " + date;    }
    
}

