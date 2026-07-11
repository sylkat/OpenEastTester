package lcr.enums;

/**
 * Representation of the instrument's relative measurement state mode toggle,
 * used to zero out parasitic baseline metrics against a reference component value.
 * * @author sylkat
 */
public enum RelativeMode {

    ON,
    OFF;

    /**
     * Resolves an incoming configuration label descriptor or state string token
     * into its corresponding RelativeMode structural enum representation.
     * * @param text the target string payload state identifier to evaluate
     * @return the resolved RelativeMode matching enum instance
     * @throws IllegalArgumentException if the provided token parameter cannot be matched
     */
    public static RelativeMode fromString(String text) {
        if (text != null) {
            String cleanText = text.trim();
            for (RelativeMode m : RelativeMode.values()) {
                if (m.name().equalsIgnoreCase(cleanText)) {
                    return m;
                }
            }
        }
        throw new IllegalArgumentException("RelativeMode state not supported: " + text);
    }
}