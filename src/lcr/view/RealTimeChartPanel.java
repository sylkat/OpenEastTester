package lcr.view;

import lcr.beans.MeasurementDTO;
import lcr.business.CSVRecordingRunnable;
import lcr.util.Constants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
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

public class RealTimeChartPanel extends JPanel implements AxisChangeListener {

    // ---------------------------------------------------------------
    // Color Palette matching DerivedPanel / InfoPanel
    // ---------------------------------------------------------------
    private static final Color BG_PANEL       = new Color(17, 24, 39);
    private static final Color BG_PLOT        = new Color(24, 32, 48);
    private static final Color BORDER_COLOR   = new Color(55, 65, 81);
    private static final Color TEXT_LABEL     = new Color(156, 163, 175);
    private static final Color TEXT_PRIMARY   = new Color(243, 244, 246);

    private static final Color ACCENT_1       = new Color(255, 170, 0);   // Yellow / Orange default
    private static final Color ACCENT_2       = new Color(56, 189, 248);
    private static final Color ACCENT_GREEN   = new Color(52, 211, 153);
    private static final Color ACCENT_RED     = new Color(239, 68, 68);    // TailWind Red 500 for Recording Stop state
    private static final Color BG_CARD_BUTTON = new Color(31, 41, 55);

    private static final Font FONT_AXIS  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_UI    = new Font("Segoe UI", Font.PLAIN, 12);

    private TimeSeries series1;
    private TimeSeries series2;
    private TimeSeriesCollection dataset;
    private NumberAxis yAxis;
    private DateAxis xAxis;
    private XYLineAndShapeRenderer renderer;
    private JCheckBox chkSeries1;
    private JCheckBox chkSeries2;

    // --- RECORD STATE & THREAD CONTEXT ---
    private boolean isRecording = false;
    private JButton btnRecord;
    private BlockingQueue<String[]> csvQueue;
    private Thread recordingThread;
    private CSVRecordingRunnable csvRunnable;

    // --- PAUSE STATE ---
    private volatile boolean isPaused = false;
    private JButton btnPause;

    // --- TIME FORMAT STATE ---
    private boolean use24HourFormat = true;
    private JButton btnTimeFormat;

    // --- PRECISION CONFIGURATION ---
    private JComboBox<String> cmbPrecision;

    // --- SCROLLBAR & SLIDING WINDOW CONFIGURATION ---
    private JScrollBar scrollBar;
    private boolean isUpdatingScroll = false;

    private static final long LIVE_WINDOW_MS = 30000;
    private boolean isUserScrolling = false;

    public RealTimeChartPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);

        initComponents();
        JFreeChart chart = createChart();
        JToolBar toolBar = initToolBar();
        initScrollBar();

        ChartPanel chartPanel = new ChartPanel(
                chart, 400, 200, 0, 0, 30000, 30000,
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

        dataset = new TimeSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
    }

    private JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null, "Time", "Value", dataset, true, true, false
        );

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

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_PLOT);
        plot.setDomainGridlinePaint(BORDER_COLOR);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.setDomainCrosshairPaint(ACCENT_1);
        plot.setRangeCrosshairPaint(ACCENT_1);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ACCENT_1);
        renderer.setSeriesPaint(1, ACCENT_2);
        renderer.setSeriesStroke(0, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesStroke(1, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setDefaultShapesFilled(true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5, 5));
        renderer.setSeriesShape(1, new Ellipse2D.Double(-2.5, -2.5, 5, 5));

        yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(false);
        styleAxis(yAxis);

        if (plot.getDomainAxis() instanceof NumberAxis) {
            styleAxis((NumberAxis) plot.getDomainAxis());
        } else {
            plot.getDomainAxis().setLabelFont(FONT_AXIS);
            plot.getDomainAxis().setLabelPaint(TEXT_LABEL);
            plot.getDomainAxis().setTickLabelFont(FONT_AXIS);
            plot.getDomainAxis().setTickLabelPaint(TEXT_LABEL);
            plot.getDomainAxis().setAxisLinePaint(BORDER_COLOR);
            plot.getDomainAxis().setTickMarkPaint(BORDER_COLOR);
        }

        xAxis = (DateAxis) plot.getDomainAxis();
        xAxis.setAutoRange(false);
        updateTimeFormat();
        xAxis.addChangeListener(this);

        return chart;
    }

    private JToolBar initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BG_PANEL);
        toolBar.setBorder(new EmptyBorder(8, 10, 8, 10));

        // --- RECORD BUTTON ---
        btnRecord = new JButton("Record");
        styleButton(btnRecord);
        btnRecord.setForeground(ACCENT_1);
        btnRecord.setToolTipText("Start logging chart data or export captured frames");
        btnRecord.addActionListener(e -> handleRecordToggle());
        toolBar.add(btnRecord);

        toolBar.addSeparator(new Dimension(8, 0));

        // Pause / Resume button
        btnPause = new JButton("Pause");
        styleButton(btnPause);
        btnPause.setForeground(ACCENT_GREEN);
        btnPause.setToolTipText("Stop feeding incoming measurements to the graph");
        btnPause.addActionListener(e -> togglePause());
        toolBar.add(btnPause);

        toolBar.addSeparator(new Dimension(8, 0));

        // Reset button
        JButton btnReset = new JButton("Reset Plot");
        styleButton(btnReset);
        btnReset.setToolTipText("Clears trace history and resets scale bounds instantly");
        btnReset.addActionListener(e -> clearChart());
        toolBar.add(btnReset);

        toolBar.addSeparator(new Dimension(8, 0));

        // Time Format Toggle button
        btnTimeFormat = new JButton("Format: 24H");
        styleButton(btnTimeFormat);
        btnTimeFormat.setToolTipText("Toggle timeline format between 12-hour and 24-hour mode");
        btnTimeFormat.addActionListener(e -> toggleTimeFormat());
        toolBar.add(btnTimeFormat);

        toolBar.addSeparator(new Dimension(12, 0));

        // Precision ComboBox Selection
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

        // Primary Parameter Checkbox
        chkSeries1 = new JCheckBox("Show Primary", true);
        styleCheckbox(chkSeries1, ACCENT_1);
        chkSeries1.addActionListener(e -> {
            renderer.setSeriesVisible(0, chkSeries1.isSelected());
            adjustSymmetricRange();
        });
        toolBar.add(chkSeries1);

        toolBar.addSeparator(new Dimension(14, 0));

        // Secondary Parameter Checkbox
        chkSeries2 = new JCheckBox("Show Secondary", true);
        styleCheckbox(chkSeries2, ACCENT_2);
        chkSeries2.addActionListener(e -> {
            renderer.setSeriesVisible(1, chkSeries2.isSelected());
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

            // Define filters clearly from the start
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

                // --- INITIALIZE CSV BACKGROUND WORKING THREAD ---
                if (fileChooser.getFileFilter() == csvFilter) {
                    csvQueue = new LinkedBlockingQueue<>();
                    csvRunnable = new CSVRecordingRunnable(targetFile, csvQueue);

                    recordingThread = new Thread(csvRunnable, "LCR-CSV-Recorder-Thread");
                    recordingThread.setPriority(Thread.MIN_PRIORITY);

                    isRecording = true; // Engage data collection safety flag
                    recordingThread.start();
                } else {
                    // Placeholder logic for TDMS / PNG hooks later
                    isRecording = true;
                }

                btnRecord.setText("Stop");
                btnRecord.setForeground(ACCENT_RED);
                btnRecord.setToolTipText("Halt ongoing measurements output pipeline streaming processes");
            }
        } else {
            // Halt collection steps safely
            isRecording = false;

            // Shut down business logic runnable if CSV was executed
            if (csvRunnable != null) {
                csvRunnable.stopRecording();
            }

            if (recordingThread != null) {
                try {
                    // Join worker thread to allow structural drainage to conclude securely
                    recordingThread.join(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                recordingThread = null;
                csvRunnable = null;
            }

            csvQueue = null; // Flush referencing contexts

            btnRecord.setText("Record");
            btnRecord.setForeground(ACCENT_1);
            btnRecord.setToolTipText("Start logging chart data or export captured frames");

            JOptionPane.showMessageDialog(this,
                    "Export completed successfully.", "Recording Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateYAxisPrecision() {
        if (yAxis == null || cmbPrecision == null) return;
        int decimals = cmbPrecision.getSelectedIndex() + 2;

        StringBuilder pattern = new StringBuilder("0.");
        for (int i = 0; i < decimals; i++) {
            pattern.append("0");
        }
        yAxis.setNumberFormatOverride(new DecimalFormat(pattern.toString()));
    }

    // ---------------------------------------------------------------
    // UI Helper and Styling Methods
    // ---------------------------------------------------------------
    private void styleAxis(NumberAxis axis) {
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
        if (use24HourFormat) {
            btnTimeFormat.setText("Format: 24H");
        } else {
            btnTimeFormat.setText("Format: 12H");
        }
        updateTimeFormat();
    }

    private void updateTimeFormat() {
        if (xAxis != null) {
            if (use24HourFormat) {
                xAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
            } else {
                xAxis.setDateFormatOverride(new SimpleDateFormat("hh:mm:ss a"));
            }
        }
    }

    // ---------------------------------------------------------------
    // Data Management Methods
    // ---------------------------------------------------------------
    public void updateData(MeasurementDTO dto) {
        if (isPaused) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Millisecond now = new Millisecond();

            if (dto.getTypeA() != null && !dto.getTypeA().isEmpty()) {
                series1.setKey(dto.getTypeA());
                chkSeries1.setText("Show " + dto.getTypeA());
            }
            if (dto.getTypeB() != null && !dto.getTypeB().isEmpty()) {
                series2.setKey(dto.getTypeB());
                chkSeries2.setText("Show " + dto.getTypeB());
            }

            series1.setMaximumItemCount(150);
            series2.setMaximumItemCount(150);
            double valueA = Double.parseDouble(dto.getRealValueA());
            double valueB = Double.parseDouble(dto.getRealValueB());
            if(dto.getTypeA().equals(Constants.LABEL_RESISTANCE)
                    || dto.getTypeA().equals(Constants.LABEL_IMPEDANCE )){
                valueA = Math.abs(valueA);
            }
            series1.addOrUpdate(now, valueA);
            series2.addOrUpdate(now, valueB);

            adjustSymmetricRange();

            // --- INJECT DATA POINT INTO THE STREAMING CONTEXT ---
            // --- INJECT DATA POINT INTO THE STREAMING CONTEXT ---
            if (isRecording && csvQueue != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                // Use now.getStart() to retrieve the standard java.util.Date object cleanly
                String timestampStr = sdf.format(now.getStart());

                String[] dataRow = new String[]{
                        timestampStr,
                        String.valueOf(valueA),
                        String.valueOf(valueB)
                };
                // Offer data row safely without blocking the main Swing EDT thread
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
        double maxAbs = 0.0;

        if (renderer.isSeriesVisible(0)) {
            for (int i = 0; i < series1.getItemCount(); i++) {
                double val = series1.getDataItem(i).getValue().doubleValue();
                maxAbs = Math.max(maxAbs, Math.abs(val));
            }
        }

        if (renderer.isSeriesVisible(1)) {
            for (int i = 0; i < series2.getItemCount(); i++) {
                double val = series2.getDataItem(i).getValue().doubleValue();
                maxAbs = Math.max(maxAbs, Math.abs(val));
            }
        }

        if (maxAbs == 0.0) {
            maxAbs = 1e-12;
        }

        double limit = maxAbs * 1.1;
        yAxis.setRange(-limit, limit);
    }

    public void clearChart() {
        SwingUtilities.invokeLater(() -> {
            series1.clear();
            series2.clear();
            yAxis.setRange(-1e-12, 1e-12);
            isUserScrolling = false;
        });
    }

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
            case 'm':           multiplier = 1e-3;  break;
            case 'k': case 'K': multiplier = 1e3;   break;
            case 'M':           multiplier = 1e6;   break;
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

    public void setRecording(boolean recording) {
        this.isRecording = recording;
    }

    public boolean isPaused() {
        return this.isPaused;
    }
}