package lcr.enums;

/**
 * Representation of the instrument's integration window speed aperture settings,
 * determining the balance between measurement resolution and sampling speed.
 * * @author sylkat
 */
public enum Aperture {
    FAST("FAST"),
    NORM("NORM"),
    MED("MEDium"),
    SLOW("SLOW"),
    SLOW2("SLOW2");

    private final String command;

    /**
     * Constructs the aperture enumeration token with its matching SCPI string value.
     * * @param command the string segment expected by the hardware registers
     */
    Aperture(String command) {
        this.command = command;
    }

    /**
     * Gets the associated raw SCPI instruction token mapping value.
     * * @return the literal string command argument
     */
    public String getCommand() {
        return command;
    }

    /**
     * Resolves a raw string response or configuration key into its matching structural
     * Aperture enum representation, checking both command keywords and name variations.
     * * @param text the incoming text label string to evaluate
     * @return the resolved Aperture enumeration token
     * @throws IllegalArgumentException if the provided text is null or cannot be matched
     */
    public static Aperture fromString(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Aperture is null");
        }

        String cleanText = text.trim();

        for (Aperture a : Aperture.values()) {
            if (a.command.equalsIgnoreCase(cleanText) || a.name().equalsIgnoreCase(cleanText)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Aperture not supported: " + cleanText);
    }
}