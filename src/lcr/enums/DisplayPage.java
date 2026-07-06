package lcr.enums;

public enum DisplayPage {

    MEAS("MEASurement"),
    SYST("SYSTem");

    private final String command;

    DisplayPage(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static DisplayPage fromString(String text) {
        for (DisplayPage p : DisplayPage.values()) {
            if (p.name().equalsIgnoreCase(text)) {
                return p;
            }
        }
        throw new IllegalArgumentException("DisplayPage no soportada: " + text);
    }
}