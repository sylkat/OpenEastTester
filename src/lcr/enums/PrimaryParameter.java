package lcr.enums;

public enum PrimaryParameter {

    AUTO,
    R,
    C,
    L,
    Z,
    DCR,
    ECAP;

    public static PrimaryParameter fromString(String text) {
        for (PrimaryParameter p : PrimaryParameter.values()) {
            if (p.name().equalsIgnoreCase(text)) {
                return p;
            }
        }
        throw new IllegalArgumentException("PrimaryParameter no soportado: " + text);
    }
}