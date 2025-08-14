
package patipayproject;

public class DonationFactory {

    public static DonationType createType(String type) {
        return switch (type.toLowerCase()) {
            case "mama" -> new mamaDonation();
            case "su" -> new suDonation();
            case "para" -> new paraDonation();
            default -> throw new IllegalArgumentException("Geçersiz bağış türü: " + type);
        };
    }
}


