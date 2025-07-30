
package patipayproject;


public class mamaDonation implements DonationType{

    @Override
    public String getTypeName() {
        
        return "mama";
    }
    
    @Override
    public String getDefaultUnit() { 
        return "kg";
    
    }
    
}
