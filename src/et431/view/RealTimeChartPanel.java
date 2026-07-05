package et431.view;

import et431.beans.Measurement;
import et431.beans.MeasurementDTO;
import et431.util.Constants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
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

public class RealTimeChartPanel extends JPanel {

    // ---------------------------------------------------------------
    // Paleta CLONADA exactamente de DerivedPanel / InfoPanel
    // ---------------------------------------------------------------
    private static final Color BG_PANEL       = new Color(17, 24, 39);     // Deep Dark Slate (Fondo Base)
    private static final Color BG_PLOT        = new Color(24, 32, 48);     // Un paso intermedio sutil para el fondo del plot
    private static final Color BORDER_COLOR   = new Color(55, 65, 81);     // Separador sutil para bordes de botones / líneas de grid
    private static final Color TEXT_LABEL     = new Color(156, 163, 175);  // Muted Gray para etiquetas secundarias
    private static final Color TEXT_PRIMARY   = new Color(243, 244, 246);  // Blanco limpio para textos principales

    private static final Color ACCENT_1       = new Color(56, 189, 248);   // Azul cian del ACCENT_COLOR de DerivedPanel
    private static final Color ACCENT_2       = new Color(236, 72, 153);   // Rosa/Magenta vibrante complementario para la línea 2
    private static final Color BG_CARD_BUTTON = new Color(31, 41, 55);     // Tono gris de tarjetas para botones estilo blend

    private static final Font FONT_AXIS  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_UI    = new Font("Segoe UI", Font.PLAIN, 12);

    private TimeSeries series1;
    private TimeSeries series2;
    private NumberAxis yAxis;
    XYLineAndShapeRenderer renderer;
    JCheckBox chkSeries1;
    JCheckBox chkSeries2;
    private boolean isRecording = false;

    public RealTimeChartPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);

        // 1. Instanciamos las dos series temporales
        series1 = new TimeSeries("Primary Parameter");
        series1.setMaximumItemCount(150);

        series2 = new TimeSeries("Secondary Parameter");
        series2.setMaximumItemCount(150);

        // 2. Añadimos las series a la colección
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        // 3. Creamos el gráfico pasándole null en el título
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Time",
                "Value",
                dataset,
                true,
                true,
                false
        );

        // --- ESTILO GENERAL DEL CHART ---
        chart.setBackgroundPaint(BG_PANEL);
        chart.setBorderVisible(false);
        chart.setPadding(new RectangleInsets(0, 12, 0, 0));

        // --- ESTILO DE LA LEYENDA ---
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setBackgroundPaint(BG_PANEL);
            legend.setItemPaint(TEXT_LABEL);
            legend.setItemFont(FONT_UI);
            legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
        }

        // Optimización de renderizado
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_PLOT);
        plot.setDomainGridlinePaint(BORDER_COLOR);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.setDomainCrosshairPaint(ACCENT_1);
        plot.setRangeCrosshairPaint(ACCENT_1);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        // Renderizador de líneas
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

        // --- CONFIGURACIÓN DEL EJE Y ---
        yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(false);

        DecimalFormat customFormat = new DecimalFormat("#,##0.000000000000");
        yAxis.setNumberFormatOverride(customFormat);
        styleAxis(yAxis);

        if (plot.getDomainAxis() instanceof NumberAxis) {
            NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
            styleAxis(xAxis);
        } else {
            plot.getDomainAxis().setLabelFont(FONT_AXIS);
            plot.getDomainAxis().setLabelPaint(TEXT_LABEL);
            plot.getDomainAxis().setTickLabelFont(FONT_AXIS);
            plot.getDomainAxis().setTickLabelPaint(TEXT_LABEL);
            plot.getDomainAxis().setAxisLinePaint(BORDER_COLOR);
            plot.getDomainAxis().setTickMarkPaint(BORDER_COLOR);
        }

        // 4. Creamos la barra de herramientas
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BG_PANEL);
        toolBar.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Botón Reset integrado a la paleta tecnológica
        JButton btnReset = new JButton("Reset Plot");
        btnReset.setFocusable(false);
        btnReset.setFont(FONT_LABEL);
        btnReset.setForeground(TEXT_PRIMARY);
        btnReset.setBackground(BG_CARD_BUTTON);
        btnReset.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        btnReset.setFocusPainted(false);
        btnReset.setContentAreaFilled(true);
        btnReset.setOpaque(true);
        btnReset.setToolTipText("Clears trace history and resets scale bounds instantly");
        btnReset.addActionListener(e -> clearChart());
        toolBar.add(btnReset);

        // Separador visual
        toolBar.addSeparator(new Dimension(16, 0));

        // Checkbox para Parámetro Primario
        chkSeries1 = new JCheckBox("Show Primary", true);
        styleCheckbox(chkSeries1, ACCENT_1);
        chkSeries1.addActionListener(e -> {
            renderer.setSeriesVisible(0, chkSeries1.isSelected());
            adjustSymmetricRange();
        });
        toolBar.add(chkSeries1);

        toolBar.addSeparator(new Dimension(14, 0));

        // Checkbox para Parámetro Secundario
        chkSeries2 = new JCheckBox("Show Secondary", true);
        styleCheckbox(chkSeries2, ACCENT_2);
        chkSeries2.addActionListener(e -> {
            renderer.setSeriesVisible(1, chkSeries2.isSelected());
            adjustSymmetricRange();
        });
        toolBar.add(chkSeries2);

        // 5. Envolvemos el gráfico en el panel contenedor
        ChartPanel chartPanel = new ChartPanel(
                chart,
                400, 200,
                0, 0,
                3000, 3000,
                true, true, true, true, true, true
        );

        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(BG_PANEL);
        chartPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        add(toolBar, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void styleAxis(NumberAxis axis) {
        axis.setLabelFont(FONT_AXIS);
        axis.setLabelPaint(TEXT_LABEL);
        axis.setTickLabelFont(FONT_AXIS);
        axis.setTickLabelPaint(TEXT_LABEL);
        axis.setAxisLinePaint(BORDER_COLOR);
        axis.setTickMarkPaint(BORDER_COLOR);
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
                g2.setColor(BG_PANEL); // Color base unificado
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

    public void updateData(MeasurementDTO dto) {
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

            series1.addOrUpdate(now, parseEngineeringValue(dto.getRealValueA()));
            series2.addOrUpdate(now, parseEngineeringValue(dto.getValueB()));

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

    public static double parseEngineeringValue(String rawValue) {
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
}