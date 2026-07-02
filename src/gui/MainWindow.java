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

    public MainWindow() {
        super("ET431 PC Controller");
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(725, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupComponents();
        setupListeners();

        meterController = new MeterController(this);
        setVisible(true);
    }

    /**
     * Instantiates UI panels and mounts them onto the main window layout regions.
     */
    private void setupComponents() {
        configurationPanel = new ConfigurationPanel();
        statusBar = new StatusBar();
        measurementPanel = new MeasurementPanel();
        infoPanel = new InfoPanel(configurationPanel);
        add(configurationPanel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
        add(measurementPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.NORTH);
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