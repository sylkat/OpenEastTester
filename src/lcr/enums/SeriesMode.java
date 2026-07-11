package lcr.enums;

/**
 * Representation of the instrument's equivalent circuit model configuration,
 * selecting between serial (SER) or parallel (PAL) parameter compilation.
 * * @author sylkat
 */
public enum SeriesMode {

    SER("SERial"),
    PAL("PALlel");

    private final String command;

    /**
     * Constructs the equivalent circuit layout token mapping with its corresponding SCPI parameter string.
     * * @param command the raw text keyword expected by the instrument configuration subsystems
     */
    SeriesMode(String command) {
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
     * Resolves an incoming configuration label descriptor or circuit mode string token
     * into its corresponding SeriesMode structural enum representation.
     * * @param text the target string payload descriptor to evaluate
     * @return the resolved SeriesMode matching enum instance
     * @throws IllegalArgumentException if the provided token parameter cannot be matched
     */
    public static SeriesMode fromString(String text) {
        if (text != null) {
            String cleanText = text.trim();
            for (SeriesMode m : SeriesMode.values()) {
                if (m.name().equalsIgnoreCase(cleanText) || m.command.equalsIgnoreCase(cleanText)) {
                    return m;
                }
            }
        }
        throw new IllegalArgumentException("SeriesMode not supported: " + text);
    }
}