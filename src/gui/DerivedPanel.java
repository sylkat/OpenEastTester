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
    private final Color BG_PANEL = new Color(17, 24, 39);         // Deep Dark Slate
    private final Color TEXT_LABEL = new Color(156, 163, 175);     // Muted Gray for Labels
    private final Color TEXT_VALUE = new Color(34, 211, 238);      // Electric Cyan for active data
    private final Color BORDER_COLOR = new Color(55, 65, 81);      // Subtle dark separator divider

    public DerivedPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(0, 14)); // Vertical gap between title and grid
        setBackground(BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(2, 2, 2, 2, new Color(31, 41, 55)), // Subtle bounding box
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // --- TITLE ---
        lblTitle = new JLabel("DERIVED PARAMETERS");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_LABEL);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        // --- 2x3 GRID CONTAINER ---
        // 3 rows, 2 columns, 24px horizontal gap, 10px vertical gap
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 24, 10));
        gridPanel.setOpaque(false);

        valueLabels = new JLabel[6];
        tagLabels = new JLabel[6];

        // Instantiate and assemble the 6 metric blocks
        for (int i = 0; i < 6; i++) {
            JPanel block = new JPanel(new GridLayout(2, 1, 0, 2));
            block.setOpaque(false);

            tagLabels[i] = new JLabel("---");
            tagLabels[i].setFont(new Font("SansSerif", Font.BOLD, 9)); // Slightly smaller for dense grid
            tagLabels[i].setForeground(TEXT_LABEL);
            tagLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            valueLabels[i] = new JLabel("-");
            valueLabels[i].setFont(new Font("Monospaced", Font.BOLD, 14));
            valueLabels[i].setForeground(TEXT_VALUE);
            valueLabels[i].setHorizontalAlignment(SwingConstants.CENTER);

            // In your 2x3 grid, value goes first, then label under it
            block.add(tagLabels[i]);
            block.add(valueLabels[i]);

            gridPanel.add(block);
        }

        add(gridPanel, BorderLayout.CENTER);
        clear();
    }

    public void updateDisplay(MeasurementDTO dto) {
        if (dto == null) {
            clear();
            return;
        }

        final String title;
        final String[] labels;
        final String[] values = new String[6];

        switch (dto.getMeasureType()) {
            case "R":
                title = "Derived from Resistance";
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
                title = "Derived from Capacitance";
                labels = Constants.labelsCapacitance;
                if (dto.getCapacitanceDerivator() != null ) { // Adjusted variable name to match your DTO getter
                    var map = dto.getCapacitanceDerivator();
                    values[0] = formatValue(map.get(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateCapacitance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateCapacitance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateCapacitance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateCapacitance.QUALITY_FACTOR), "");
                    values[5] = "---"; // Empty Slot
                }
                break;

            case "L":
                title = "Derived from Inductance";
                labels = Constants.labelsInductance;
                if (dto.getInductanceDerivator() != null) {
                    var map = dto.getInductanceDerivator();
                    values[0] = formatValue(map.get(DerivateInductance.SERIES_RESISTANCE), "Ω");
                    values[1] = formatValue(map.get(DerivateInductance.REACTANCE), "Ω");
                    values[2] = formatValue(map.get(DerivateInductance.IMPEDANCE), "Ω");
                    values[3] = formatValue(map.get(DerivateInductance.PHASE_ANGLE), "°");
                    values[4] = formatValue(map.get(DerivateInductance.LOSS_FACTOR), "");
                    values[5] = "---"; // Empty Slot
                }
                break;

            case "Z":
                title = "Derived from Impedance";
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
                title = "Derived Parameters";
                labels = null;
                break;
        }

        SwingUtilities.invokeLater(() -> {
            if (title != null) {
                lblTitle.setText(title.toUpperCase());
            }

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
                    valueLabels[i].setText("---");
                }
            }

            revalidate();
            repaint();
        });
    }

    /**
     * Helper method to cleanly format double values into scientific or fixed notation
     * while filtering out nulls, NaNs, or Infinite mathematical values.
     */
    private String formatValue(Double value, String unit) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return "---";
        }

        // Optional engineering display shortcut: if values are incredibly tiny (like Farads/Henries parásitos)
        // you might want to use scientific notation so you don't get 0.000000023 H.
        if (Math.abs(value) > 0.0 && Math.abs(value) < 0.001) {
            return String.format("%.3e %s", value, unit).trim();
        }

        return String.format("%.3f %s", value, unit).trim();
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