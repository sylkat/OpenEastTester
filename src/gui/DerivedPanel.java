package gui;

import et431.beans.ConfigDTO;
import et431.beans.MeasurementDTO;
import et431.enums.DerivateCapacitance;
import et431.enums.DerivateImpedance;
import et431.enums.DerivateInductance;
import et431.enums.DerivateResistance;
import et431.util.*;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static et431.util.Constants.*;

/**
 * Dashboard panel that displays calculated derived parameters
 * in a 2x3 grid using the high-contrast, dark-tech aesthetic of InfoPanel.
 */
public class DerivedPanel extends JPanel {

    private JLabel lblTitle;
    private JLabel[] valueLabels;
    private JLabel[] tagLabels;

    // --- HIGH-CONTRAST TECH PALETTE (Cloned from InfoPanel) ---
    // --- HIGH-CONTRAST TECH PALETTE (Cloned from InfoPanel) ---
    private final Color BG_PANEL = new Color(17, 24, 39);         // Deep Dark Slate
    private final Color TEXT_LABEL = new Color(156, 163, 175);     // Muted Gray for Labels

    // CAMBIADO: De cian eléctrico a Verde de Laboratorio de alta visibilidad (Bench Matrix Green)
    private final Color TEXT_VALUE = new Color(34, 197, 94);       // Hex: #22C55E (Vibrant Laboratory Green)

    private final Color BORDER_COLOR = new Color(55, 65, 81);      // Subtle dark separator divider

    public DerivedPanel() {
        initialize();
    }

    // ============================================================
//  Versión mejorada de initialize()
//  Mismo BG_PANEL, TEXT_LABEL y TEXT_VALUE que ya usas.
//  Se agrega un color ACCENT_COLOR (o usa el que ya tengas
//  definido en tu paleta, p. ej. un azul/verde de acento).
// ============================================================

    private static final Color ACCENT_COLOR = new Color(56, 189, 248); // ajusta a tu paleta

    private void initialize() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(55, 65, 81)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        // --- TÍTULO CON ACENTO ---
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        lblTitle = new JLabel("DERIVED PARAMETERS");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_LABEL);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel underline = new JPanel();
        underline.setBackground(ACCENT_COLOR);
        underline.setPreferredSize(new Dimension(36, 2));
        underline.setMaximumSize(new Dimension(36, 2));
        underline.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(underline);

        add(titlePanel, BorderLayout.NORTH);

        // --- GRID 3x2 DE TARJETAS ---
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 14, 12));
        gridPanel.setOpaque(false);

        valueLabels = new JLabel[6];
        tagLabels = new JLabel[6];

        for (int i = 0; i < 6; i++) {
            RoundedCardPanel block = new RoundedCardPanel(10, blend(BG_PANEL, Color.WHITE, 0.045f));
            block.setLayout(new GridLayout(2, 1, 0, 4));
            block.setBorder(BorderFactory.createEmptyBorder(9, 6, 9, 6));

            valueLabels[i] = new JLabel("-");
            valueLabels[i].setFont(new Font("Monospaced", Font.BOLD, 15));
            valueLabels[i].setForeground(TEXT_VALUE);
            valueLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            tagLabels[i] = new JLabel("---");
            tagLabels[i].setFont(new Font("SansSerif", Font.BOLD, 9));
            tagLabels[i].setForeground(TEXT_LABEL);
            tagLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            // Valor arriba, etiqueta abajo (más legible en tarjeta)
            block.add(valueLabels[i]);
            block.add(tagLabels[i]);

            gridPanel.add(block);
        }

        add(gridPanel, BorderLayout.CENTER);
        clear();
    }

    /**
     * Mezcla un color base con otro (blanco/negro) para aclarar u oscurecer
     * ligeramente sin salir de la paleta original.
     */
    private static Color blend(Color base, Color target, float ratio) {
        int r = (int) (base.getRed()   + (target.getRed()   - base.getRed())   * ratio);
        int g = (int) (base.getGreen() + (target.getGreen() - base.getGreen()) * ratio);
        int b = (int) (base.getBlue()  + (target.getBlue()  - base.getBlue())  * ratio);
        return new Color(r, g, b);
    }

    /**
     * Panel con esquinas redondeadas, usado como "tarjeta" para cada métrica.
     * Mantiene el color de fondo del panel general, solo con un tono sutilmente
     * distinto para dar profundidad sin romper la paleta.
     */
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
            // Borde muy sutil para separar la tarjeta del fondo
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

       // final String title;
        final String[] labels;
        final String[] values = new String[6];

        switch (dto.getMeasureType()) {
            case "R":
               // title = "Derived from Resistance";
                labels = Constants.labelsResistance; // Assuming these are in Constants
                if (dto.getResistanceDerivator() != null) {
                    var map = dto.getResistanceDerivator();
                    values[0] = formatValue(map.get(DerivateResistance.IMPEDANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateResistance.PHASE_ANGLE), "°");
                    values[2] = formatValue(map.get(DerivateResistance.QUALITY_FACTOR), "");
                    values[3] = formatValue(map.get(DerivateResistance.LOSS_FACTOR), "");
                    values[4] = formatValue(map.get(DerivateResistance.PARASITIC_INDUCTANCE), "H");
                    values[5] = formatValue(map.get(DerivateResistance.PARASITIC_CAPACITANCE), "F");
                }
                break;

            case "C":
               // title = "Derived from Capacitance";
                labels = Constants.labelsCapacitance;
                if (dto.getCapacitanceDerivator() != null ) { // Adjusted variable name to match your DTO getter
                    var map = dto.getCapacitanceDerivator();
                    values[0] = formatValue(map.get(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateCapacitance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateCapacitance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateCapacitance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateCapacitance.QUALITY_FACTOR), "");
                    values[5] = ""; // Empty Slot
                }
                break;

            case "L":
               // title = "Derived from Inductance";
                labels = Constants.labelsInductance;
                if (dto.getInductanceDerivator() != null) {
                    var map = dto.getInductanceDerivator();
                    values[0] = formatValue(map.get(DerivateInductance.SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateInductance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateInductance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateInductance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateInductance.LOSS_FACTOR), "");
                    values[5] = ""; // Empty Slot
                }
                break;

            case "Z":
               // title = "Derived from Impedance";
                labels = Constants.labelsImpedance;
                if (dto.getImpedanceDerivator() != null) {
                    var map = dto.getImpedanceDerivator();
                    values[0] = formatValue(map.get(DerivateImpedance.RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateImpedance.PHASE_ANGLE), "°");
                    values[2] = formatValue(map.get(DerivateImpedance.QUALITY_FACTOR), "");
                    values[3] = formatValue(map.get(DerivateImpedance.LOSS_FACTOR), "");
                    values[4] = formatValue(map.get(DerivateImpedance.PARASITIC_INDUCTANCE), "H");
                    values[5] = formatValue(map.get(DerivateImpedance.PARASITIC_CAPACITANCE), "F");
                }
                break;

            default:
               // title = "Derived Parameters";
                labels = null;
                break;
        }

        SwingUtilities.invokeLater(() -> {
            //if (title != null) {
           //     lblTitle.setText(title.toUpperCase());
          //  }

            for (int i = 0; i < 6; i++) {
                // Update Metric Tags/Labels
                if (labels != null && i < labels.length && labels[i] != null) {
                    tagLabels[i].setText(labels[i].toUpperCase());
                } else {
                    tagLabels[i].setText("");
                }

                // Update Metric Numerical Values
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

    private String formatValue(Double value, String unit) {

        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }

        // Sin unidad (Q, D...)
        if (unit == null || unit.isEmpty()) {
            return String.format("%.3f", value);
        }

        double abs = Math.abs(value);

        // --- EXCEPCIÓN PARA RESISTENCIA BAJA (Evita mΩ y µΩ, muestra 0.000 Ω) ---
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

    /**
     * Resets indicators back to a clean default state.
     */
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            lblTitle.setText("DERIVED PARAMETERS");
            for (int i = 0; i < 6; i++) {
                tagLabels[i].setText("N/A");
                valueLabels[i].setText("---");
            }
        });
    }
}