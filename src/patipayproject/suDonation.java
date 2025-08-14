
package patipayproject;

public class suDonation implements DonationType {

    @Override
    public String getTypeName() {
        return "su";
    }

    @Override
    public String getDefaultUnit() {
        return "adet";
    }
}

