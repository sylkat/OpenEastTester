package lcr.enums;

public enum Aperture {
    FAST("FAST"),
    NORM("NORM"),
    MED("MEDium"),
    SLOW("SLOW"),
    SLOW2("SLOW2");
    private final String command;

    Aperture(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

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
        throw new IllegalArgumentException(" (Aperture) not supported: " + cleanText);
    }
}