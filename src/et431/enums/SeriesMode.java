package et431.enums;

public enum SeriesMode {

    SER("SERial"),
    PAL("PALlel");

    private final String command;

    SeriesMode(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static SeriesMode fromString(String text) {
        for (SeriesMode m : SeriesMode.values()) {
            if (m.name().equalsIgnoreCase(text)) {
                return m;
            }
        }
        throw new IllegalArgumentException("SeriesMode no soportado: " + text);
    }
}