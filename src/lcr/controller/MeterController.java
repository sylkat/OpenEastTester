package lcr.controller;

import lcr.beans.ConfigDTO;
import lcr.enums.*;
import lcr.view.MeterView;
import lcr.beans.DeviceInfo;
import lcr.business.ConfigBusiness;
import lcr.business.MeasurementBusiness;
import lcr.business.MeterBusiness;
import lcr.observer.MeasurementObserver;

/**
 * Main application controller coordinating UI user interactions from the view layer
 * and driving async transactional updates to the underlying LCR meter hardware.
 * * @author sylkat
 */
public class MeterController {

    private final MeterView meterView;
    private final MeasurementBusiness measurementBusiness;
    private final ConfigBusiness configBusiness;
    public final MeterBusiness meterBusiness;
    private final Object serialLock = new Object();

    /**
     * Constructs the controller core, mapping business services and attaching
     * the target view component as a measurement lifecycle observer.
     * * @param meterView the main UI frame view presentation instance
     */
    public MeterController(MeterView meterView) {
        this.meterView = meterView;
        this.meterBusiness = new MeterBusiness();
        this.configBusiness = new ConfigBusiness();
        this.measurementBusiness = new MeasurementBusiness(this.meterBusiness, configBusiness, serialLock);

        if (meterView instanceof MeasurementObserver) {
            this.measurementBusiness.addObserver((MeasurementObserver) meterView);
        }
    }

    /**
     * Handles connection toggle actions from the UI button event, routing either
     * a setup connection flow or an immediate graceful disconnect routine.
     * * @param selectedPort  the OS communication channel interface identifier
     * @param selectedModel the specific hardware manufacturer enum model type
     * @return true if the toggle state switch operation finishes successfully
     */
    public boolean connectButtonPressed(String selectedPort, SupportedMeter selectedModel) {
        if (meterBusiness.meter == null || !meterBusiness.meter.isConnected()) {
            return connect(selectedPort, selectedModel);
        } else {
            disconnect();
            return true;
        }
    }

    /**
     * Initializes a physical serial link to the instrument, verifies connection metrics,
     * updates the presentation view bounds, and boots the streaming telemetry loop.
     */
    private boolean connect(String port, SupportedMeter selectedModel) {
        try {
            meterBusiness.connect(port, selectedModel);
            boolean connected = meterBusiness.meter.isConnected();
            if (!connected) {
                return false;
            }

            DeviceInfo info = meterBusiness.meter.getDeviceInfo();
            Thread.sleep(500);

            meterView.updateConnectionState(connected, info.getManufacturer(), info.getModel(), info.getFirmware(), port);
            measurementBusiness.startMeasurementTimer();
            return true;

        } catch (Exception ex) {
            System.err.println("Failed to establish connection on port " + port + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Tears down active streaming worker threads and safely disconnects the physical hardware device link.
     */
    public void disconnect() {
        if (meterBusiness.meter != null) {
            meterBusiness.meter.disconnect();
        }
        measurementBusiness.stopMeasurementTimer();
        meterView.updateConnectionState(false, "", "", "", "");
    }

    /**
     * Suspends the reader thread polling and dispatches a full configuration block payload
     * sequence to the instrument registers in a decoupled thread task.
     * * @param config the structural configuration data transfer object
     */
    public void applyConfiguration(ConfigDTO config) {
        if (measurementBusiness != null) {
            measurementBusiness.pauseSync(2500);
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

    /**
     * Asynchronously updates the hardware sampling signal frequency parameter.
     * * @param freq the target test signal Frequency enum item
     */
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

    /**
     * Asynchronously updates the hardware operational signal voltage limit.
     * * @param volt the target test level Voltage enum item
     */
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

    /**
     * Asynchronously updates the measurement integration window speed aperture parameter.
     * * @param aper the target resolution Aperture speed mode
     */
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

    /**
     * Asynchronously updates the instrument primary reading display mode selector.
     * * @param prim the target PrimaryParameter measurement selector
     */
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

    /**
     * Asynchronously updates the instrument secondary reading display mode selector.
     * * @param sec the target SecondaryParameter measurement selector
     */
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

    /**
     * Asynchronously updates the equivalent circuit matching circuit standard topology.
     * * @param mode the structural SeriesMode configuration matrix
     */
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

    /**
     * Asynchronously toggles automatic hardware scale configuration tracking loops.
     * * @param auto true to hook into automatic scaling hardware routines
     */
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

    /**
     * Asynchronously locks the instrument tracking window inside a strict manual scaling bracket.
     * * @param range the discrete electrical Range value parameter bracket
     */
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

    /**
     * Asynchronously updates the internal physical DC bias injection voltage configuration.
     * * @param bias the target hardware BiasVoltage injection steps level
     */
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

    /**
     * Retrieves the structural synchronization reference for multi-threaded thread operations.
     * * @return the shared serial resource locker mutex object
     */
    public Object getSerialLock() {
        return this.serialLock;
    }
}