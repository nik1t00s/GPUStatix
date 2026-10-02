package com.gpustatix.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

/** Shared desktop palette and spacing for the Swing controls. */
public final class StudioTheme {
    public static final Color BACKGROUND = new Color(16, 23, 34);
    public static final Color SURFACE = new Color(24, 37, 54);
    public static final Color TEXT = new Color(231, 237, 247);
    public static final Color ACCENT = new Color(101, 214, 187);
    private StudioTheme() { }

    public static void install() {
        FontUIResource font = new FontUIResource("SansSerif", Font.PLAIN, 13);
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (key.toString().endsWith(".font")) UIManager.put(key, font);
        }
        for (String name : new String[] {"Panel", "Viewport", "ScrollPane", "TabbedPane", "Label", "OptionPane"}) {
            UIManager.put(name + ".background", new ColorUIResource(BACKGROUND));
            UIManager.put(name + ".foreground", new ColorUIResource(TEXT));
        }
        UIManager.put("TextField.background", new ColorUIResource(SURFACE));
        UIManager.put("TextField.foreground", new ColorUIResource(TEXT));
        UIManager.put("TextField.caretForeground", new ColorUIResource(ACCENT));
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 85, 112)), new EmptyBorder(8, 10, 8, 10)));
        UIManager.put("Button.background", new ColorUIResource(SURFACE));
        UIManager.put("Button.foreground", new ColorUIResource(TEXT));
        UIManager.put("Button.border", new EmptyBorder(10, 16, 10, 16));
        UIManager.put("TabbedPane.selected", new ColorUIResource(SURFACE));
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(12, 12, 12, 12));
    }
}
