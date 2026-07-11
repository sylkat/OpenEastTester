package lcr.enums;

/**
 * Representation of the different display screen pages or operational views
 * configurable on the hardware meter front panel layout.
 * * @author sylkat
 */
public enum DisplayPage {

    MEAS("MEASurement"),
    SYST("SYSTem");

    private final String command;

    /**
     * Constructs the display page token mapping with its corresponding SCPI parameter string.
     * * @param command the raw text keyword expected by the instrument configuration subsystems
     */
    DisplayPage(String command) {
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
     * Resolves a raw string identifier descriptor into its matching DisplayPage enumeration token.
     * * @param text the target string key payload to evaluate
     * @return the resolved DisplayPage matching enum instance
     * @throws IllegalArgumentException if the provided token descriptor cannot be resolved
     */
    public static DisplayPage fromString(String text) {
        if (text != null) {
            String cleanText = text.trim();
            for (DisplayPage p : DisplayPage.values()) {
                if (p.name().equalsIgnoreCase(cleanText) || p.command.equalsIgnoreCase(cleanText)) {
                    return p;
                }
            }
        }
        throw new IllegalArgumentException("DisplayPage not supported: " + text);
    }
}