package gui;

import et431.beans.ConfigDTO;
import et431.beans.MeasurementDTO;
import et431.controller.MeterController;
import et431.enums.*;
import et431.observer.MeasurementObserver;
import et431.view.MeterView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main application frame that orchestrates the subpanels and connects UI events to the controller.
 */
public class MainWindow extends JFrame implements MeasurementObserver, MeterView {
    private ConfigurationPanel configurationPanel;
    private StatusBar statusBar;
    private MeasurementPanel measurementPanel;
    private MeterController meterController;
    private InfoPanel infoPanel;
    private DerivedPanel derivedPanel;

    public MainWindow() {
        super("Open East Tester");

        initialize();
    }

    private void initialize() {
        setIcon();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(825, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setupComponents();
        setupListeners();
        meterController = new MeterController(this);
        setVisible(true);
    }

    private void setIcon(){
        try {
            java.net.URL iconURL = getClass().getResource("/resources/app_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            } else {
                System.err.println("Icon not found");
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }
    }

    /**
     * Instantiates UI panels and mounts them onto the main window layout regions.
     */
    private void setupComponents() {
        configurationPanel = new ConfigurationPanel();
        statusBar = new StatusBar();
        measurementPanel = new MeasurementPanel();
        infoPanel = new InfoPanel(configurationPanel);
        derivedPanel = new DerivedPanel();

        JPanel centerContainer = new JPanel(new BorderLayout(10, 0));
        centerContainer.setOpaque(false);
        centerContainer.add(measurementPanel, BorderLayout.CENTER);
        centerContainer.add(derivedPanel, BorderLayout.EAST);

        add(configurationPanel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER); // Añadimos el contenedor intermedio aquí
    }

    /**
     * Binds UI control actions to controller operations, respecting synchronization flags.
     */
    private void setupListeners() {
        ActionListener comboListener = e -> {
            // Guard clause to prevent cyclic command loops when updating UI fields from hardware responses
            if (configurationPanel.isSynchronizing()) {
                return;
            }

            Object source = e.getSource();
            if (source == configurationPanel.getFrequencyComboBox()) {
                Frequency freq = (Frequency) configurationPanel.getFrequencyComboBox().getSelectedItem();
                meterController.changeFrequency(freq);
            } else if (source == configurationPanel.getVoltageComboBox()) {
                Voltage volt = (Voltage) configurationPanel.getVoltageComboBox().getSelectedItem();
                meterController.changeVoltage(volt);
            } else if (source == configurationPanel.getApertureComboBox()) {
                Aperture aper = (Aperture) configurationPanel.getApertureComboBox().getSelectedItem();
                meterController.changeAperture(aper);
            } else if (source == configurationPanel.getPrimaryComboBox()) {
                PrimaryParameter prim = (PrimaryParameter) configurationPanel.getPrimaryComboBox().getSelectedItem();
                meterController.changePrimaryParameter(prim);
            } else if (source == configurationPanel.getSecondaryComboBox()) {
                SecondaryParameter sec = (SecondaryParameter) configurationPanel.getSecondaryComboBox().getSelectedItem();
                meterController.changeSecondaryParameter(sec);
            } else if (source == configurationPanel.getSeriesModeComboBox()) {
                SeriesMode mode = (SeriesMode) configurationPanel.getSeriesModeComboBox().getSelectedItem();
                meterController.changeSeriesMode(mode);
            } else if (source == configurationPanel.getAutoRangeCheckBox()) {
                boolean auto = configurationPanel.getAutoRangeCheckBox().isSelected();
                configurationPanel.getRangeComboBox().setEnabled(!auto);
                meterController.changeAutoRange(auto);
            } else if (source == configurationPanel.getRangeComboBox()) {
                Range range = (Range) configurationPanel.getRangeComboBox().getSelectedItem();
                meterController.changeRange(range);
            } else if (source == configurationPanel.getBiasComboBox()) {
                BiasVoltage bias = (BiasVoltage) configurationPanel.getBiasComboBox().getSelectedItem();
                meterController.changeBias(bias);
            }
            ConfigDTO config = configurationPanel.getConfigFromUI();
            infoPanel.updateDisplay(config);
        };

        // Attach the centralized listener to all meter parameter components
        configurationPanel.getFrequencyComboBox().addActionListener(comboListener);
        configurationPanel.getVoltageComboBox().addActionListener(comboListener);
        configurationPanel.getApertureComboBox().addActionListener(comboListener);
        configurationPanel.getPrimaryComboBox().addActionListener(comboListener);
        configurationPanel.getSecondaryComboBox().addActionListener(comboListener);
        configurationPanel.getSeriesModeComboBox().addActionListener(comboListener);
        configurationPanel.getAutoRangeCheckBox().addActionListener(comboListener);
        configurationPanel.getRangeComboBox().addActionListener(comboListener);
        configurationPanel.getBiasComboBox().addActionListener(comboListener);

        // Connection button trigger handler
        configurationPanel.getConnectButton().addActionListener(e -> {
            String selectedPort = (String) configurationPanel.getPortComboBox().getSelectedItem();
            boolean checkingConnectAction = configurationPanel.getConnectButton().getText().equalsIgnoreCase("Connect Instrument");

            meterController.connectButtonPressed(selectedPort);

            // Push full UI configuration block down to the meter upon establishing initial connection
            if (checkingConnectAction) {
                setConfiguration();
            }
        });

        // Sidebar collapse/expand toggle handler
        configurationPanel.getToggleCollapseButton().addActionListener(e -> {
            configurationPanel.toggleCollapseState();
            // Force LayoutManager to recalculate sizes and expand the MeasurementPanel
            revalidate();
            repaint();
        });
    }

    private void setConfiguration() {
        ConfigDTO config = configurationPanel.getConfigFromUI();
        infoPanel.updateDisplay(config);
        meterController.applyConfiguration(config);
    }

    @Override
    public void onMeasurementReceived(MeasurementDTO dto) {
        measurementPanel.setPrimary(dto.getTypeA(), dto.getValueA());
        measurementPanel.setSecondary(dto.getTypeB(), dto.getValueB());
        derivedPanel.updateDisplay(dto);
    }

    @Override
    public void updateConnectionState(boolean isConnected, String model, String firmware, String portName) {
        configurationPanel.getConnectButton().setText(isConnected ? "Disconnect" : "Connect Instrument");
        statusBar.setStatus(isConnected ? "Connected" : "Disconnected");
        if (isConnected) {
            statusBar.setPort(portName);
            statusBar.setModel(model);
            statusBar.setFirmware(firmware);
        } else {
            statusBar.setPort("-");
            statusBar.setModel("-");
            statusBar.setFirmware("-");
            measurementPanel.clear();
            derivedPanel.clear();
        }
    }

    @Override
    public void updateUIFromConfig(ConfigDTO config) {
        infoPanel.updateDisplay(config);
        SwingUtilities.invokeLater(() -> {
            configurationPanel.updateCombosWithoutTriggeringEvents(config);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}