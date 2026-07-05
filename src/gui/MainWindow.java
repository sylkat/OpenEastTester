package gui;

import et431.beans.ConfigDTO;
import et431.beans.MeasurementDTO;
import et431.controller.MeterController;
import et431.enums.*;
import et431.observer.MeasurementObserver;
import et431.view.MeterView;
import et431.view.RealTimeChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private RealTimeChartPanel realTimeChartPanel;

    // Propiedad para recordar el contenedor de la gráfica y controlar el toggle
    private JPanel southContainer;
    private JButton btnTogglePlot;
    JPanel mainContentGrid;

    public MainWindow() {
        super("Open East Tester");

        initialize();
    }

    private void initialize() {
        setIcon();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Ajustamos la altura inicial a 750 para que quepan holgadamente los bloques numéricos + gráfico
        setSize(825, 750);
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
        realTimeChartPanel = new RealTimeChartPanel();

        // 1. Botón de control rápido para alternar la gráfica (Estilo a juego con el gráfico)
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

// --- LA CLAVE: El color de fondo que antes era oscuro/negro, ahora es Deep Dark Slate ---
        Color bgDarkApp = new Color(17, 24, 39); // Mismo fondo unificado que DerivedPanel y RealTimeChartPanel

// Subpanel para organizar la barra inferior (Botón a la derecha)
        JPanel bottomBarLayout = new JPanel(new BorderLayout(0, 0));
        bottomBarLayout.setOpaque(true);
        bottomBarLayout.setBackground(bgDarkApp); // <--- Aquí ya no es negro, es el azul pizarra oscuro
        bottomBarLayout.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper.setOpaque(true);
        btnWrapper.setBackground(bgDarkApp); // <--- Mismo fondo unificado
        btnWrapper.add(btnTogglePlot);

        bottomBarLayout.add(btnWrapper, BorderLayout.NORTH);

// Subpanel southContainer que agrupa gráfico y el botón inferior
        southContainer = new JPanel(new BorderLayout(0, 0));
        southContainer.setOpaque(true);
        southContainer.setBackground(bgDarkApp); // <--- Mismo fondo unificado
        southContainer.add(realTimeChartPanel, BorderLayout.CENTER);
        southContainer.add(bottomBarLayout, BorderLayout.SOUTH);

        // 2. Contenedor para la parte superior numérica (Medición + Derivados)
        JPanel topDataPanel = new JPanel(new BorderLayout(0, 0));
        topDataPanel.setOpaque(false);
        topDataPanel.add(measurementPanel, BorderLayout.WEST);
        topDataPanel.add(derivedPanel, BorderLayout.CENTER);
        measurementPanel.setPreferredSize(new Dimension(400, 300));
        measurementPanel.setMaximumSize(new Dimension(400, 300));
        topDataPanel.setPreferredSize(new Dimension(200, 300));
        topDataPanel.setMaximumSize(new Dimension(200, 300));
        // 3. ¡LA CLAVE!: Un contenedor central elástico usando GridBagLayout
        mainContentGrid = new JPanel(new GridBagLayout());
        mainContentGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // Fila 0: Bloque numérico (No estira verticalmente por sí solo)
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // peso vertical 0
        gbc.insets = new Insets(0, 0, 0, 0); // Margen inferior
        mainContentGrid.add(topDataPanel, gbc);

        // Fila 1: Bloque del gráfico (Se lleva todo el peso y elasticidad vertical)
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // peso vertical completo
        gbc.insets = new Insets(0, 0, 0, 0);
        mainContentGrid.add(southContainer, gbc);

        // 4. Distribución limpia del Frame Principal
        add(infoPanel, BorderLayout.NORTH);
        add(configurationPanel, BorderLayout.EAST);
        add(mainContentGrid, BorderLayout.CENTER); // El GridBag dinámico toma el control
        add(statusBar, BorderLayout.SOUTH);        // La barra de estado fija abajo del todo
    }

    /**
     * Binds UI control actions to controller operations, respecting synchronization flags.
     */
    private void setupListeners() {
        // AÑADIDO: Listener del botón de ocultar/mostrar gráfico con redimensionado del JFrame
        btnTogglePlot.addActionListener(e -> {
            boolean isVisible = realTimeChartPanel.isVisible();

            if (isVisible) {
                // 1. Primero reducimos el tamaño de la ventana para preparar el colapso
                setSize(getWidth(), 440);

                // 2. Ocultamos el gráfico
                realTimeChartPanel.setVisible(false);
                btnTogglePlot.setText("Show Plot");
                realTimeChartPanel.setSize(new Dimension(200,800));
                btnTogglePlot.setForeground(new Color(0x00, 0xE5, 0xC9)); // Cian
            } else {
                // 1. Primero mostramos el gráfico para que recupere su espacio
                realTimeChartPanel.setVisible(true);
                realTimeChartPanel.setSize(new Dimension(200,1000));
                btnTogglePlot.setText("Hide Plot");
                btnTogglePlot.setForeground(new Color(0x9A, 0x9D, 0xA3));

                // 2. Expandimos la ventana
                setSize(getWidth(), 750);
            }

            // 3. Forzamos a todo el árbol de contenedores a recalcularse y repintarse al instante
            mainContentGrid.updateUI(); // <-- Esto obliga a reubicar el botón de inmediato sin esperar
            revalidate();
            repaint();
        });

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

        configurationPanel.getConnectButton().addActionListener(e -> {
            onButtonConnect();
        });

        configurationPanel.getToggleCollapseButton().addActionListener(e -> {
            configurationPanel.toggleCollapseState();
            revalidate();
            repaint();
        });
    }

    private void onButtonConnect(){
        String selectedPort = (String) configurationPanel.getPortComboBox().getSelectedItem();
        boolean checkingConnectAction = configurationPanel.getConnectButton().getText().equalsIgnoreCase("Connect Instrument");

        if(!meterController.connectButtonPressed(selectedPort)){
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
        meterController.applyConfiguration(config);
    }

    @Override
    public void onMeasurementReceived(MeasurementDTO dto) {
        measurementPanel.setPrimary(dto.getTypeA()+" ("+dto.getSeriesMode()+")", dto.getValueA());
        measurementPanel.setSecondary(dto.getTypeB(), dto.getValueB());
        derivedPanel.updateDisplay(dto);
        realTimeChartPanel.updateData(dto);
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
    @Override
    public void  onDisconnected() {
        meterController.disconnect();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}