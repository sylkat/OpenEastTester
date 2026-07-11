package gui;

import com.fazecast.jSerialComm.SerialPort;
import lcr.beans.ConfigDTO;
import lcr.beans.MeasurementDTO;
import lcr.business.SerialDetector;
import lcr.controller.MeterController;
import lcr.enums.*;
import lcr.observer.MeasurementObserver;
import lcr.util.Constants;
import lcr.view.MeterView;
import lcr.view.RealTimeChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import static lcr.util.Constants.TITLE_APP;

/**
 * Main application frame that orchestrates the subpanels and connects UI events to the controller.
 */
public class MainWindow extends JFrame implements MeasurementObserver, MeterView {
    private ConfigurationPanel configurationPanel;
    private StatusBarPanel statusBarPanel;
    private MeasurementPanel measurementPanel;
    private MeterController meterController;
    private InfoPanel infoPanel;
    private DerivedPanel derivedPanel;
    private RealTimeChartPanel realTimeChartPanel;

    private JPanel southContainer;
    private JButton btnTogglePlot;
    JPanel mainContentGrid;

    public MainWindow() {
        super(TITLE_APP);
        initialize();
    }

    private void initialize() {
        setIcon();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(825, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        meterController = new MeterController(this);
        setupComponents();
        setupListeners();
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
        statusBarPanel = new StatusBarPanel();
        measurementPanel = new MeasurementPanel();
        infoPanel = new InfoPanel(configurationPanel);
        derivedPanel = new DerivedPanel();
        realTimeChartPanel = new RealTimeChartPanel(meterController);
        btnTogglePlot = new JButton("Hide Plot");
        btnTogglePlot.setFocusable(false);
        btnTogglePlot.setFocusPainted(false);
        btnTogglePlot.setContentAreaFilled(true);
        btnTogglePlot.setOpaque(true);
        btnTogglePlot.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTogglePlot.setBackground(new Color(31, 41, 55));   // Mismo color gris-tarjeta que usamos en el gráfico
        btnTogglePlot.setForeground(new Color(156, 163, 175)); // TEXT_LABEL (Muted Gray)
        btnTogglePlot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true), // BORDER_COLOR
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        Color bgDarkApp = new Color(17, 24, 39);

        JPanel bottomBarLayout = new JPanel(new BorderLayout(0, 0));
        bottomBarLayout.setOpaque(true);
        bottomBarLayout.setBackground(bgDarkApp);
        bottomBarLayout.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper.setOpaque(true);
        btnWrapper.setBackground(bgDarkApp);
        btnWrapper.add(btnTogglePlot);

        bottomBarLayout.add(btnWrapper, BorderLayout.NORTH);
        southContainer = new JPanel(new BorderLayout(0, 0));
        southContainer.setOpaque(true);
        southContainer.setBackground(bgDarkApp);
        southContainer.add(realTimeChartPanel, BorderLayout.CENTER);
        southContainer.add(bottomBarLayout, BorderLayout.SOUTH);


        JPanel topDataPanel = new JPanel(new BorderLayout(0, 0));
        topDataPanel.setOpaque(false);
        topDataPanel.add(measurementPanel, BorderLayout.WEST);
        topDataPanel.add(derivedPanel, BorderLayout.CENTER);
        measurementPanel.setPreferredSize(new Dimension(400, 300));
        measurementPanel.setMaximumSize(new Dimension(400, 300));
        topDataPanel.setPreferredSize(new Dimension(200, 300));
        topDataPanel.setMaximumSize(new Dimension(200, 300));
        mainContentGrid = new JPanel(new GridBagLayout());
        mainContentGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // peso vertical 0
        gbc.insets = new Insets(0, 0, 0, 0);
        mainContentGrid.add(topDataPanel, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // peso vertical completo
        gbc.insets = new Insets(0, 0, 0, 0);
        mainContentGrid.add(southContainer, gbc);

        add(infoPanel, BorderLayout.NORTH);
        add(configurationPanel, BorderLayout.EAST);
        add(mainContentGrid, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);
    }


    /**
     * Binds UI control actions to controller operations, respecting synchronization flags.
     */
    private void setupListeners() {
        setButtonTogglePotListener();
        setComboBoxListeners();
        configurationPanel.getConnectButton().addActionListener(e -> {onButtonConnect();});
        configurationPanel.getToggleCollapseButton().addActionListener(e -> {
            configurationPanel.toggleCollapseState();
            revalidate();
            repaint();
        });
        initSerialPortListener();
    }

    private void onButtonConnect(){
        String selectedPort = (String) configurationPanel.getPortComboBox().getSelectedItem();
        SupportedMeter selectedModel= (SupportedMeter) configurationPanel.getModelComboBox().getSelectedItem();
        boolean checkingConnectAction = configurationPanel.getConnectButton().getText().equalsIgnoreCase("Connect");

        if(!meterController.connectButtonPressed(selectedPort,selectedModel)){
            JOptionPane.showMessageDialog(
                    this,
                    "Could not establish a connection with the LCR meter on port " + selectedPort + ".\n" +
                            "Please verify the cable connection, make sure the device is ON, and try again.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (checkingConnectAction) {
            setConfiguration();
        }
    }

    private void setConfiguration() {
        ConfigDTO config = configurationPanel.getConfigFromUI();
        infoPanel.updateDisplay(config);
       //meterController.applyConfiguration(config);
    }

    @Override
    public void onMeasurementReceived(MeasurementDTO dto) {
        measurementPanel.setPrimary(dto.getTypeA()+" ("+dto.getSeriesMode()+")", dto.getValueA());
        measurementPanel.setSecondary(dto.getTypeB(), dto.getValueB());
        derivedPanel.updateDisplay(dto);
        realTimeChartPanel.updateData(dto);
    }

    @Override
    public void updateConnectionState(boolean isConnected,String manufacter, String model, String firmware, String portName) {
        configurationPanel.getConnectButton().setText(isConnected ? "Disconnect" : "Connect");
        statusBarPanel.setStatus(isConnected ? "Connected" : "Disconnected");
        if (isConnected) {
            statusBarPanel.setPort(portName);
            statusBarPanel.setManufacter(manufacter);
            statusBarPanel.setModel(model);
            statusBarPanel.setFirmware(firmware);
        } else {
            statusBarPanel.setPort("-");
            statusBarPanel.setManufacter("-");
            statusBarPanel.setModel("-");
            statusBarPanel.setFirmware("-");
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
    @Override
    public void  onDisconnected() {
        meterController.disconnect();
    }

    private void setComboBoxListeners(){
        ActionListener comboListener = e -> {
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
            }else if (source == configurationPanel.getChkDebug()) {
                Constants.SHOW_LOGS = configurationPanel.getChkDebug().isSelected();
            }
            ConfigDTO config = configurationPanel.getConfigFromUI();
            infoPanel.updateDisplay(config);
        };

        // Attach listeners...
        configurationPanel.getFrequencyComboBox().addActionListener(comboListener);
        configurationPanel.getVoltageComboBox().addActionListener(comboListener);
        configurationPanel.getApertureComboBox().addActionListener(comboListener);
        configurationPanel.getPrimaryComboBox().addActionListener(comboListener);
        configurationPanel.getSecondaryComboBox().addActionListener(comboListener);
        configurationPanel.getSeriesModeComboBox().addActionListener(comboListener);
        configurationPanel.getAutoRangeCheckBox().addActionListener(comboListener);
        configurationPanel.getRangeComboBox().addActionListener(comboListener);
        configurationPanel.getBiasComboBox().addActionListener(comboListener);
        configurationPanel.getChkDebug().addActionListener(comboListener);
    }

    private void setButtonTogglePotListener(){
        btnTogglePlot.addActionListener(e -> {
            boolean isVisible = realTimeChartPanel.isVisible();
            if (isVisible) {
                setSize(getWidth(), 440);
                realTimeChartPanel.setVisible(false);
                btnTogglePlot.setText("Show Plot");
                realTimeChartPanel.setSize(new Dimension(200,800));
                btnTogglePlot.setForeground(new Color(0x00, 0xE5, 0xC9)); // Cian
            } else {
                realTimeChartPanel.setVisible(true);
                realTimeChartPanel.setSize(new Dimension(200,1000));
                btnTogglePlot.setText("Hide Plot");
                btnTogglePlot.setForeground(new Color(0x9A, 0x9D, 0xA3));
                setSize(getWidth(), 750);
            }
            mainContentGrid.updateUI();
            revalidate();
            repaint();
        });
    }

    public void initSerialPortListener(){
        SerialDetector detector = new SerialDetector(new SerialDetector.OnPortDetectedListener() {
            @Override
            public void onNewPortConnected(String portName) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        configurationPanel.getPortComboBox().removeAllItems(); // Usa removeAllItems() para JComboBox
                        for (SerialPort port : SerialPort.getCommPorts()) {
                            configurationPanel.getPortComboBox().addItem(port.getSystemPortName());
                        }
                        configurationPanel.getPortComboBox().revalidate();
                        configurationPanel.getPortComboBox().repaint();
                    }
                });
            }
        });
        detector.startMonitoring();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}