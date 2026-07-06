package gui;

import lcr.beans.ConfigDTO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Horizontal dashboard header that displays current meter configurations
 * using the high-contrast, dark-tech aesthetic of the MeasurementPanel.
 */
public class InfoPanel extends JPanel {

    private JLabel lblFrequency;
    private JLabel lblVoltage;
    private JLabel lblAperture;
    private JLabel lblFunctions;
    private JLabel lblRangeMode;
    private JLabel lblBias;

    // --- HIGH-CONTRAST TECH PALETTE (Matching MeasurementPanel) ---
    private final Color BG_PANEL = new Color(17, 24, 39);         // Deep Dark Slate
    private final Color TEXT_LABEL = new Color(156, 163, 175);     // Muted Gray for Labels
    private final Color TEXT_VALUE = new Color(34, 211, 238);      // Electric Cyan for active configurations
    private final Color BORDER_COLOR = new Color(55, 65, 81);      // Subtle dark separator divider

    public InfoPanel(ConfigurationPanel configurationPanel) {
        initialize( configurationPanel);
    }

    private void initialize(ConfigurationPanel configurationPanel) {
        setLayout(new GridBagLayout());
        setBackground(BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(31, 41, 55)), // Dark bottom accent line
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 8, 0, 8);

        // --- INSTANTIATE METRIC READOUTS ---
        lblFrequency = createValueLabel();
        lblVoltage = createValueLabel();
        lblAperture = createValueLabel();
        lblFunctions = createValueLabel();
        lblRangeMode = createValueLabel();
        lblBias = createValueLabel();

        // --- ASSEMBLY INTO HORIZONTAL GRID ---
        int gridx = 0;
        addMetricBlock("FREQUENCY", lblFrequency, gbc, gridx++);
        addSeparator(gbc, gridx++);
        addMetricBlock("TEST VOLTAGE", lblVoltage, gbc, gridx++);
        addSeparator(gbc, gridx++);
        addMetricBlock("SPEED", lblAperture, gbc, gridx++);
        addSeparator(gbc, gridx++);
        addMetricBlock("FUNCTION (1° / 2°)", lblFunctions, gbc, gridx++);
        addSeparator(gbc, gridx++);
        addMetricBlock("RANGE MODE", lblRangeMode, gbc, gridx++);
        addSeparator(gbc, gridx++);
        addMetricBlock("BIAS VOLTAGE", lblBias, gbc, gridx++);

        clear();
    }

    /**
     * Builds a structured vertical block inside the horizontal layout containing a label and its value.
     */
    private void addMetricBlock(String labelText, JLabel valueLabel, GridBagConstraints gbc, int gridx) {
        JPanel block = new JPanel(new GridLayout(2, 1, 0, 2));
        block.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(TEXT_LABEL);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        block.add(label);
        block.add(valueLabel);

        gbc.gridx = gridx;
        gbc.weightx = 1.0;
        add(block, gbc);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Monospaced", Font.BOLD, 14));
        label.setForeground(TEXT_VALUE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void addSeparator(GridBagConstraints gbc, int gridx) {
        JLabel sep = new JLabel("|");
        sep.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sep.setForeground(BORDER_COLOR);
        sep.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = gridx;
        gbc.weightx = 0.0;
        add(sep, gbc);
    }

    /**
     * Updates all parameters simultaneously parsing data from a central configuration object.
     */
    public void updateDisplay(ConfigDTO config) {
        if (config == null) {
            clear();
            return;
        }

        lblFrequency.setText(config.getFrequency() != null ? config.getFrequency().toString().replace("HZ", "") : "-");
        lblVoltage.setText(config.getVoltage() != null ? config.getVoltage().toString().replace("MV", "")  : "-");
        lblAperture.setText(config.getAperture() != null ? config.getAperture().toString() : "-");

        String prim = config.getPrimaryMeasurement() != null ? config.getPrimaryMeasurement().toString() : "?";
        String sec = config.getSecondaryMeasurement() != null ? config.getSecondaryMeasurement().toString() : "?";
        lblFunctions.setText(prim + " / " + sec);

        if (config.isAutoRange()) {
            lblRangeMode.setText("AUTO");
        } else {
            lblRangeMode.setText(config.getRange() != null ? "FIXED (" + config.getRange().toString() + ")" : "FIXED");
        }

        lblBias.setText(config.getBias() != null ? config.getBias().toString().replace("BIAS", "") : "-");
    }

    /**
     * Resets indicators back to a clean default state.
     */
    public void clear() {
        lblFrequency.setText("---");
        lblVoltage.setText("---");
        lblAperture.setText("---");
        lblFunctions.setText("--- / ---");
        lblRangeMode.setText("---");
        lblBias.setText("---");
    }
}