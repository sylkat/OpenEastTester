package lcr.enums;

/**
 * Representation of the secondary electrical measurement parameters supported
 * by the instrument, evaluating phase, dissipation, quality, or loss characteristics.
 * * @author sylkat
 */
public enum SecondaryParameter {
    X,
    D,
    Q,
    THR,
    ESR,
    EMPTY;

    /**
     * Resolves an incoming configuration label descriptor or parameter string token
     * into its corresponding SecondaryParameter structural enum representation.
     * * @param text the target string payload descriptor to evaluate
     * @return the resolved SecondaryParameter matching enum instance
     * @throws IllegalArgumentException if the provided token parameter cannot be matched
     */
    public static SecondaryParameter fromString(String text) {
        if (text != null) {
            String cleanText = text.trim();
            for (SecondaryParameter p : SecondaryParameter.values()) {
                if (p.name().equalsIgnoreCase(cleanText)) {
                    return p;
                }
            }
        }
        throw new IllegalArgumentException("SecondaryParameter not supported: " + text);
    }
}