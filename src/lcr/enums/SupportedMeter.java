package lcr.enums;

/**
 * Representation of the different hardware instrument manufacturers and models
 * officially supported by the application's driving communication layer layers.
 * * @author sylkat
 */
public enum SupportedMeter {

    EAST_TESTER("East Tester"),
    HIOKI("Hioki"),
    TONGHUI("Tonghui");

    private final String displayName;

    /**
     * Constructs the supported meter model token with its matching user interface display name.
     * * @param displayName the human-readable manufacturer label text string
     */
    SupportedMeter(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the associated friendly label text string for UI component rendering.
     * * @return the descriptive display name string
     */
    public String getDisplayName() {
        return displayName;
    }
}