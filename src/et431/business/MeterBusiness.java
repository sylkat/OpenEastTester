package et431.business;

import et431.enums.*;

public class MeterBusiness {
    public ET431 meter;
    public void connect(String port) {
        try {
            meter = new ET431(port);
            meter.connect();
            System.out.println("Connected!");
            Thread.sleep(200);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
