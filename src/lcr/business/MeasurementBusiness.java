package lcr.business;

import lcr.beans.ConfigDTO;
import lcr.beans.Measurement;
import lcr.beans.MeasurementDTO;
import lcr.enums.*;
import lcr.observer.MeasurementObserver;
import lcr.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lcr.util.Constants.LABEL_RESISTANCE;

/**
 * Background worker thread responsible for continuously polling measurement data
 * from the LCR meter, performing metric conversions, and executing periodic hardware configuration syncs.
 * * @author sylkat
 */
public class MeasurementBusiness implements Runnable {

    private volatile long pauseUntil = 0;
    private Thread workerThread;
    private volatile boolean running = false;
    private static final int CONFIG_SYNC_INTERVAL = 5;
    private final MeterBusiness meterBusiness;
    private final ConfigBusiness configBusiness;
    private final List<MeasurementObserver> observers = new ArrayList<>();
    private static final double MAX_RESISTANCE_LIMIT = 450_000_000.0;
    private final Object serialLock;

    /**
     * Constructs the measurement controller loop instance.
     * * @param meterBusiness  the core meter abstraction controller
     * @param configBusiness the core configuration business coordinator
     * @param serialLock     the shared synchronization mutex for safe multi-threaded serial interface usage
     */
    public MeasurementBusiness(MeterBusiness meterBusiness, ConfigBusiness configBusiness, Object serialLock) {
        this.meterBusiness = meterBusiness;
        this.configBusiness = configBusiness;
        this.serialLock = serialLock;
    }

    /**
     * Registers a notification observer to listen for real-time measurements and UI updates.
     * * @param observer the measurement observer implementation
     */
    public void addObserver(MeasurementObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Spawns and initiates the background LCR hardware reader thread pool lifecycle.
     */
    public void startMeasurementTimer() {
        if (running) {
            return;
        }
        running = true;
        workerThread = new Thread(this, "LCR-Reader-Thread");
        workerThread.start();
    }

    /**
     * Signals the background loop worker to stop execution and attempts an immediate thread interrupt.
     */
    public void stopMeasurementTimer() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
    }

    /**
     * Core polling engine loop that processes telemetry reads, tracks connection stability,
     * and triggers automatic settings synchronizations.
     */
    @Override
    public void run() {
        int cycleCount = 0;
        while (running) {
            try {
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    if (cycleCount % CONFIG_SYNC_INTERVAL == 0) {
                        syncConfigFromHardware();
                    }
                    ConfigDTO currentConfig = configBusiness.getCurrentConfig();
                    if (currentConfig != null) {
                        readAndNotifyMeasurement(currentConfig);
                    }
                }
                cycleCount++;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("Measurement thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                if (ex.getMessage().contains("Unable to send command: FETCH?")) {
                    if (!meterBusiness.meter.isConnected()) {
                        for (MeasurementObserver observer : observers) {
                            observer.onDisconnected();
                        }
                        running = false;
                        break;
                    }
                }
                if (Constants.DEBUG > 2) {
                    System.err.println("ERROR IN MEASUREMENT LOOP EXIT: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Spawns a parallel thread worker task to align local configuration descriptors
     * with the current state registers found inside the device hardware layout.
     */
    private void syncConfigFromHardware() {
        Thread syncThread = new Thread(() -> {
            try {
                PrimaryParameter prim = null;
                Frequency freq = null;
                Voltage volt = null;
                Aperture aper = null;
                SecondaryParameter sec = null;
                SeriesMode seriesMode = null;
                boolean autoRange = true;
                Range range = null;
                BiasVoltage bias = null;
                ConfigDTO hardwareConfig = null;

                try {
                    prim = meterBusiness.meter.getPrimaryParameter();
                    if (prim.toString().equals("DCR")) {
                        hardwareConfig = new ConfigDTO(Frequency.HZ0, Voltage.MV0, aper,
                                PrimaryParameter.DCR, SecondaryParameter.EMPTY,
                                true, Range.R1000, BiasVoltage.OFF, SeriesMode.SER);
                        triggerUpdateUi(hardwareConfig);
                        return;
                    }
                } catch (Exception e) {
                    if (Constants.DEBUG > 1) {
                        System.err.println("Failed to read Primary hardware: " + e.getMessage());
                    }
                    return;
                }

                freq = meterBusiness.meter.getFrequency();
                volt = meterBusiness.meter.getVoltage();
                aper = meterBusiness.meter.getAperture();
                sec = meterBusiness.meter.getSecondaryParameter();
                autoRange = meterBusiness.meter.isAutoRange();
                seriesMode = meterBusiness.meter.getSeriesMode();
                if (!autoRange) {
                    range = meterBusiness.meter.getRange();
                }
                bias = meterBusiness.meter.getBiasVoltage();
                hardwareConfig = new ConfigDTO(freq, volt, aper, prim, sec, autoRange, range, bias, seriesMode);
                triggerUpdateUi(hardwareConfig);
            } catch (Exception e) {
                if (Constants.DEBUG > 0) {
                    System.err.println("Failed to sync configuration from hardware: " + e.getMessage());
                }
            }
        });
        syncThread.start();
    }

    /**
     * Commits configuration mappings back into memory storage and dispatches update commands to the UI layer.
     */
    private void triggerUpdateUi(ConfigDTO hardwareConfig) {
        configBusiness.setConfiguration(hardwareConfig);
        for (MeasurementObserver observer : observers) {
            observer.updateUIFromConfig(hardwareConfig);
        }
    }

    /**
     * Performs a synchronized data call frame retrieval via SCPI, derives secondary parameters,
     * scales value strings, and fires complete datasets towards the application listeners.
     */
    private void readAndNotifyMeasurement(ConfigDTO currentConfig) throws Exception {
        Measurement measurement;
        Map<DerivateResistance, Double> resistanceDerivator = null;
        Map<DerivateCapacitance, Double> capacitanceDerivator = null;
        Map<DerivateInductance, Double> inductanceDerivator = null;
        Map<DerivateImpedance, Double> impedanceDerivator = null;

        synchronized (serialLock) {
            measurement = meterBusiness.meter.fetch();
        }

        double valueA = measurement.getPrimaryValue();
        double valueB = measurement.getSecondaryValue();
        String typeAStr = DisplayFormatter.getDisplayLabel(currentConfig.getPrimaryMeasurement().toString());
        String typeBStr = DisplayFormatter.getDisplayLabel(currentConfig.getSecondaryMeasurement().toString());
        String unitA = getManualUnit(currentConfig.getPrimaryMeasurement());
        String unitB = getManualUnitSecondary(currentConfig.getSecondaryMeasurement());

        if (typeAStr.equals("AUTO")) {
            unitA = detectPrimaryAutoUnit(valueA);
        }

        String displayValueA = ValueFormatter.formatSI(valueA, unitA);
        if (typeAStr.startsWith(LABEL_RESISTANCE)) {
            displayValueA = ValueFormatter.formatSI(Math.abs(valueA), unitA);
        }

        String displayValueB = ValueFormatter.formatSI(valueB, unitB);
        if (currentConfig.getSecondaryMeasurement().equals(SecondaryParameter.D)) {
            displayValueB = String.valueOf(valueB);
        }

        if (unitA.equals("Ω") && Math.abs(valueA) >= MAX_RESISTANCE_LIMIT) {
            displayValueA = "OL";
        }
        if (unitB.equals("Ω") && Math.abs(valueB) >= MAX_RESISTANCE_LIMIT) {
            displayValueB = "OL";
        }

        if (currentConfig.getPrimaryMeasurement().toString().equals("DCR")) {
            displayValueB = "";
            typeBStr = "";
            if (valueA == 0.0 && valueB == 0.0) {
                return;
            }
        }

        switch (currentConfig.getPrimaryMeasurement().toString()) {
            case "R":
                resistanceDerivator = ResistanceDerivator.calculate(valueA, valueB, currentConfig.getFrequency().getValue());
                break;
            case "C":
            case "ECAP":
                capacitanceDerivator = CapacitanceDerivator.calculate(valueA, valueB, currentConfig.getFrequency().getValue());
                break;
            case "L":
                inductanceDerivator = InductanceDerivator.calculate(valueA, valueB, currentConfig.getFrequency().getValue());
                break;
            case "Z":
                impedanceDerivator = ImpedanceDerivator.calculate(valueA, valueB, currentConfig.getFrequency().getValue());
                break;
        }

        MeasurementDTO dto = new MeasurementDTO(
                String.valueOf(valueA),
                String.valueOf(valueB),
                currentConfig.getPrimaryMeasurement().toString(),
                typeAStr,
                displayValueA,
                typeBStr,
                displayValueB,
                currentConfig.getSeriesMode(),
                meterBusiness.meter.isConnected(),
                resistanceDerivator,
                capacitanceDerivator,
                inductanceDerivator,
                impedanceDerivator);

        for (MeasurementObserver observer : observers) {
            observer.onMeasurementReceived(dto);
        }
    }

    /**
     * Resolves the primary unit symbol according to the parameter type context.
     * * @param param the target PrimaryParameter enum object
     * @return the metric unit string (F, H, or Ω)
     */
    public String getManualUnit(Object param) {
        if (param == PrimaryParameter.C || param == PrimaryParameter.ECAP) return "F";
        if (param == PrimaryParameter.L) return "H";
        return "Ω";
    }

    /**
     * Resolves the secondary unit symbol according to the parameter type context.
     * * @param param the target SecondaryParameter enum object
     * @return the metric unit string (deg, Ω, or empty string)
     */
    public String getManualUnitSecondary(Object param) {
        if (param == SecondaryParameter.D || param == SecondaryParameter.Q) return "";
        if (param == SecondaryParameter.THR) return "deg";
        return "Ω";
    }

    /**
     * Infers base unit tags during automatic detection routines depending on measurement threshold steps.
     * * @param value the raw real-world numeric reading float level
     * @return a best-guess base unit identifier string (F or Ω)
     */
    public String detectPrimaryAutoUnit(double value) {
        return (Math.abs(value) > 0 && Math.abs(value) < 0.001) ? "F" : "Ω";
    }

    /**
     * Requests a temporary delay pause blocking configurations sweeps during specific operational windows.
     * * @param durationMs total sleep timeline limit expressed in milliseconds
     */
    public void pauseSync(long durationMs) {
        this.pauseUntil = System.currentTimeMillis() + durationMs;
    }
}