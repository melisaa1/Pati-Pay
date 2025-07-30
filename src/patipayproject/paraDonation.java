
package patipayproject;

public class paraDonation implements DonationType {

    @Override
    public String getTypeName() {
       return "para";
    }
    
    @Override
    public String getDefaultUnit() { 
        return "TRY";
    
    }
    
}
