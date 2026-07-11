package lcr.business;

import lcr.view.BiasSweepDialog;
import lcr.view.RealTimeChartPanel;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Asynchronous worker that performs a bias voltage sweep (0-499)
 * on a background thread and updates the UI chart in real time.
 * * @author Sylkat
 */
public class BiasSweepWorker extends SwingWorker<Boolean, BiasDataPoint> {

    private final SerialConnection serialPort;
    private final RealTimeChartPanel chartPanel;
    private final int maxSteps = 500;
    private final int settlingDelayMs = 35;
    private static final Pattern LCR_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
    private final BiasSweepDialog biasSweepDialog;

    /**
     * Initializes the worker with required hardware and UI references.
     * * @param serialPort      the active serial connection handler
     * @param chartPanel      the main chart panel reference
     * @param biasSweepDialog the dialog displaying the sweep progress
     */
    public BiasSweepWorker(SerialConnection serialPort, RealTimeChartPanel chartPanel, BiasSweepDialog biasSweepDialog) {
        this.serialPort = serialPort;
        this.chartPanel = chartPanel;
        this.biasSweepDialog = biasSweepDialog;
    }

    /**
     * Executes the hardware sweep loop in the background.
     * * @return true if the execution finishes completely
     * @throws Exception if an error occurs during the serial communication
     */
    @Override
    protected Boolean doInBackground() throws Exception {
        for (int bias = 0; bias < maxSteps; bias++) {
            if (isCancelled()) {
                break;
            }

            try {
                serialPort.execute("BIAS:VOLT " + bias);
                Thread.sleep(settlingDelayMs);
                String response = serialPort.execute("FETCH?");

                if (response != null && !response.isEmpty()) {
                    BiasDataPoint point = parseLcrResponse(bias, response);
                    if (point != null) {
                        publish(point);
                    }
                }

                int progressPercent = (int) (((float) bias / maxSteps) * 100);
                setProgress(progressPercent);

            } catch (Exception e) {
                System.err.println("[Open LCR] Error at bias step " + bias + ": " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * Updates the UI chart using published data points on the Event Dispatch Thread.
     * * @param chunks the list of data points accumulated to update
     */
    @Override
    protected void process(List<BiasDataPoint> chunks) {
        for (BiasDataPoint point : chunks) {
            biasSweepDialog.updateSweepGraph(point.getBias(), point.getPrimaryValue());
        }
    }

    /**
     * Handles post-execution cleanup and resets hardware bias back to zero.
     */
    @Override
    protected void done() {
        try {
            get();
        } catch (Exception e) {
            System.err.println("[Open LCR] Exception on worker completion: " + e.getMessage());
        } finally {
            try {
                serialPort.execute("SET:BIAS 0");
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Parses LCR response string to extract primary and secondary measurements.
     * * @param biasStep the current step index
     * @param response the raw string data fetched from the device
     * @return a new BiasDataPoint, or null if parsing fails
     */
    private BiasDataPoint parseLcrResponse(int biasStep, String response) {
        Matcher matcher = LCR_PATTERN.matcher(response);

        double primaryValue = 0.0;
        double secondaryValue = 0.0;

        if (matcher.find()) {
            primaryValue = Double.parseDouble(matcher.group());
        } else {
            return null;
        }

        if (matcher.find()) {
            secondaryValue = Double.parseDouble(matcher.group());
        } else {
            return null;
        }

        return new BiasDataPoint(biasStep, primaryValue, secondaryValue);
    }
}

/**
 * Immutable data holder for parsed bias sweep samples.
 */
class BiasDataPoint {
    private final int bias;
    private final double primaryValue;
    private final double secondaryValue;

    public BiasDataPoint(int bias, double primaryValue, double secondaryValue) {
        this.bias = bias;
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
    }

    public int getBias() { return bias; }
    public double getPrimaryValue() { return primaryValue; }
    public double getSecondaryValue() { return secondaryValue; }
}