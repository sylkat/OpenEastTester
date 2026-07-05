package et431.business;

import et431.beans.DeviceInfo;
import et431.enums.*;

public class MeterBusiness {
    public LcrMeter meter;

    public void connect(String port) {
        try {
            meter = new ET431(port);
            meter.connect();
            Thread.sleep(400);
            DeviceInfo response = meter.getDeviceInfo();
            if (response.getManufacturer().toUpperCase().contains("HIOKI")) {
                Thread.sleep(400);
                meter.disconnect();
                Thread.sleep(500);
                meter = new HiokiMeter(port);
                meter.connect();
            }
            System.out.println("Connected!");
            Thread.sleep(200);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
