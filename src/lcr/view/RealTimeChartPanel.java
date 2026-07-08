package lcr.view;

import lcr.beans.MeasurementDTO;
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
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class RealTimeChartPanel extends JPanel implements AxisChangeListener {

    // ---------------------------------------------------------------
    // Color Palette matching DerivedPanel / InfoPanel
    // ---------------------------------------------------------------
    private static final Color BG_PANEL       = new Color(17, 24, 39);     // Deep Dark Slate (Base Background)
    private static final Color BG_PLOT        = new Color(24, 32, 48);     // Subtle intermediate step for plot background
    private static final Color BORDER_COLOR   = new Color(55, 65, 81);     // Separator color for button borders / grid lines
    private static final Color TEXT_LABEL     = new Color(156, 163, 175);  // Muted Gray for secondary labels
    private static final Color TEXT_PRIMARY   = new Color(243, 244, 246);  // Clean White for primary text

    private static final Color ACCENT_1       = new Color(255, 170, 0);
    private static final Color ACCENT_2       = new Color(56, 189, 248);
    private static final Color ACCENT_GREEN   = new Color(52, 211, 153);   // Emerald Green for "Pause" state
    private static final Color BG_CARD_BUTTON = new Color(31, 41, 55);     // Card gray tone for blending buttons

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
    private boolean isRecording = false;

    // --- PAUSE STATE ---
    private volatile boolean isPaused = false;
    private JButton btnPause;

    // --- TIME FORMAT STATE ---
    private boolean use24HourFormat = true;
    private JButton btnTimeFormat;

    // --- SCROLLBAR COMPONENTS ---
    private JScrollBar scrollBar;
    private boolean isUpdatingScroll = false; // Flag to prevent infinite event loops

    public RealTimeChartPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);

        initComponents();
        JFreeChart chart = createChart();
        JToolBar toolBar = initToolBar();
        initScrollBar();

        // Wrap the chart inside a specialized container panel
        ChartPanel chartPanel = new ChartPanel(
                chart,
                400, 200,
                0, 0,
                3000, 3000,
                true, true, true, true, true, true
        );

        chartPanel.setBackground(BG_PANEL);
        chartPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(false); // Kept vertical auto-range logic locked against accidental mouse wheel adjustments

        // South container to add a clean margin to the bottom bar
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(BG_PANEL);
        southPanel.setBorder(new EmptyBorder(2, 40, 8, 20)); // Subtle alignment matching the internal plot area
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

        // General chart styling
        chart.setBackgroundPaint(BG_PANEL);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(0, 12, 0, 0));

        // Legend styling
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setBackgroundPaint(BG_PANEL);
            legend.setItemPaint(TEXT_LABEL);
            legend.setItemFont(FONT_UI);
            legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
        }

        // Plot rendering optimization
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_PLOT);
        plot.setDomainGridlinePaint(BORDER_COLOR);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.setDomainCrosshairPaint(ACCENT_1);
        plot.setRangeCrosshairPaint(ACCENT_1);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        // Line renderer setup
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

        // Y-Axis configuration
        yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(false);
        yAxis.setNumberFormatOverride(new DecimalFormat("#.##"));
        styleAxis(yAxis);

        // X-Axis configuration
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
        updateTimeFormat();
        xAxis.addChangeListener(this); // Listen for timeline view changes (such as mouse wheel zooms)

        return chart;
    }

    private JToolBar initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BG_PANEL);
        toolBar.setBorder(new EmptyBorder(8, 10, 8, 10));

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

        // Scrollbar adjustment listener: updates the chart view bounds when dragging
        scrollBar.addAdjustmentListener(e -> {
            if (isUpdatingScroll || isPaused) return;

            double minX = series1.getItemCount() > 0 ? series1.getTimePeriod(0).getFirstMillisecond() : 0;
            double maxX = series1.getItemCount() > 0 ? series1.getTimePeriod(series1.getItemCount() - 1).getLastMillisecond() : 0;

            if (maxX - minX <= 0) return;

            long currentWidth = (long) (xAxis.getUpperBound() - xAxis.getLowerBound());
            int value = scrollBar.getValue();

            long newLower = (long) (minX + ((maxX - minX - currentWidth) * value / 100.0));
            long newUpper = newLower + currentWidth;

            isUpdatingScroll = true;
            xAxis.setRange(newLower, newUpper);
            isUpdatingScroll = false;
        });
    }

    // Triggered automatically when mouse zooms alter the plot domain range
    @Override
    public void axisChanged(AxisChangeEvent event) {
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
            // Disable scrollbar if the chart fits the total history inside the visible area
            scrollBar.setValues(0, 100, 0, 100);
            scrollBar.setEnabled(false);
        } else {
            scrollBar.setEnabled(true);
            int extent = (int) ((currentWidth / totalRange) * 100);
            int value = (int) (((currentMin - minX) / (totalRange - currentWidth)) * (100 - extent));

            // Constrain thumb values within boundaries to prevent layout hitching
            value = Math.max(0, Math.min(value, 100 - extent));
            scrollBar.setValues(value, extent, 0, 100);
        }

        isUpdatingScroll = false;
    }

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