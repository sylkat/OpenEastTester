package gui;

import com.fazecast.jSerialComm.SerialPort;
import et431.beans.ConfigDTO;
import et431.enums.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * Sidebar configuration panel containing setup controls for the LCR meter.
 * Supports dynamic collapse/expand actions to maximize main viewing space.
 */
public class ConfigurationPanel extends JPanel {

    private JComboBox<String> cmbPort;
    private JButton btnConnect;
    private JButton btnApply;

    private JComboBox<Frequency> cmbFrequency;
    private JComboBox<Voltage> cmbVoltage;
    private JComboBox<Aperture> cmbAperture;
    private JComboBox<PrimaryParameter> cmbPrimary;
    private JComboBox<SecondaryParameter> cmbSecondary;
    private JComboBox<SeriesMode> cmbSeriesMode;
    private JCheckBox chkAutoRange;
    private JComboBox<Range> cmbRange;
    private JComboBox<BiasVoltage> cmbBias;

    // --- COLLAPSIBLE UI COMPONENTS & STATE ---
    private JButton btnToggleCollapse;
    private JScrollPane scrollPane;
    private boolean isCollapsed = false;

    private boolean isSynchronizing = false;

    // --- HIGH-CONTRAST TECH PALETTE (Matching InfoPanel) ---
    private final Color BG_PANEL = new Color(17, 24, 39);          // Deep Dark Slate
    private final Color BG_CARD = new Color(31, 41, 55);           // Dark Gray for Subpanels
    private final Color TEXT_LABEL = new Color(156, 163, 175);     // Muted Gray for Labels
    private final Color TEXT_VALUE = new Color(243, 244, 246);     // High Contrast Off-White for Combos
    private final Color BORDER_COLOR = new Color(55, 65, 81);      // Subtle dark separator lines

    private final Color BTN_CONNECT_BG = new Color(55, 65, 81);    // Mid Slate Gray
    private final Color BTN_TOGGLE_BG = new Color(37, 99, 235);    // Premium Electric Blue for Toggle

    public ConfigurationPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(0, 8));
        setBackground(BG_PANEL);
        setPreferredSize(new Dimension(300, 0)); // Initial expanded width
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // --- TOP ACTION CONTROL: TOGGLE COLLAPSE ---
        btnToggleCollapse = new JButton("→ Hide Panel");
        styleButton(btnToggleCollapse, BTN_TOGGLE_BG, Color.WHITE);
        add(btnToggleCollapse, BorderLayout.NORTH);

        // --- CONNECTION SECTION ---
        JPanel pnlConnection = new JPanel(new GridBagLayout());
        styleSubPanel(pnlConnection, "PORT CONNECTION");
        GridBagConstraints gbcConn = createDefaultGbc();

        addLabel(pnlConnection, "Serial Port:", gbcConn, 0);
        cmbPort = new JComboBox<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            cmbPort.addItem(port.getSystemPortName());
        }
        styleComponent(cmbPort);
        pnlConnection.add(cmbPort, gbcConn);

        gbcConn.gridx = 0; gbcConn.gridy = 1; gbcConn.gridwidth = 2;
        gbcConn.insets = new Insets(10, 4, 4, 4);
        btnConnect = new JButton("Connect");
        styleButton(btnConnect, BTN_CONNECT_BG, Color.WHITE);
        pnlConnection.add(btnConnect, gbcConn);

        // --- LCR METER SETTINGS SECTION ---
        JPanel pnlSettings = new JPanel(new GridBagLayout());
        styleSubPanel(pnlSettings, "METER PARAMETERS");
        GridBagConstraints gbcSet = createDefaultGbc();
        int row = 0;

        addLabel(pnlSettings, "Frequency:", gbcSet, row);
        cmbFrequency = new JComboBox<>(Frequency.values());
        cmbFrequency.setSelectedItem(Frequency.valueOf("HZ1000"));
        styleComponent(cmbFrequency);
        pnlSettings.add(cmbFrequency, gbcSet);
        row++;

        addLabel(pnlSettings, "Test Voltage:", gbcSet, row);
        cmbVoltage = new JComboBox<>(Voltage.values());
        cmbVoltage.setSelectedItem(Voltage.valueOf("MV600"));
        styleComponent(cmbVoltage);
        pnlSettings.add(cmbVoltage, gbcSet);
        row++;

        addLabel(pnlSettings, "Speed (Aperture):", gbcSet, row);
        cmbAperture = new JComboBox<>(Aperture.values());
        styleComponent(cmbAperture);
        pnlSettings.add(cmbAperture, gbcSet);
        row++;

        addLabel(pnlSettings, "Primary Function:", gbcSet, row);
        cmbPrimary = new JComboBox<>(PrimaryParameter.values());
        styleComponent(cmbPrimary);
        pnlSettings.add(cmbPrimary, gbcSet);
        row++;

        addLabel(pnlSettings, "Secondary Function:", gbcSet, row);
        cmbSecondary = new JComboBox<>(SecondaryParameter.values());
        styleComponent(cmbSecondary);
        pnlSettings.add(cmbSecondary, gbcSet);
        row++;

        addLabel(pnlSettings, "Equivalent Mode:", gbcSet, row);
        cmbSeriesMode = new JComboBox<>(SeriesMode.values());
        styleComponent(cmbSeriesMode);
        pnlSettings.add(cmbSeriesMode, gbcSet);
        row++;

        addLabel(pnlSettings, "Auto Range:", gbcSet, row);
        chkAutoRange = new JCheckBox("Auto");
        chkAutoRange.setBackground(pnlSettings.getBackground());
        chkAutoRange.setForeground(TEXT_VALUE);
        chkAutoRange.setFont(new Font("SansSerif", Font.BOLD, 12));
        chkAutoRange.setFocusPainted(false);
        pnlSettings.add(chkAutoRange, gbcSet);
        row++;

        addLabel(pnlSettings, "Fixed Range:", gbcSet, row);
        cmbRange = new JComboBox<>(Range.values());
        styleComponent(cmbRange);
        pnlSettings.add(cmbRange, gbcSet);
        row++;

        addLabel(pnlSettings, "Bias Voltage:", gbcSet, row);
        cmbBias = new JComboBox<>(BiasVoltage.values());
        styleComponent(cmbBias);
        pnlSettings.add(cmbBias, gbcSet);
        row++;

        // Reactive local UI flow logic for handling the range dropdown state
        chkAutoRange.addItemListener(e -> {
            boolean isAuto = (e.getStateChange() == ItemEvent.SELECTED);
            cmbRange.setEnabled(!isAuto);
            cmbRange.setBackground(isAuto ? new Color(55, 65, 81) : BG_CARD);
            cmbRange.setForeground(isAuto ? new Color(107, 114, 128) : TEXT_VALUE);
        });

        gbcSet.gridx = 0; gbcSet.gridy = row; gbcSet.gridwidth = 2;
        gbcSet.insets = new Insets(16, 4, 4, 4);

        // --- FINAL ASSEMBLY ---
        JPanel pnlCenterGroup = new JPanel(new GridBagLayout());
        pnlCenterGroup.setBackground(BG_PANEL);

        GridBagConstraints gbcGroup = new GridBagConstraints();
        gbcGroup.gridx = 0; gbcGroup.gridy = 0;
        gbcGroup.fill = GridBagConstraints.HORIZONTAL;
        gbcGroup.weightx = 1.0;
        pnlCenterGroup.add(pnlConnection, gbcGroup);

        gbcGroup.gridy = 1;
        gbcGroup.insets = new Insets(12, 0, 0, 0);
        pnlCenterGroup.add(pnlSettings, gbcGroup);

        gbcGroup.gridy = 2;
        gbcGroup.weighty = 1.0;
        gbcGroup.fill = GridBagConstraints.BOTH;
        JPanel pnlSpacer = new JPanel();
        pnlSpacer.setOpaque(false);
        pnlCenterGroup.add(pnlSpacer, gbcGroup);

        scrollPane = new JScrollPane(pnlCenterGroup);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_PANEL);
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private GridBagConstraints createDefaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void styleSubPanel(JPanel panel, String title) {
        panel.setBackground(BG_CARD);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                "  " + title + "  "
        );
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
        border.setTitleColor(new Color(156, 163, 175));
        panel.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(10, 10, 12, 10)
        ));
    }

    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.4;
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT_LABEL);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6;
    }

    private void styleComponent(JComponent comp) {
        comp.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comp.setBackground(BG_CARD);
        comp.setForeground(TEXT_VALUE);
        comp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        if (comp instanceof JComboBox) {
            ((JComboBox<?>) comp).setFocusable(false);
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    // --- COLLAPSIBLE UI COMPONENTS & STATE ---
    public void toggleCollapseState() {
        isCollapsed = !isCollapsed;
        if (isCollapsed) {
            scrollPane.setVisible(false);
            btnToggleCollapse.setText("← Panel");
            setPreferredSize(new Dimension(55, 0)); // Width optimized for compact collapsed layout text/arrow
        } else {
            scrollPane.setVisible(true);
            btnToggleCollapse.setText("→ Hide Panel");
            setPreferredSize(new Dimension(300, 0)); // Return to baseline configuration width
        }
    }

    // --- GETTERS ---
    public JButton getToggleCollapseButton() { return btnToggleCollapse; }
    public JComboBox<String> getPortComboBox() { return cmbPort; }
    public JButton getConnectButton() { return btnConnect; }
    public JButton getApplyButton() { return btnApply; }
    public JComboBox<Frequency> getFrequencyComboBox() { return cmbFrequency; }
    public JComboBox<Voltage> getVoltageComboBox() { return cmbVoltage; }
    public JComboBox<Aperture> getApertureComboBox() { return cmbAperture; }
    public JComboBox<PrimaryParameter> getPrimaryComboBox() { return cmbPrimary; }
    public JComboBox<SecondaryParameter> getSecondaryComboBox() { return cmbSecondary; }
    public JComboBox<SeriesMode> getSeriesModeComboBox() { return cmbSeriesMode; }
    public JCheckBox getAutoRangeCheckBox() { return chkAutoRange; }
    public JComboBox<Range> getRangeComboBox() { return cmbRange; }
    public JComboBox<BiasVoltage> getBiasComboBox() { return cmbBias; }

    public boolean isSynchronizing() {
        return isSynchronizing;
    }

    public ConfigDTO getConfigFromUI() {
        return new ConfigDTO(
                (Frequency) getFrequencyComboBox().getSelectedItem(),
                (Voltage) getVoltageComboBox().getSelectedItem(),
                (Aperture) getApertureComboBox().getSelectedItem(),
                (PrimaryParameter) getPrimaryComboBox().getSelectedItem(),
                (SecondaryParameter) getSecondaryComboBox().getSelectedItem(),
                getAutoRangeCheckBox().isSelected(),
                (Range) getRangeComboBox().getSelectedItem(),
                (BiasVoltage) getBiasComboBox().getSelectedItem()
        );
    }

    public void updateCombosWithoutTriggeringEvents(ConfigDTO config) {
        if (config == null) return;
        isSynchronizing = true;
        try {
            if (config.getFrequency() != null) cmbFrequency.setSelectedItem(config.getFrequency());
            if (config.getVoltage() != null) cmbVoltage.setSelectedItem(config.getVoltage());
            if (config.getAperture() != null) cmbAperture.setSelectedItem(config.getAperture());
            if (config.getPrimaryMeasurement() != null) cmbPrimary.setSelectedItem(config.getPrimaryMeasurement());
            if (config.getSecondaryMeasurement() != null) cmbSecondary.setSelectedItem(config.getSecondaryMeasurement());

            chkAutoRange.setSelected(config.isAutoRange());
            cmbRange.setEnabled(!config.isAutoRange());
            cmbRange.setBackground(config.isAutoRange() ? new Color(55, 65, 81) : BG_CARD);
            cmbRange.setForeground(config.isAutoRange() ? new Color(107, 114, 128) : TEXT_VALUE);

            if (config.getRange() != null) {
                cmbRange.setSelectedItem(config.getRange());
            }
            if (config.getBias() != null) cmbBias.setSelectedItem(config.getBias());
        } finally {
            isSynchronizing = false;
        }
    }
}

