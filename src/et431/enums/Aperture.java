package et431.enums;

public enum Aperture {
    FAST("FAST"),
    MED("MEDium"),
    SLOW("SLOW");

    private final String command;

    Aperture(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static Aperture fromString(String text) {
        if (text == null) {
            throw new IllegalArgumentException("El texto de Aperture no puede ser null");
        }

        String cleanText = text.trim();

        for (Aperture a : Aperture.values()) {
            if (a.command.equalsIgnoreCase(cleanText) || a.name().equalsIgnoreCase(cleanText)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Velocidad (Aperture) no soportada: " + cleanText);
    }
}