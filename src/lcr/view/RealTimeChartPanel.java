package lcr.view;

import lcr.beans.MeasurementDTO;
import lcr.business.CSVRecordingRunnable;
import lcr.controller.MeterController;
import lcr.util.Constants;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Custom graphical presentation panel managing real-time dynamic dashboard feeds
 * for live telemetry data streams using asymmetric synchronized charting layers.
 * * @author sylkat
 */
public class RealTimeChartPanel extends JPanel implements AxisChangeListener {

    private static final Color BG_PANEL       = new Color(17, 24, 39);
    private static final Color BG_PLOT        = new Color(24, 32, 48);
    private static final Color BORDER_COLOR   = new Color(55, 65, 81);
    private static final Color TEXT_LABEL     = new Color(156, 163, 175);
    private static final Color TEXT_PRIMARY   = new Color(243, 244, 246);

    private static final Color ACCENT_1       = new Color(255, 170, 0);
    private static final Color ACCENT_2       = new Color(56, 189, 248);
    private static final Color ACCENT_GREEN   = new Color(52, 211, 153);
    private static final Color ACCENT_RED     = new Color(239, 68, 68);
    private static final Color BG_CARD_BUTTON = new Color(31, 41, 55);

    private static final Font FONT_AXIS  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_UI    = new Font("Segoe UI", Font.PLAIN, 12);

    private TimeSeries series1;
    private TimeSeries series2;

    private TimeSeriesCollection dataset1;
    private TimeSeriesCollection dataset2;

    private NumberAxis yAxis1;
    private NumberAxis yAxis2;

    private DateAxis xAxis;

    private XYLineAndShapeRenderer renderer1;
    private XYLineAndShapeRenderer renderer2;

    private JCheckBox chkSeries1;
    private JCheckBox chkSeries2;

    private XYPlot subplot1;
    private XYPlot subplot2;

    private boolean isRecording = false;
    private JButton btnRecord;
    private BlockingQueue<String[]> csvQueue;
    private Thread recordingThread;
    private CSVRecordingRunnable csvRunnable;

    private volatile boolean isPaused = false;
    private JButton btnPause;
    private JButton btnBiasSweep;

    private boolean use24HourFormat = true;
    private JButton btnTimeFormat;

    private JComboBox<String> cmbPrecision;
    private JScrollBar scrollBar;
    private boolean isUpdatingScroll = false;

    private static final long LIVE_WINDOW_MS = 30000;
    private boolean isUserScrolling = false;

    /**
     * Active bridge instance driving business layer translations.
     */
    public final MeterController meterController;

    /**
     * Constructs the real-time runtime graphing canvas panel linking execution controls.
     * * @param meterController the controller driving system message relays
     */
    public RealTimeChartPanel(MeterController meterController) {
        this.meterController = meterController;
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);

        initComponents();
        JFreeChart chart = createChart();
        JToolBar toolBar = initToolBar();
        initScrollBar();

        ChartPanel chartPanel = new ChartPanel(
                chart, 400, 400, 0, 0, 30000, 30000,
                true, true, true, true, true, true
        );

        chartPanel.setBackground(BG_PANEL);
        chartPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(false);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(BG_PANEL);
        southPanel.setBorder(new EmptyBorder(2, 40, 8, 20));
        southPanel.add(scrollBar, BorderLayout.CENTER);

        add(toolBar, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void initComponents() {
        series1 = new TimeSeries("Primary Parameter");
        series1.setMaximumItemCount(150);

        series2 = new TimeSeries("Secondary Parameter");
        series2.setMaximumItemCount(150);

        dataset1 = new TimeSeriesCollection(series1);
        dataset2 = new TimeSeriesCollection(series2);
    }

    private JFreeChart createChart() {
        xAxis = new DateAxis("Time");
        xAxis.setAutoRange(false);
        styleAxis(xAxis);
        updateTimeFormat();
        xAxis.addChangeListener(this);

        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(xAxis);
        combinedPlot.setGap(15.0);
        combinedPlot.setBackgroundPaint(BG_PANEL);
        combinedPlot.setOutlineVisible(false);

        yAxis1 = new NumberAxis("");
        yAxis1.setAutoRange(false);
        styleAxis(yAxis1);

        renderer1 = new XYLineAndShapeRenderer(true, false);
        renderer1.setSeriesPaint(0, ACCENT_1);
        renderer1.setSeriesStroke(0, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer1.setDefaultShapesFilled(true);
        renderer1.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5, 5));

        subplot1 = new XYPlot(dataset1, null, yAxis1, renderer1);
        styleSubplot(subplot1);
        combinedPlot.add(subplot1, 1);

        yAxis2 = new NumberAxis("");
        yAxis2.setAutoRange(false);
        styleAxis(yAxis2);

        renderer2 = new XYLineAndShapeRenderer(true, false);
        renderer2.setSeriesPaint(0, ACCENT_2);
        renderer2.setSeriesStroke(0, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer2.setDefaultShapesFilled(true);
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5, 5));

        subplot2 = new XYPlot(dataset2, null, yAxis2, renderer2);
        styleSubplot(subplot2);
        combinedPlot.add(subplot2, 1);

        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        chart.setBackgroundPaint(BG_PANEL);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(0, 12, 0, 0));

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setBackgroundPaint(BG_PANEL);
            legend.setItemPaint(TEXT_LABEL);
            legend.setItemFont(FONT_UI);
            legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
        }

        return chart;
    }

    private void styleSubplot(XYPlot plot) {
        plot.setBackgroundPaint(BG_PLOT);
        plot.setDomainGridlinePaint(BORDER_COLOR);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.setDomainCrosshairPaint(ACCENT_1);
        plot.setRangeCrosshairPaint(ACCENT_1);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
    }

    private JToolBar initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BG_PANEL);
        toolBar.setBorder(new EmptyBorder(8, 10, 8, 10));

        btnRecord = new JButton("Record");
        styleButton(btnRecord);
        btnRecord.setForeground(ACCENT_1);
        btnRecord.setToolTipText("Start logging chart data or export captured frames");
        btnRecord.addActionListener(e -> handleRecordToggle());
        toolBar.add(btnRecord);

        toolBar.addSeparator(new Dimension(8, 0));

        btnPause = new JButton("Pause");
        styleButton(btnPause);
        btnPause.setForeground(ACCENT_GREEN);
        btnPause.setToolTipText("Stop feeding incoming measurements to the graph");
        btnPause.addActionListener(e -> togglePause());
        toolBar.add(btnPause);

        toolBar.addSeparator(new Dimension(8, 0));

        JButton btnReset = new JButton("Reset Plot");
        styleButton(btnReset);
        btnReset.setToolTipText("Clears trace history and resets scale bounds instantly");
        btnReset.addActionListener(e -> clearChart());
        toolBar.add(btnReset);

        toolBar.addSeparator(new Dimension(8, 0));

        btnBiasSweep = new JButton("Bias C-V");
        styleButton(btnBiasSweep);
        btnBiasSweep.setToolTipText("Execute continuous Bias Voltage Sweep analysis profiles");
        btnBiasSweep.addActionListener(e -> {
            Window ancestor = SwingUtilities.getWindowAncestor(this);
            BiasSweepDialog dialog = new BiasSweepDialog(ancestor, this.meterController.meterBusiness.meter.getSerialConnection(), this);
            dialog.setVisible(true);
        });
        toolBar.add(btnBiasSweep);

        toolBar.addSeparator(new Dimension(8, 0));

        btnTimeFormat = new JButton("Format: 24H");
        styleButton(btnTimeFormat);
        btnTimeFormat.setToolTipText("Toggle timeline format between 12-hour and 24-hour mode");
        btnTimeFormat.addActionListener(e -> toggleTimeFormat());
        toolBar.add(btnTimeFormat);

        toolBar.addSeparator(new Dimension(12, 0));

        JLabel lblPrecision = new JLabel("Precision: ");
        lblPrecision.setFont(FONT_LABEL);
        lblPrecision.setForeground(TEXT_LABEL);
        toolBar.add(lblPrecision);

        String[] precisionOptions = new String[19];
        for (int i = 0; i < precisionOptions.length; i++) {
            precisionOptions[i] = (i + 2) + " Decimals";
        }
        cmbPrecision = new JComboBox<>(precisionOptions);
        cmbPrecision.setSelectedIndex(10);
        styleComboBox(cmbPrecision);
        cmbPrecision.addActionListener(e -> updateYAxisPrecision());
        toolBar.add(cmbPrecision);

        updateYAxisPrecision();

        toolBar.addSeparator(new Dimension(16, 0));

        chkSeries1 = new JCheckBox("Show Primary", true);
        styleCheckbox(chkSeries1, ACCENT_1);
        chkSeries1.addActionListener(e -> {
            renderer1.setSeriesVisible(0, chkSeries1.isSelected());
            adjustSymmetricRange();
        });
        toolBar.add(chkSeries1);

        toolBar.addSeparator(new Dimension(14, 0));

        chkSeries2 = new JCheckBox("Show Secondary", true);
        styleCheckbox(chkSeries2, ACCENT_2);
        chkSeries2.addActionListener(e -> {
            renderer2.setSeriesVisible(0, chkSeries2.isSelected());
            adjustSymmetricRange();
        });
        toolBar.add(chkSeries2);

        return toolBar;
    }

    private void initScrollBar() {
        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 100, 0, 100);
        scrollBar.setBackground(BG_PANEL);
        scrollBar.setForeground(BG_CARD_BUTTON);
        scrollBar.setOpaque(true);

        scrollBar.addAdjustmentListener(e -> {
            if (isUpdatingScroll || isPaused || series1.getItemCount() == 0) return;

            double minX = series1.getTimePeriod(0).getFirstMillisecond();
            double maxX = series1.getTimePeriod(series1.getItemCount() - 1).getLastMillisecond();

            if (maxX - minX <= 0) return;

            long currentWidth = (long) (xAxis.getUpperBound() - xAxis.getLowerBound());
            int value = scrollBar.getValue();

            long newLower = (long) (minX + ((maxX - minX - currentWidth) * value / 100.0));
            long newUpper = newLower + currentWidth;

            isUserScrolling = (value < (100 - scrollBar.getModel().getExtent() - 1));

            isUpdatingScroll = true;
            xAxis.setRange(newLower, newUpper);
            isUpdatingScroll = false;
        });
    }

    @Override
    public void axisChanged(AxisChangeEvent event) {
        updateScrollbarState();
    }

    private void updateScrollbarState() {
        if (isUpdatingScroll || scrollBar == null || series1 == null || series1.getItemCount() == 0) {
            return;
        }

        double minX = series1.getTimePeriod(0).getFirstMillisecond();
        double maxX = series1.getTimePeriod(series1.getItemCount() - 1).getLastMillisecond();
        double totalRange = maxX - minX;

        if (totalRange <= 0) return;

        double currentMin = xAxis.getLowerBound();
        double currentMax = xAxis.getUpperBound();
        double currentWidth = currentMax - currentMin;

        isUpdatingScroll = true;

        if (currentWidth >= totalRange) {
            scrollBar.setValues(0, 100, 0, 100);
            scrollBar.setEnabled(false);
        } else {
            scrollBar.setEnabled(true);
            int extent = (int) ((currentWidth / totalRange) * 100);
            int value = (int) (((currentMin - minX) / (totalRange - currentWidth)) * (100 - extent));

            value = Math.max(0, Math.min(value, 100 - extent));
            scrollBar.setValues(value, extent, 0, 100);
        }

        isUpdatingScroll = false;
    }

    private void handleRecordToggle() {
        if (!isRecording) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Export Path and Format");
            fileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Data Table (*.csv)", "csv");
            fileChooser.addChoosableFileFilter(csvFilter);
            fileChooser.setFileFilter(csvFilter);

            JPanel accessoryPanel = new JPanel(new GridBagLayout());
            accessoryPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR), "Export Format",
                    0, 0, FONT_LABEL, TEXT_LABEL));
            accessoryPanel.setBackground(BG_PLOT);

            String[] formats = {"CSV (Data Table)", "TDMS (LabVIEW Binary)", "PNG (Chart Image)"};
            JComboBox<String> cmbFormat = new JComboBox<>(formats);
            styleComboBox(cmbFormat);
            cmbFormat.setMaximumSize(new Dimension(160, 26));
            accessoryPanel.add(cmbFormat);
            fileChooser.setAccessory(accessoryPanel);

            cmbFormat.addActionListener(e -> {
                int selection = cmbFormat.getSelectedIndex();
                if (selection == 0) fileChooser.setFileFilter(csvFilter);
            });

            fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, evt -> {
                Object newFilter = evt.getNewValue();
                if (newFilter == csvFilter) cmbFormat.setSelectedIndex(0);
            });

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File targetFile = fileChooser.getSelectedFile();
                String ext = "";
                if (fileChooser.getFileFilter() == csvFilter) ext = ".csv";

                if (!targetFile.getName().toLowerCase().endsWith(ext)) {
                    targetFile = new File(targetFile.getAbsolutePath() + ext);
                }

                if (fileChooser.getFileFilter() == csvFilter) {
                    csvQueue = new LinkedBlockingQueue<>();
                    csvRunnable = new CSVRecordingRunnable(targetFile, csvQueue);

                    recordingThread = new Thread(csvRunnable, "LCR-CSV-Recorder-Thread");
                    recordingThread.setPriority(Thread.MIN_PRIORITY);

                    isRecording = true;
                    recordingThread.start();
                } else {
                    isRecording = true;
                }

                btnRecord.setText("Stop");
                btnRecord.setForeground(ACCENT_RED);
                btnRecord.setToolTipText("Halt ongoing measurements output pipeline streaming processes");
            }
        } else {
            isRecording = false;

            if (csvRunnable != null) {
                csvRunnable.stopRecording();
            }

            if (recordingThread != null) {
                try {
                    recordingThread.join(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                recordingThread = null;
                csvRunnable = null;
            }

            csvQueue = null;

            btnRecord.setText("Record");
            btnRecord.setForeground(ACCENT_1);
            btnRecord.setToolTipText("Start logging chart data or export captured frames");

            JOptionPane.showMessageDialog(this,
                    "Export completed successfully.", "Recording Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateYAxisPrecision() {
        if (yAxis1 == null || yAxis2 == null || cmbPrecision == null) return;
        int decimals = cmbPrecision.getSelectedIndex() + 2;

        StringBuilder pattern = new StringBuilder("0.");
        for (int i = 0; i < decimals; i++) {
            pattern.append("0");
        }
        DecimalFormat format = new DecimalFormat(pattern.toString());
        yAxis1.setNumberFormatOverride(format);
        yAxis2.setNumberFormatOverride(format);
    }

    private void styleAxis(org.jfree.chart.axis.Axis axis) {
        axis.setLabelFont(FONT_AXIS);
        axis.setLabelPaint(TEXT_LABEL);
        axis.setTickLabelFont(FONT_AXIS);
        axis.setTickLabelPaint(TEXT_LABEL);
        axis.setAxisLinePaint(BORDER_COLOR);
        axis.setTickMarkPaint(BORDER_COLOR);
    }

    private void styleButton(JButton btn) {
        btn.setFocusable(false);
        btn.setFont(FONT_LABEL);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_CARD_BUTTON);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFocusable(false);
        combo.setFont(FONT_UI);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(BG_CARD_BUTTON);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        combo.setMaximumSize(new Dimension(110, 26));
    }

    private void styleCheckbox(JCheckBox chk, Color accent) {
        chk.setFocusable(false);
        chk.setFont(FONT_UI);
        chk.setForeground(TEXT_PRIMARY);
        chk.setOpaque(false);
        chk.setIconTextGap(8);
        chk.setIcon(coloredDotIcon(BG_CARD_BUTTON));
        chk.setSelectedIcon(coloredDotIcon(accent));
    }

    private Icon coloredDotIcon(Color color) {
        int size = 14;
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(x, y, size, size, 4, 4);
                g2.setColor(color);
                g2.fillOval(x + 3, y + 3, size - 6, size - 6);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(x, y, size - 1, size - 1, 4, 4);
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            btnPause.setText("Resume");
            btnPause.setForeground(ACCENT_1);
        } else {
            btnPause.setText("Pause");
            btnPause.setForeground(ACCENT_GREEN);
        }
    }

    private void toggleTimeFormat() {
        use24HourFormat = !use24HourFormat;
        btnTimeFormat.setText(use24HourFormat ? "Format: 24H" : "Format: 12H");
        updateTimeFormat();
    }

    private void updateTimeFormat() {
        if (xAxis != null) {
            xAxis.setDateFormatOverride(use24HourFormat ? new SimpleDateFormat("HH:mm:ss") : new SimpleDateFormat("hh:mm:ss a"));
        }
    }

    /**
     * Appends parsed LCR component data streams directly into active visual line traces.
     * * @param dto the tracking data object payload containing incoming hardware measurements
     */
    public void updateData(MeasurementDTO dto) {
        if (isPaused) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Millisecond now = new Millisecond();

            if (dto.getTypeA() != null && !dto.getTypeA().isEmpty()) {
                series1.setKey(dto.getTypeA());
                chkSeries1.setText("Show " + dto.getTypeA());
                yAxis1.setLabel(dto.getTypeA());
            }
            if (dto.getTypeB() != null && !dto.getTypeB().isEmpty()) {
                series2.setKey(dto.getTypeB());
                chkSeries2.setText("Show " + dto.getTypeB());
                yAxis2.setLabel(dto.getTypeB());
            }

            series1.setMaximumItemCount(150);
            series2.setMaximumItemCount(150);

            double valueA = Double.parseDouble(dto.getRealValueA());
            double valueB = Double.parseDouble(dto.getRealValueB());

            if (Constants.LABEL_RESISTANCE.equals(dto.getTypeA()) || Constants.LABEL_IMPEDANCE.equals(dto.getTypeA())) {
                valueA = Math.abs(valueA);
            }

            series1.addOrUpdate(now, valueA);
            series2.addOrUpdate(now, valueB);

            adjustSymmetricRange();

            if (isRecording && csvQueue != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String timestampStr = sdf.format(now.getStart());

                String[] dataRow = new String[]{
                        timestampStr,
                        String.valueOf(valueA),
                        String.valueOf(valueB)
                };
                csvQueue.offer(dataRow);
            }

            long latestTime = now.getLastMillisecond();
            long firstTime = series1.getTimePeriod(0).getFirstMillisecond();
            long totalDataSpan = latestTime - firstTime;

            if (!isUserScrolling) {
                if (totalDataSpan > LIVE_WINDOW_MS) {
                    xAxis.setRange(latestTime - LIVE_WINDOW_MS, latestTime);
                } else {
                    xAxis.setRange(firstTime, latestTime);
                }
            } else {
                updateScrollbarState();
            }
        });
    }

    private void adjustSymmetricRange() {
        double maxAbs1 = 0.0;
        if (renderer1.isSeriesVisible(0)) {
            for (int i = 0; i < series1.getItemCount(); i++) {
                double val = series1.getDataItem(i).getValue().doubleValue();
                maxAbs1 = Math.max(maxAbs1, Math.abs(val));
            }
        }
        if (maxAbs1 == 0.0) maxAbs1 = 1e-12;
        double limit1 = maxAbs1 * 1.1;
        yAxis1.setRange(-limit1, limit1);

        double maxAbs2 = 0.0;
        if (renderer2.isSeriesVisible(0)) {
            for (int i = 0; i < series2.getItemCount(); i++) {
                double val = series2.getDataItem(i).getValue().doubleValue();
                maxAbs2 = Math.max(maxAbs2, Math.abs(val));
            }
        }
        if (maxAbs2 == 0.0) maxAbs2 = 1e-12;
        double limit2 = maxAbs2 * 1.1;
        yAxis2.setRange(-limit2, limit2);
    }

    /**
     * Clears all tracking chart datasets and resets axes scale lines.
     */
    public void clearChart() {
        SwingUtilities.invokeLater(() -> {
            series1.clear();
            series2.clear();
            yAxis1.setRange(-1e-12, 1e-12);
            yAxis2.setRange(-1e-12, 1e-12);
            isUserScrolling = false;
        });
    }

    /**
     * Helper parser utility translating engineering multiplier tokens into raw numeric bounds.
     * * @param rawValue the raw string payload token to scale
     * @return the completely solved primitive value mapping factor
     */
    public static Double parseEngineeringValue(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0.0;
        }

        String clean = rawValue.trim().replaceAll("(?i)[fhoΩ]", "");
        if (clean.isEmpty()) return 0.0;

        char lastChar = clean.charAt(clean.length() - 1);
        double multiplier = 1.0;
        boolean hasPrefix = true;

        switch (lastChar) {
            case 'p': case 'P': multiplier = 1e-12; break;
            case 'n': case 'N': multiplier = 1e-9;  break;
            case 'u': case 'U': case 'μ': multiplier = 1e-6;  break;
            case 'm':                           multiplier = 1e-3;  break;
            case 'k': case 'K': multiplier = 1e3;   break;
            case 'M':                           multiplier = 1e6;   break;
            default:
                hasPrefix = false;
                break;
        }

        if (hasPrefix) {
            clean = clean.substring(0, clean.length() - 1).trim();
        }

        try {
            return Double.parseDouble(clean) * multiplier;
        } catch (NumberFormatException e) {
            if (Constants.DEBUG > 3) {
                System.err.println("Error parsing engineering value: " + rawValue);
            }
            return 0.0;
        }
    }

    /**
     * Configures the active background data logging state flag.
     * * @param recording true to activate background file writers
     */
    public void setRecording(boolean recording) {
        this.isRecording = recording;
    }

    /**
     * Gets the tracking pipeline paused conditional state context.
     * * @return true if drawing routines are locked down
     */
    public boolean isPaused() {
        return this.isPaused;
    }
}