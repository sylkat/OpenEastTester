package et431.enums;

public enum BiasVoltage {

    OFF(0),
    MV100(100),
    MV200(200),
    MV300(300),
    MV500(500);

    private final int value;

    BiasVoltage(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
    @Override
    public String toString() {
        if (this == OFF) {
            return "OFF";
        }
        return value + " mV";
    }

    public static BiasVoltage fromString(String text) {
        if (text == null) return null;

        String cleanText = text.trim();

        for (BiasVoltage a : BiasVoltage.values()) {
            if (a.name().equalsIgnoreCase(cleanText) ||
                    String.valueOf(a.getValue()).equals(cleanText) ||
                    a.toString().equalsIgnoreCase(cleanText)) {
                return a;
            }
        }
        throw new IllegalArgumentException("BiasVoltage no soportada: " + text);
    }
}