package et431.beans;

public class Measurement {

    private final double primaryValue;
    private final double secondaryValue;

    public Measurement(double primaryValue, double secondaryValue) {
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
    }

    public double getPrimaryValue() {
        return primaryValue;
    }

    public double getSecondaryValue() {
        return secondaryValue;
    }

}
