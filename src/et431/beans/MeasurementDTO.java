package et431.beans;

public class MeasurementDTO {
    private final String typeA;
    private final String valueA;
    private final String typeB;
    private final String valueB;

    public MeasurementDTO(String typeA, String valueA, String typeB, String valueB) {
        this.typeA = typeA;
        this.valueA = valueA;
        this.typeB = typeB;
        this.valueB = valueB;
    }

    public String getTypeA() { return typeA; }
    public String getValueA() { return valueA; }
    public String getTypeB() { return typeB; }
    public String getValueB() { return valueB; }
}