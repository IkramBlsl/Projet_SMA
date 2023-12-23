/**
 * Represents different types of merchandise available.
 * Provides methods to access and parse merchandise properties.
 */
public enum Merchandise {
    MERCHANDISE1("1"),
    MERCHANDISE2("2");

    private final String value;

    Merchandise(String value) {
        this.value = value;
    }
    /**
     * Retrieves the value of the merchandise.
     * @return The value associated with the merchandise
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses a string property value into a Merchandise enum.
     * @param propertyValue The property value to parse
     * @return The corresponding Merchandise enum value
     * @throws IllegalArgumentException if no matching enum constant is found for the property value
     */

    public static Merchandise parseMerchandise(String propertyValue) {
        for (Merchandise enumValue : Merchandise.values()) {
            if (enumValue.getValue().equals(propertyValue)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant for property value: " + propertyValue);
    }

}
