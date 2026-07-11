package lcr.business;

import lcr.beans.ConfigDTO;

/**
 * Controller class responsible for managing and applying hardware configuration
 * parameters sequentially to the LCR meter device.
 * * @author sylkat
 */
public class ConfigBusiness {

    private ConfigDTO currentConfig;

    /**
     * Sets the local configuration parameters.
     * * @param config the configuration data transfer object
     */
    public void setConfiguration(ConfigDTO config) {
        this.currentConfig = config;
    }

    /**
     * Retrieves the current local configuration parameters.
     * * @return the active ConfigDTO instance
     */
    public ConfigDTO getCurrentConfig() {
        return currentConfig;
    }

    /**
     * Transmits and applies the current configuration sequence to the LCR meter
     * hardware with necessary stabilization delays.
     * * @param meter the target LCR meter hardware controller instance
     */
    public void applyConfig(LcrMeter meter) {
        if (meter == null || currentConfig == null) {
            return;
        }

        try {
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
                Thread.sleep(500);
                meter.setRange(currentConfig.getRange());
            }

            Thread.sleep(500);
            meter.setBiasVoltage(currentConfig.getBias());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}