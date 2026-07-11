package lcr.view;

import lcr.beans.ConfigDTO;

/**
 * View interface defining the presentation layer contract for the LCR meter instrument,
 * handling real-time connection telemetry and operational hardware configurations.
 * * @author sylkat
 */
public interface MeterView {

    /**
     * Updates the user interface layout elements to reflect the active serial link status,
     * binding relevant hardware device identification tokens.
     * * @param isConnected  true if the underlying hardware communication pipeline is active
     * @param manufacturer the resolved hardware device manufacturer label
     * @param model        the specific LCR meter model variant descriptor
     * @param firmware     the running hardware control firmware version identification string
     * @param portName     the active physical OS port descriptor (e.g., "COM3", "/dev/ttyUSB0")
     */
    void updateConnectionState(boolean isConnected, String manufacturer, String model, String firmware, String portName);

}