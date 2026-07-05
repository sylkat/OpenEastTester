package et431.enums;

public enum SupportedMeter {
    EAST_TESTER("East Tester"),
    HIOKI("Hioki"),
    TONGHUI("Tonghui");

    private final String displayName;

    SupportedMeter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}