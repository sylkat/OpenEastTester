package et431.enums;

public enum Range {
    R100(100),
    R1000(1000),
    R10000(10000),
    R100000(100000);

    private final int value;

    Range(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    @Override
    public String toString() {
        if (value >= 1000) {
            int kValue = value / 1000;
            return kValue + " kΩ";
        }
        return value + " Ω";
    }

    public static Range fromString(String text) {
        if (text == null) return null;

        String cleanText = text.trim();

        for (Range r : Range.values()) {
            if (String.valueOf(r.getValue()).equals(cleanText) ||
                    r.name().equalsIgnoreCase(cleanText) ||
                    r.toString().equalsIgnoreCase(cleanText)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rango (Range) no soportado: " + text);
    }
}