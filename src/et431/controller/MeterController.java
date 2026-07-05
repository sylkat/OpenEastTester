package et431.controller;

import et431.beans.ConfigDTO;
import et431.enums.*;
import et431.view.MeterView;
import et431.beans.DeviceInfo;
import et431.business.ConfigBusiness;
import et431.business.MeasurementBusiness;
import et431.business.MeterBusiness;
import et431.observer.MeasurementObserver;

import javax.swing.*;

/**
 * Main controller handling UI interactions and driving the LCR meter business logic.
 */
public class MeterController {
    private final MeterView meterView;
    private final MeasurementBusiness measurementBusiness;
    private final ConfigBusiness configBusiness;
    private final MeterBusiness meterBusiness;
    private final Object serialLock = new Object();

    public MeterController(MeterView meterView) {
        this.meterView = meterView;
        this.meterBusiness = new MeterBusiness();
        this.configBusiness = new ConfigBusiness();
        this.measurementBusiness = new MeasurementBusiness(this.meterBusiness, configBusiness, serialLock);

        if (meterView instanceof MeasurementObserver) {
            this.measurementBusiness.addObserver((MeasurementObserver) meterView);
        }
    }

    public boolean connectButtonPressed(String selectedPort) {
        if (meterBusiness.meter == null || !meterBusiness.meter.isConnected()) {
            if(connect(selectedPort)){
                return true;
            }else{
                return false;
            }

        } else {
            disconnect();
            return true;
        }
    }

    private boolean connect(String port) {
        try {
            meterBusiness.connect(port);
            boolean connected = meterBusiness.meter.isConnected();
            if (!connected) {
                return false;
            }
            DeviceInfo info = meterBusiness.meter.getDeviceInfo();
            Thread.sleep(500);
            meterView.updateConnectionState(connected, info.getModel(), info.getFirmware(), port);
            measurementBusiness.startMeasurementTimer();
            return true;
        } catch (Exception ex) {
            System.err.println("Failed to establish connection on port " + port + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    public void disconnect() {
        if (meterBusiness.meter != null) {
            meterBusiness.meter.disconnect();
        }
        measurementBusiness.stopMeasurementTimer();
        meterView.updateConnectionState(false, "", "", "");
    }

    public void applyConfiguration(ConfigDTO config) {
        if (measurementBusiness != null) {
            measurementBusiness.pauseSync(2500); // Higher margin due to multiple internal delays
        }
        configBusiness.setConfiguration(config);

        new Thread(() -> {
            try {
                if (meterBusiness.meter != null) {
                    synchronized (serialLock) {
                        configBusiness.applyConfig(meterBusiness.meter);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error applying full configuration block: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeFrequency(Frequency freq) {
        measurementBusiness.pauseSync(1500);
        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setFrequency(freq);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setFrequency(freq);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing meter frequency: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeVoltage(Voltage volt) {
        measurementBusiness.pauseSync(1500);

        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setVoltage(volt);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setVoltage(volt);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing meter voltage: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeAperture(Aperture aper) {
        measurementBusiness.pauseSync(1500);
        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setAperture(aper);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setAperture(aper);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing meter aperture: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changePrimaryParameter(PrimaryParameter prim) {
        measurementBusiness.pauseSync(1500);
        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setPrimaryParameter(prim);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setPrimaryMeasurement(prim);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing primary parameter: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeSecondaryParameter(SecondaryParameter sec) {
        measurementBusiness.pauseSync(1500);
        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setSecondaryParameter(sec);
                    }
                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setSecondaryMeasurement(sec);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing secondary parameter: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeSeriesMode(SeriesMode mode) {
        measurementBusiness.pauseSync(1500);
        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setSeriesMode(mode);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setSeriesMode(mode);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing series/parallel mode: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeAutoRange(boolean auto) {
        measurementBusiness.pauseSync(1500);

        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setAutoRange(auto);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setAutoRange(auto);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error toggling auto-range: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeRange(Range range) {
        measurementBusiness.pauseSync(1500);

        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setRange(range);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setRange(range);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing measurement range: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public void changeBias(BiasVoltage bias) {
        measurementBusiness.pauseSync(1500);

        new Thread(() -> {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    synchronized (serialLock) {
                        meterBusiness.meter.setBiasVoltage(bias);
                    }

                    if (configBusiness.getCurrentConfig() != null) {
                        configBusiness.getCurrentConfig().setBias(bias);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error changing bias voltage: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public Object getSerialLock() {
        return this.serialLock;
    }
}