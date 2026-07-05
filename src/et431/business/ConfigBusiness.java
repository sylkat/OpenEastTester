package et431.business;

import et431.enums.*;
import et431.beans.ConfigDTO;

/**
 * Controller to apply configuration parameters to the LCR meter.
 */
public class ConfigBusiness {
    private ConfigDTO currentConfig;

    public void setConfiguration(ConfigDTO config) {
        this.currentConfig = config;
    }

    public ConfigDTO getCurrentConfig() {
        return currentConfig;
    }

    public void applyConfig(LcrMeter meter) {
        if (meter == null || currentConfig == null) return;
        try {
            // Required delays for the hardware to process each command
            meter.setFrequency(currentConfig.getFrequency());
            Thread.sleep(200);
            meter.setVoltage(currentConfig.getVoltage());
            Thread.sleep(200);
            meter.setAperture(currentConfig.getAperture());
            Thread.sleep(200);
            meter.setPrimaryParameter(currentConfig.getPrimaryMeasurement());
            Thread.sleep(200);
            meter.setSecondaryParameter(currentConfig.getSecondaryMeasurement());
            Thread.sleep(200);

            if (currentConfig.isAutoRange()) {
                meter.setAutoRange(true);
            } else {
                meter.setAutoRange(false);
                Thread.sleep(500); // Switching to manual range requires more time
                meter.setRange(currentConfig.getRange());
            }
            Thread.sleep(500);
            meter.setBiasVoltage(currentConfig.getBias());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}