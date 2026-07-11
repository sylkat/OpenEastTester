package lcr.view;

import lcr.beans.MeasurementDTO;
import lcr.business.BiasSweepWorker;
import lcr.business.SerialConnection;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class BiasSweepDialog extends JDialog {

    private static final Color BG_PANEL       = new Color(17, 24, 39);
    private static final Color BG_PLOT        = new Color(24, 32, 48);
    private static final Color BORDER_COLOR   = new Color(55, 65, 81);
    private static final Color TEXT_LABEL     = new Color(156, 163, 175);
    private static final Color TEXT_PRIMARY   = new Color(243, 244, 246);
    private static final Color ACCENT_CYAN    = new Color(56, 189, 248);
    private static final Color ACCENT_RED     = new Color(239, 68, 68);
    private static final Color BG_CARD_BUTTON = new Color(31, 41, 55);

    private static final Font FONT_AXIS  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);

    private XYSeries sweepSeries;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private BiasSweepWorker sweepWorker;
    private JProgressBar progressBar;
    private JButton btnCancel;

    public BiasSweepDialog(Window owner, SerialConnection serialPort, RealTimeChartPanel mainPanel) {
        super(owner, "Bias C-V Sweep Analysis", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_PANEL);
        setSize(700, 500);
        setLocationRelativeTo(owner);

        sweepSeries = new XYSeries("C-V Curve");
        sweepSeries.setMaximumItemCount(550);
        XYSeriesCollection dataset = new XYSeriesCollection(sweepSeries);

        JFreeChart chart = createXYChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(BG_PANEL);
        chartPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(BG_PANEL);
        bottomPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(ACCENT_CYAN);
        progressBar.setBackground(BG_CARD_BUTTON);
        progressBar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        btnCancel = new JButton("Abort Sweep");
        styleButton(btnCancel);
        btnCancel.setForeground(ACCENT_RED);
        btnCancel.addActionListener(e -> cancelSweep());

        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(btnCancel, BorderLayout.EAST);

        add(chartPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sweepWorker = new BiasSweepWorker(serialPort, mainPanel, this);
        sweepWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        sweepWorker.execute();
    }

    private JFreeChart createXYChart(XYSeriesCollection dataset) {
        xAxis = new NumberAxis("Bias Voltage (Steps 0 - 499)");
        yAxis = new NumberAxis("Capacitance / Primary Value");

        styleAxis(xAxis);
        styleAxis(yAxis);

        xAxis.setRange(0.0, 500.0);
        yAxis.setAutoRange(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, ACCENT_CYAN);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4, 4));

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(BG_PLOT);
        plot.setDomainGridlinePaint(BORDER_COLOR);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setBackgroundPaint(BG_PANEL);
        chart.setBorderVisible(false);

        return chart;
    }

    public void updateSweepGraph(double bias, double capacitance) {
        SwingUtilities.invokeLater(() -> {
            sweepSeries.addOrUpdate(bias, capacitance);
        });
    }

    private void cancelSweep() {
        if (sweepWorker != null && !sweepWorker.isDone()) {
            sweepWorker.cancel(true);
        }
        dispose();
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
        btn.setBackground(BG_CARD_BUTTON);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 30));
    }
}