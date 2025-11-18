package utils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class FontUtil {
    /**
     * Apply a global default font (family chain) to all Swing UI defaults.
     * Call this BEFORE creating any Swing components/windows.
     *
     * @param families Preferred font families in order, e.g. {"Segoe UI", "San Francisco", "Ubuntu", "SansSerif"}
     */
    public static void applyGlobalFont(String... families) {
        if (families == null || families.length == 0) return;

        String chosen = chooseAvailableFamily(families);
        Font base = UIManager.getFont("Label.font");
        if (base == null) base = new JLabel().getFont();

        // keep existing sizes per key; only swap family and plain style
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();
        Set<Object> updated = new HashSet<>();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = defaults.get(key);
            if (val instanceof FontUIResource f) {
                Font newFont = new Font(chosen, Font.PLAIN, f.getSize());
                defaults.put(key, new FontUIResource(newFont));
                updated.add(key);
            } else if (val instanceof Font f) {
                Font newFont = new Font(chosen, Font.PLAIN, f.getSize());
                defaults.put(key, new FontUIResource(newFont));
                updated.add(key);
            }
        }
    }

    private static String chooseAvailableFamily(String[] families) {
        Set<String> installed = new HashSet<>();
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            installed.add(name.toLowerCase());
        }
        for (String fam : families) {
            if (fam != null && installed.contains(fam.toLowerCase())) return fam;
        }
        return "SansSerif"; // fallback logical font
    }
}
