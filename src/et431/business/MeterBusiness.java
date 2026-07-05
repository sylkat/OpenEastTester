package et431.business;

import et431.beans.DeviceInfo;
import et431.enums.*;

import static et431.enums.SupportedMeter.*;

public class MeterBusiness {
    public LcrMeter meter;

    public void connect(String port, SupportedMeter selectedModel) {
        try {
            switch (selectedModel) {
                case EAST_TESTER:
                    meter = new ET431(port);
                    meter.connect();
                    break;
                case HIOKI:
                    meter = new HiokiMeter(port);
                    meter.connect();
                    break;
                case TONGHUI:
                    meter = new TonghuiMeter(port);
                    meter.connect();
                    break;
            }
            System.out.println("Connected!");
            Thread.sleep(200);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
