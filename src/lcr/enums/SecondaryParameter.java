package lcr.enums;

public enum SecondaryParameter {
    X,
    D,
    Q,
    THR,
    ESR,
    EMPTY;

    public static SecondaryParameter fromString(String text) {
        for (SecondaryParameter p : SecondaryParameter.values()) {
            if (p.name().equalsIgnoreCase(text)) {
                return p;
            }
        }
        throw new IllegalArgumentException("SecondaryParameter no soportado: " + text);
    }
}