package gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

import static et431.util.Constants.*;

/**
 * UI panel for displaying real-time primary and secondary LCR measurements.
 * Refactored with a premium, high-contrast dark tech aesthetic matching InfoPanel.
 */
public class MeasurementPanel extends JPanel {

    private JLabel lblPrimaryName;
    private JLabel lblPrimaryValue;

    private JLabel lblSecondaryName;
    private JLabel lblSecondaryValue;

    private JLabel lblComponentIcon; // Component graphic placeholder label

    // --- HIGH-CONTRAST TECH PALETTE (Unified with InfoPanel) ---
    private final Color BG_PANEL = new Color(17, 24, 39);          // Deep Dark Slate
    private final Color BG_LCD = new Color(13, 16, 23);            // Deep Black/Slate for LCD
    private final Color COLOR_SECONDARY = new Color(0, 255, 204);    // Cyan
    private final Color COLOR_PRIMARY = new Color(255, 170, 0);  // Orange
    private final Color TEXT_MUTED = new Color(156, 163, 175);     // Muted Gray for Labels
    private final Color COMPONENT = new Color(218, 255, 0);
    private final Color BADGE_OK = new Color(40, 167, 69);
    private final Color BADGE_WARN = new Color(220, 53, 69);

    public MeasurementPanel() {
        initialize();
    }

    private void initialize() {
        setBackground(BG_PANEL);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setLayout(new BorderLayout(0, 15));

        JPanel pnlDisplayContainer = new JPanel(new GridLayout(2, 1, 0, 12));
        pnlDisplayContainer.setBackground(BG_PANEL);

        Font fontLabels = new Font("SansSerif", Font.BOLD, 18);
        Font fontPrimaryValue = new Font("Monospaced", Font.BOLD, 54);
        Font fontSecondaryValue = new Font("Monospaced", Font.BOLD, 32);

        // --- PRIMARY DISPLAY ---
        JPanel pnlPrimaryLCD = new JPanel(new BorderLayout(15, 0));
        styleLcdBox(pnlPrimaryLCD);

        JPanel pnlPrimaryTop = new JPanel(new BorderLayout());
        pnlPrimaryTop.setOpaque(false);

        lblPrimaryName = new JLabel("AUTO");
        lblPrimaryName.setFont(fontLabels);
        lblPrimaryName.setForeground(COLOR_PRIMARY);
        pnlPrimaryTop.add(lblPrimaryName, BorderLayout.WEST);

        // SUBPANEL PARA AGRUPAR BADGE E ICONO EN LA DERECHA
        JPanel pnlRightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightGroup.setOpaque(false);


        lblComponentIcon = new JLabel();
        lblComponentIcon.setIcon(null); // Default placeholder (Z)
        pnlRightGroup.add(lblComponentIcon); // Añadido segundo (Derecha del badge)

        pnlPrimaryTop.add(pnlRightGroup, BorderLayout.EAST);

        pnlPrimaryLCD.add(pnlPrimaryTop, BorderLayout.NORTH);

        lblPrimaryValue = new JLabel("-----");
        lblPrimaryValue.setFont(fontPrimaryValue);
        lblPrimaryValue.setForeground(COLOR_PRIMARY);
        lblPrimaryValue.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlPrimaryLCD.add(lblPrimaryValue, BorderLayout.CENTER);

        // --- SECONDARY DISPLAY ---
        JPanel pnlSecondaryLCD = new JPanel(new BorderLayout(15, 0));
        styleLcdBox(pnlSecondaryLCD);

        lblSecondaryName = new JLabel("SECONDARY");
        lblSecondaryName.setFont(fontLabels);
        lblSecondaryName.setForeground(COLOR_SECONDARY);
        pnlSecondaryLCD.add(lblSecondaryName, BorderLayout.NORTH);

        lblSecondaryValue = new JLabel("-----");
        lblSecondaryValue.setFont(fontSecondaryValue);
        lblSecondaryValue.setForeground(COLOR_SECONDARY);
        lblSecondaryValue.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlSecondaryLCD.add(lblSecondaryValue, BorderLayout.CENTER);

        pnlDisplayContainer.add(pnlPrimaryLCD);
        pnlDisplayContainer.add(pnlSecondaryLCD);

        add(pnlDisplayContainer, BorderLayout.CENTER);
    }

    private void styleLcdBox(JPanel panel) {
        panel.setBackground(BG_LCD);
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(30,35,45), new Color(10,12,16)),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                )
        ));
    }

    public void setPrimary(String name, String value) {
        lblPrimaryName.setText(name);
        lblPrimaryValue.setText(value);

        updateComponentGraphic(name);


    }

    public void setSecondary(String name, String value) {
        lblSecondaryName.setText(name);
        lblSecondaryValue.setText(value);
    }

    public void clear() {
        lblPrimaryName.setText("PRIMARY");
        lblPrimaryValue.setText("-----");
        lblSecondaryName.setText("SECONDARY");
        lblSecondaryValue.setText("-----");
        lblComponentIcon.setIcon(new ComponentIcon("Z", COLOR_PRIMARY));
    }

    private void updateComponentGraphic(String name) {
        if (name == null) return;
        String token = name;
        lblComponentIcon.setIcon(new ComponentIcon("R", COMPONENT));
        if(name.startsWith("AUTO")){
            lblComponentIcon.setIcon(null);
        }
        if(name.startsWith(LABEL_CAPACITANCE)){
            lblComponentIcon.setIcon(new ComponentIcon("C", COMPONENT));
        }
        if(name.startsWith(LABEL_INDUCTANCE)){
            lblComponentIcon.setIcon(new ComponentIcon("L", COMPONENT));
        }
    }

    // --- PRIVATE VECTOR GRAPHIC GENERATOR ---
    private static class ComponentIcon implements Icon {
        private final String type;
        private final int width = 36;
        private final int height = 24;
        private final Color color;

        public ComponentIcon(String type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int midY = y + (height / 2);

            switch (type) {
                case "R":
                    g2.drawLine(x, midY, x + 6, midY);
                    g2.drawLine(x + 6, midY, x + 10, y + 4);
                    g2.drawLine(x + 10, y + 4, x + 16, y + 20);
                    g2.drawLine(x + 16, y + 20, x + 22, y + 4);
                    g2.drawLine(x + 22, y + 4, x + 26, y + 20);
                    g2.drawLine(x + 26, y + 20, x + 30, midY);
                    g2.drawLine(x + 30, midY, x + width, midY);
                    break;

                case "C":
                    g2.drawLine(x, midY, x + 15, midY);
                    g2.drawLine(x + 15, y + 3, x + 15, y + 21);
                    g2.drawLine(x + 21, y + 3, x + 21, y + 21);
                    g2.drawLine(x + 21, midY, x + width, midY);
                    break;

                case "L":
                    g2.drawLine(x, midY, x + 4, midY);
                    g2.drawArc(x + 4, midY - 6, 8, 12, 0, 180);
                    g2.drawArc(x + 11, midY - 6, 8, 12, 0, 180);
                    g2.drawArc(x + 18, midY - 6, 8, 12, 0, 180);
                    g2.drawLine(x + 26, midY, x + width, midY);
                    break;

                default:
                    g2.drawLine(x, midY, x + 6, midY);
                    g2.drawRect(x + 6, y + 5, 24, 14);
                    g2.drawLine(x + 30, midY, x + width, midY);
                    break;
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }
}