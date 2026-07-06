package lcr.beans;

public class DeviceInfo {

    private final String manufacturer;
    private final String model;
    private final String firmware;
    private final String serialNumber;

    public DeviceInfo(String manufacturer,
                      String model,
                      String firmware,
                      String serialNumber) {

        this.manufacturer = manufacturer;
        this.model = model;
        this.firmware = firmware;
        this.serialNumber = serialNumber;

    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getFirmware() {
        return firmware;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String toString() {
        return manufacturer + "," + model + "," + firmware + "," + serialNumber;
    }

}