public enum Merchandise {
    MERCHANDISE1("1"),
    MERCHANDISE2("2");

    private final String value;

    Merchandise(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Merchandise parseMerchandise(String propertyValue) {
        for (Merchandise enumValue : Merchandise.values()) {
            if (enumValue.getValue().equals(propertyValue)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant for property value: " + propertyValue);
    }

}
