package et431.business;

import et431.beans.ConfigDTO;
import et431.beans.Measurement;
import et431.beans.MeasurementDTO;
import et431.enums.*;
import et431.observer.MeasurementObserver;
import et431.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Background worker that continuously polls data and syncs config from the LCR meter.
 */
public class MeasurementBusiness implements Runnable {
    private volatile long pauseUntil = 0;
    private Thread workerThread;
    private volatile boolean running = false;
    private static final int CONFIG_SYNC_INTERVAL = 5;
    private final MeterBusiness meterBusiness;
    private final ConfigBusiness configBusiness;
    private final List<MeasurementObserver> observers = new ArrayList<>();
    private static final double MAX_RESISTANCE_LIMIT = 20_000_000.0;
    private final Object serialLock;

    public MeasurementBusiness(MeterBusiness meterBusiness, ConfigBusiness configBusiness,Object serialLock) {
        this.meterBusiness = meterBusiness;
        this.configBusiness = configBusiness;
        this.serialLock=serialLock;
    }

    public void addObserver(MeasurementObserver observer) {
        this.observers.add(observer);
    }

    public void startMeasurementTimer() {
        if (running) return;

        running = true;
        workerThread = new Thread(this, "LCR-Reader-Thread");
        workerThread.start();
    }

    public void stopMeasurementTimer() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
    }


    @Override
    public void run() {
        int cycleCount = 0;
        while (running) {
            try {
                if (System.currentTimeMillis() < pauseUntil) {
                    Thread.sleep(500);
                    continue;
                }
                if (meterBusiness.meter != null && meterBusiness.meter.isConnected()) {
                    // Periodic sync to make sure UI and hardware stay matching
                    if (cycleCount % CONFIG_SYNC_INTERVAL == 0) {
                        syncConfigFromHardware();
                    }
                    ConfigDTO currentConfig = configBusiness.getCurrentConfig();
                    if (currentConfig != null) {
                        readAndNotifyMeasurement(currentConfig);
                    }
                }
                cycleCount++;
                Thread.sleep(200); // Polling rate delay
            } catch (InterruptedException e) {
                System.err.println("Measurement thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                System.err.println("Error in measurement loop: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void syncConfigFromHardware() {
        try {
            Frequency freq = meterBusiness.meter.getFrequency();
            Voltage volt = meterBusiness.meter.getVoltage();
            Aperture aper = meterBusiness.meter.getAperture();
            PrimaryParameter prim = meterBusiness.meter.getPrimaryParameter();
            SecondaryParameter sec = meterBusiness.meter.getSecondaryParameter();
            boolean autoRange = meterBusiness.meter.isAutoRange();
            Range range = null;
            if(!autoRange){
                 range = meterBusiness.meter.getRange();
            }
            BiasVoltage bias = meterBusiness.meter.getBiasVoltage();
            ConfigDTO hardwareConfig = new ConfigDTO(freq, volt, aper, prim, sec, autoRange, range, bias);
            configBusiness.setConfiguration(hardwareConfig);
            for (MeasurementObserver observer : observers) {
                observer.updateUIFromConfig(hardwareConfig);
            }
        } catch (Exception e) {
            System.err.println("Failed to sync configuration from hardware: " + e.getMessage());
        }
    }

    private void readAndNotifyMeasurement(ConfigDTO currentConfig) throws Exception {
        Measurement measurement;
        Map<DerivateResistance, Double> resistanceDerivator=null;
        Map<DerivateCapacitance, Double> capacitanceDerivator=null;
        Map<DerivateInductance, Double> inductanceDerivator=null;
        Map<DerivateImpedance, Double> impedanceDerivator=null;
        // Lock to avoid simultaneous commands on the serial port
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
        String displayValueA = ValueFormatter.formatSI(Math.abs(valueA), unitA);
        String displayValueB = ValueFormatter.formatSI(Math.abs(valueB), unitB);
        if(currentConfig.getSecondaryMeasurement().equals(SecondaryParameter.D)){
            displayValueB=Math.abs(valueB)+"";
        }
        // Overload handling for values exceeding limits
        if (unitA.equals("Ω") && Math.abs(valueA) >= MAX_RESISTANCE_LIMIT) {
            displayValueA = "OL";
        }

        switch (currentConfig.getPrimaryMeasurement().toString()){
            case "R":
               resistanceDerivator=ResistanceDerivator.calculate(valueA,valueB,currentConfig.getFrequency().getValue());
                break;
            case "C":
               capacitanceDerivator= CapacitanceDerivator.calculate(valueA,valueB,currentConfig.getFrequency().getValue());
                break;
            case "L":
                 inductanceDerivator=InductanceDerivator.calculate(valueA,valueB,currentConfig.getFrequency().getValue());
                break;
            case "Z":
                 impedanceDerivator= ImpedanceDerivator.calculate(valueA,valueB,currentConfig.getFrequency().getValue());
                break;

        }
        MeasurementDTO dto = new MeasurementDTO(
                currentConfig.getPrimaryMeasurement().toString(),
                typeAStr,
                displayValueA,
                typeBStr,
                displayValueB,
                resistanceDerivator,
                capacitanceDerivator,
                inductanceDerivator,
                impedanceDerivator);
        for (MeasurementObserver observer : observers) {
            observer.onMeasurementReceived(dto);
        }
    }

    public String getManualUnit(Object param) {
        if (param == PrimaryParameter.C) return "F";
        if (param == PrimaryParameter.L) return "H";
        return "Ω";
    }

    public String getManualUnitSecondary(Object param) {
        if (param == SecondaryParameter.D) return "";
        if (param == SecondaryParameter.Q) return "";
        if (param == SecondaryParameter.THR) return "deg";
        return "Ω";
    }

    public String detectPrimaryAutoUnit(double value) {
        // Fallback guess: low values are likely capacitance, else resistance
        return (Math.abs(value) > 0 && Math.abs(value) < 0.001) ? "F" : "Ω";
    }
    public void pauseSync(long durationMs) {
        this.pauseUntil = System.currentTimeMillis() + durationMs;
    }
}