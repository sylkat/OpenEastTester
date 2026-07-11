package lcr.enums;

/**
 * Representation of the primary electrical measurement parameters supported
 * by the instrument, defining the core physical property under evaluation.
 * * @author sylkat
 */
public enum PrimaryParameter {

    AUTO,
    R,
    C,
    L,
    Z,
    DCR,
    ECAP;

    /**
     * Resolves an incoming string descriptor token into its corresponding
     * PrimaryParameter structural enum representation.
     * * @param text the target string payload to evaluate
     * @return the resolved PrimaryParameter matching enum instance
     * @throws IllegalArgumentException if the provided token parameter cannot be matched
     */
    public static PrimaryParameter fromString(String text) {
        if (text != null) {
            String cleanText = text.trim();
            for (PrimaryParameter p : PrimaryParameter.values()) {
                if (p.name().equalsIgnoreCase(cleanText)) {
                    return p;
                }
            }
        }
        throw new IllegalArgumentException("PrimaryParameter not supported: " + text);
    }
}