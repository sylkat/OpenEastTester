package lcr.business;

import lcr.enums.*;

/**
 * Factory and coordinator class responsible for initializing, selecting,
 * and establishing connections to different supported LCR meter hardware models.
 * * @author sylkat
 */
public class MeterBusiness {

    public LcrMeter meter;

    /**
     * Instantiates the matching LcrMeter implementation based on the selected model
     * type and opens the communication channel over the specified serial port link.
     * * @param port          the OS serial port name identifier
     * @param selectedModel the specific hardware manufacturer enum model type
     */
    public void connect(String port, SupportedMeter selectedModel) {
        try {
            switch (selectedModel) {
                case EAST_TESTER:
                    meter = new ETMeter(port);
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