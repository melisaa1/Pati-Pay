
package patipayproject;

import java.time.LocalDate;

public class Donation {
    
    private int id; 
    private int userId; 
    private String username;
    private DonationType type;
    private LocalDate date;
    private double amount;
    private String unit;
    

    public Donation(int id, int userId, DonationType type, LocalDate date, double amount, String unit) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.date = date;
        this.amount=amount;
        this.unit=unit;
    }
  

    public Donation(int userId, DonationType type, LocalDate date, double amount, String unit) {
        this(0, userId, type, date, amount, unit);
    }
    
    public Donation(DonationType type, String date) {
    this.type = type;
    this.date = LocalDate.parse(date);
    this.amount=0;
}
    
    public Donation(int id, int userId, String username, DonationType type,
                LocalDate date, double amount, String unit) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.type = type;
    this.date = date;
    this.amount = amount;
    this.unit = unit;
}




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
    
    public double getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTypeName() {
        return type.getTypeName();
    }

    
    @Override
    public String toString() {
         return "Bağış: " + type + " - Tarih: " + date + " - Miktar: " + amount + unit;    }
    
}

