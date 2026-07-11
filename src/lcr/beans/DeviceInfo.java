package lcr.beans;

/**
 * Immutable object containing identification and firmware details of the connected LCR device.
 * * @author sylkat
 */
public class DeviceInfo {

    private final String manufacturer;
    private final String model;
    private final String firmware;
    private final String serialNumber;

    /**
     * Constructs a device information container.
     * * @param manufacturer the hardware manufacturer name
     * @param model        the device model identifier
     * @param firmware     the current firmware version revision
     * @param serialNumber the unique hardware serial number
     */
    public DeviceInfo(String manufacturer, String model, String firmware, String serialNumber) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.firmware = firmware;
        this.serialNumber = serialNumber;
    }

    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public String getFirmware() { return firmware; }
    public String getSerialNumber() { return serialNumber; }

    /**
     * Returns a comma-separated representation of the device info.
     * * @return a CSV formatted string
     */
    @Override
    public String toString() {
        return manufacturer + "," + model + "," + firmware + "," + serialNumber;
    }
}