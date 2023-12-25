/**
 * Represents different types of products available.
 * Provides methods to access and parse product properties.
 */
public enum Product {
    PRODUCT1("1"),
    PRODUCT2("2");

    private final String value;

    Product(String value) {
        this.value = value;
    }

    /**
     * Retrieves the value of the product.
     * @return The value associated with the product.
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses a string property value into a Product.
     * @param propertyValue The property value to parse.
     * @return The corresponding Product.
     * @throws IllegalArgumentException if no matching enum constant is found for the property value.
     */
    public static Product parseProduct(String propertyValue) {
        for (Product enumValue : Product.values()) {
            if (enumValue.getValue().equals(propertyValue)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant for property value: " + propertyValue);
    }

}
