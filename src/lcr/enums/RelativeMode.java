package lcr.enums;

public enum RelativeMode {

    ON,
    OFF;

    public static RelativeMode fromString(String text) {
        for (RelativeMode m : RelativeMode.values()) {
            if (m.name().equalsIgnoreCase(text)) {
                return m;
            }
        }
        throw new IllegalArgumentException("RelativeMode no soportado: " + text);
    }
}