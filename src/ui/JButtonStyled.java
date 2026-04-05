package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JButtonStyled extends JButton {

    private final Color normalColor;
    private final Color hoverColor;
    private final Color textColor;
    private boolean hovered = false;

    public JButtonStyled(String text, Color normalColor,
                          Color hoverColor, Color textColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor  = hoverColor;
        this.textColor   = textColor;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(Theme.FONT_BUTTON);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width, 42));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovered = true; repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = false; repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? hoverColor : normalColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        super.paintComponent(g);
        g2.dispose();
    }
}