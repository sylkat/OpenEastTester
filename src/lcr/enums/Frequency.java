package lcr.enums;

public enum Frequency {
    HZ0(0),
    HZ100(100),
    HZ120(120),
    HZ1000(1000),
    HZ9999(9999), // Nuestro viejo amigo para engañar al bug de los 10kHz
    HZ40000(40000),
    HZ100000(100000);

    private final int value;

    Frequency(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static Frequency fromValue(int value) {
        if (value == 10000) {
            value = 9999;
        }

        for (Frequency f : Frequency.values()) {
            if (f.getValue() == value) {
                return f;
            }
        }
        throw new IllegalArgumentException("Frecuencia no soportada por el hardware: " + value + " Hz");
    }

    @Override
    public String toString() {
        if (this == HZ9999) return "10 kHz";
        if (value >= 1000) return (value / 1000) + " kHz";
        return value + " Hz";
    }
}