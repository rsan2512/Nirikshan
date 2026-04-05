package ui;

import java.awt.*;

public class Theme {

    // ── Colors ──
    public static final Color PRIMARY        = new Color(37, 99, 235);   // blue
    public static final Color PRIMARY_DARK   = new Color(29, 78, 216);   // dark blue
    public static final Color PRIMARY_LIGHT  = new Color(219, 234, 254); // light blue
    public static final Color SUCCESS        = new Color(22, 163, 74);   // green
    public static final Color DANGER         = new Color(220, 38, 38);   // red
    public static final Color WARNING        = new Color(234, 179, 8);   // yellow
    public static final Color BG_MAIN        = new Color(15, 23, 42);    // dark navy
    public static final Color BG_CARD        = new Color(30, 41, 59);    // card bg
    public static final Color BG_INPUT       = new Color(51, 65, 85);    // input bg
    public static final Color TEXT_PRIMARY   = new Color(248, 250, 252); // white
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // grey
    public static final Color BORDER         = new Color(71, 85, 105);   // border

    // ── Fonts ──
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_NAV      = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Reusable styled button ──
    public static JButtonStyled primaryButton(String text) {
        return new JButtonStyled(text, PRIMARY, PRIMARY_DARK, TEXT_PRIMARY);
    }

    public static JButtonStyled successButton(String text) {
        return new JButtonStyled(text, SUCCESS, new Color(15, 118, 56), TEXT_PRIMARY);
    }

    public static JButtonStyled dangerButton(String text) {
        return new JButtonStyled(text, DANGER, new Color(185, 28, 28), TEXT_PRIMARY);
    }
}