package gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Status bar component that provides real-time connection status and instrument metadata.
 */
public class StatusBarPanel extends JPanel {

    private final JLabel lblStatus;
    private final JLabel lblPort;
    private final JLabel lblManufacter;
    private final JLabel lblModel;
    private final JLabel lblFirmware;
    private final Color LABEL_COLOR = new Color(110, 120, 135);
    private final Color VALUE_COLOR = new Color(40, 44, 52);
    private final Color CONN_COLOR = new Color(34, 139, 34);
    private final Color DISC_COLOR = new Color(178, 34, 34);

    public StatusBarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setBackground(new Color(245, 246, 248));
        Font panelFont = new Font("Monospaced", Font.BOLD, 12);

        lblStatus = new JLabel();
        lblStatus.setFont(panelFont);
        lblPort = new JLabel();
        lblPort.setFont(panelFont);
        lblManufacter = new JLabel();
        lblManufacter.setFont(panelFont);
        lblModel = new JLabel();
        lblModel.setFont(panelFont);
        lblFirmware = new JLabel();
        lblFirmware.setFont(panelFont);

        clear();
        add(lblStatus);
        add(createSeparator());
        add(lblPort);
        add(createSeparator());
        add(lblManufacter);
        add(createSeparator());
        add(lblModel);
        add(createSeparator());
        add(lblFirmware);
    }

    private JLabel createSeparator() {
        JLabel sep = new JLabel("|");
        sep.setFont(new Font("Dialog", Font.PLAIN, 12));
        sep.setForeground(new Color(200, 200, 200));
        return sep;
    }

    private String formatText(String label, String value, String colorHex) {
        String labelHex = String.format("#%02x%02x%02x", LABEL_COLOR.getRed(), LABEL_COLOR.getGreen(), LABEL_COLOR.getBlue());
        return "<html><font color='" + labelHex + "'>" + label + ": </font><font color='" + colorHex + "'>" + value + "</font></html>";
    }

    private String formatText(String label, String value, Color valueColor) {
        String hex = String.format("#%02x%02x%02x", valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue());
        return formatText(label, value, hex);
    }

    public void setStatus(String status) {
        // Fallback to disconnected color if string doesn't contain connection keywords
        Color statusColor = status.toLowerCase().contains("connect") ? CONN_COLOR : DISC_COLOR;
        lblStatus.setText(formatText("Status", status.toUpperCase(), statusColor));
    }

    public void setPort(String port) {
        lblPort.setText(formatText("Port", port, VALUE_COLOR));
    }

    public void setManufacter(String manufacter) {
        lblManufacter.setText(formatText("Manufacter", manufacter, VALUE_COLOR));
    }

    public void setModel(String model) {
        lblModel.setText(formatText("Model", model, VALUE_COLOR));
    }

    public void setFirmware(String firmware) {
        lblFirmware.setText(formatText("Firmware", firmware, VALUE_COLOR));
    }

    public void clear() {
        setStatus("Disconnected");
        setPort("-");
        setModel("-");
        setFirmware("-");
    }
}