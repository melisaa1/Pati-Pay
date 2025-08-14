
package patipayproject;

public interface DonationType {
    String getTypeName();
    default String getDefaultUnit() {
        return "adet";
    }
}

