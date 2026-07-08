package gui;

import lcr.beans.MeasurementDTO;
import lcr.enums.DerivateCapacitance;
import lcr.enums.DerivateImpedance;
import lcr.enums.DerivateInductance;
import lcr.enums.DerivateResistance;
import lcr.util.*;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.Map; // Agregado para tipar explícitamente los mapas en Java 8

import static lcr.util.Constants.*;

/**
 * Dashboard panel that displays calculated derived parameters
 * in a 2x3 grid using the high-contrast, dark-tech aesthetic of InfoPanel.
 */
public class DerivedPanel extends JPanel {

    private JLabel[] valueLabels;
    private JLabel[] tagLabels;

    // --- HIGH-CONTRAST TECH PALETTE (Cloned from InfoPanel) ---
    private final Color BG_PANEL = new Color(17, 24, 39);         // Deep Dark Slate
   // private final Color TEXT_LABEL =new Color(56, 189, 248);
    private final Color TEXT_LABEL = new Color(156, 163, 175);
    private final Color COLOR_SECONDARY = new Color(255, 170, 0);  // Orange
    // Verde de Laboratorio de alta visibilidad (Bench Matrix Green)
    private final Color TEXT_VALUE = new Color(255, 170, 0);       // Hex: #22C55E (Vibrant Laboratory Green)

    private final Color BORDER_COLOR = new Color(55, 65, 81);      // Subtle dark separator divider

    public DerivedPanel() {
        initialize();
    }

    private static final Color ACCENT_COLOR = new Color(56, 189, 248);

    private void initialize() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(55, 65, 81)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));


        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 14, 12));
        gridPanel.setOpaque(false);

        valueLabels = new JLabel[6];
        tagLabels = new JLabel[6];

        for (int i = 0; i < 6; i++) {
            RoundedCardPanel block = new RoundedCardPanel(10, blend(BG_PANEL, Color.WHITE, 0.045f));
            block.setLayout(new GridLayout(2, 1, 0, 4));
            block.setBorder(BorderFactory.createEmptyBorder(9, 6, 9, 6));

            valueLabels[i] = new JLabel("-");
            valueLabels[i].setFont(new Font("Monospaced", Font.BOLD, 20));
            valueLabels[i].setForeground(TEXT_VALUE);
            valueLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            tagLabels[i] = new JLabel("---");
            tagLabels[i].setFont(new Font("SansSerif", Font.BOLD, 11));
            tagLabels[i].setForeground(TEXT_LABEL);
            tagLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            block.add(valueLabels[i]);
            block.add(tagLabels[i]);

            gridPanel.add(block);
        }

        add(gridPanel, BorderLayout.CENTER);
        clear();
    }

    private static Color blend(Color base, Color target, float ratio) {
        int r = (int) (base.getRed()   + (target.getRed()   - base.getRed())   * ratio);
        int g = (int) (base.getGreen() + (target.getGreen() - base.getGreen()) * ratio);
        int b = (int) (base.getBlue()  + (target.getBlue()  - base.getBlue())  * ratio);
        return new Color(r, g, b);
    }

    private static class RoundedCardPanel extends JPanel {
        private final int arc;
        private final Color bg;

        RoundedCardPanel(int arc, Color bg) {
            this.arc = arc;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(255, 255, 255, 12));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public void updateDisplay(MeasurementDTO dto) {
        if (dto == null) {
            clear();
            return;
        }

        final String[] labels;
        final String[] values = new String[6];
        //System.out.println("Second Primary type:"+ dto.getTypeB());
        switch (dto.getMeasureType()) {
            case "R":
                if(!dto.getTypeB().startsWith(LABEL_REACTANCE)){
                    labels = Constants.labelsAuto;
                    break;
                }
                labels = Constants.labelsResistance;
                if (dto.getResistanceDerivator() != null) {
                    Map<DerivateResistance, Double> map = dto.getResistanceDerivator();
                    values[0] = formatValue(map.get(DerivateResistance.IMPEDANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateResistance.PHASE_ANGLE), "°");
                    values[2] = formatValue(map.get(DerivateResistance.QUALITY_FACTOR), "");
                    values[3] = formatValue(map.get(DerivateResistance.LOSS_FACTOR), "");
                    values[4] = formatValue(map.get(DerivateResistance.PARASITIC_INDUCTANCE), "H");
                    values[5] = formatValue(map.get(DerivateResistance.PARASITIC_CAPACITANCE), "F");
                }
                break;

            case "C":
                if(!dto.getTypeB().startsWith(LABEL_LOSS_FACTOR)){
                    labels = Constants.labelsAuto;
                    break;
                }
                labels = Constants.labelsCapacitance;
                if (dto.getCapacitanceDerivator() != null) {
                    Map<DerivateCapacitance, Double> map = dto.getCapacitanceDerivator();
                    values[0] = formatValue(map.get(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateCapacitance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateCapacitance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateCapacitance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateCapacitance.QUALITY_FACTOR), "");
                    values[5] = "";
                }
                break;
            case "ECAP":
                if(!dto.getTypeB().startsWith(LABEL_LOSS_FACTOR)){
                    labels = Constants.labelsAuto;
                    break;
                }
                labels = Constants.labelsCapacitance;
                if (dto.getCapacitanceDerivator() != null) {
                    Map<DerivateCapacitance, Double> map = dto.getCapacitanceDerivator();
                    values[0] = formatValue(map.get(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateCapacitance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateCapacitance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateCapacitance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateCapacitance.QUALITY_FACTOR), "");
                    values[5] = "";
                }
                break;
            case "L":
                if(!dto.getTypeB().startsWith(LABEL_QUALITY_FACTOR)){
                    labels = Constants.labelsAuto;
                    break;
                }
                labels = Constants.labelsInductance;
                if (dto.getInductanceDerivator() != null) {
                    Map<DerivateInductance, Double> map = dto.getInductanceDerivator();
                    values[0] = formatValue(map.get(DerivateInductance.SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateInductance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateInductance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateInductance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateInductance.LOSS_FACTOR), "");
                    values[5] = "";
                }
                break;

            case "Z":
                if(!dto.getTypeB().startsWith(LABEL_REACTANCE)){
                    labels = Constants.labelsAuto;
                    break;
                }
                labels = Constants.labelsImpedance;
                if (dto.getImpedanceDerivator() != null) {
                    Map<DerivateImpedance, Double> map = dto.getImpedanceDerivator();
                    values[0] = formatValue(map.get(DerivateImpedance.RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateImpedance.PHASE_ANGLE), "°");
                    values[2] = formatValue(map.get(DerivateImpedance.QUALITY_FACTOR), "");
                    values[3] = formatValue(map.get(DerivateImpedance.LOSS_FACTOR), "");
                    values[4] = formatValue(map.get(DerivateImpedance.PARASITIC_INDUCTANCE), "H");
                    values[5] = formatValue(map.get(DerivateImpedance.PARASITIC_CAPACITANCE), "F");
                }
                break;

            default:
                labels = null;
                break;
        }

        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 6; i++) {
                if (labels != null && i < labels.length && labels[i] != null) {
                    tagLabels[i].setText(labels[i].toUpperCase());
                } else {
                    tagLabels[i].setText("");
                }

                if (values != null && i < values.length && values[i] != null) {
                    valueLabels[i].setText(values[i]);
                } else {
                    valueLabels[i].setText("");
                }
            }

            revalidate();
            repaint();
        });
    }

    private void setValuesEmpty(String[] values, MeasurementDTO dto ){
        for (int i = 0; i < 6;i++){
            values[i] = "";
        }
    }

    private String formatValue(Double value, String unit) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }

        if (unit == null || unit.isEmpty()) {
            return String.format("%.3f", value);
        }

        double abs = Math.abs(value);

        if ("Ω".equals(unit) && abs < 1.0) {
            return String.format("%.3f Ω", value);
        }

        String prefix = "";
        double scaled = value;

        if (abs >= 1e9) {
            scaled = value / 1e9;
            prefix = "G";
        }
        else if (abs >= 1e6) {
            scaled = value / 1e6;
            prefix = "M";
        }
        else if (abs >= 1e3) {
            scaled = value / 1e3;
            prefix = "k";
        }
        else if (abs >= 1) {
            scaled = value;
            prefix = "";
        }
        else if (abs >= 1e-3) {
            scaled = value * 1e3;
            prefix = "m";
        }
        else if (abs >= 1e-6) {
            scaled = value * 1e6;
            prefix = "µ";
        }
        else if (abs >= 1e-9) {
            scaled = value * 1e9;
            prefix = "n";
        }
        else if (abs >= 1e-12) {
            scaled = value * 1e12;
            prefix = "p";
        }
        else {
            scaled = value * 1e15;
            prefix = "f";
        }

        return String.format("%.3f %s%s", scaled, prefix, unit);
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 6; i++) {
                tagLabels[i].setText("N/A");
                valueLabels[i].setText("---");
            }
        });
    }
}