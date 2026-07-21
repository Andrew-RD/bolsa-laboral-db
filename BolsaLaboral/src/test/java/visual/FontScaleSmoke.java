package visual;

import javax.swing.UIManager;
import java.awt.Font;

/** Imprime métricas comparables entre ejecuciones con distinta flatlaf.uiScale. */
public final class FontScaleSmoke {

    private FontScaleSmoke() {
    }

    public static void main(String[] args) {
        UIUtils.initializeLookAndFeel();
        Login login = new Login();
        try {
            System.out.println("FONT_SCALE scaleProperty=" + System.getProperty("flatlaf.uiScale", "normal")
                    + " scaled100=" + UIUtils.scale(100)
                    + " default=" + size("defaultFont")
                    + " large=" + size("large.font")
                    + " h1=" + size("h1.font")
                    + " label=" + size("Label.font")
                    + " button=" + size("Button.font")
                    + " table=" + size("Table.font")
                    + " textField=" + size("TextField.font")
                    + " menu=" + size("Menu.font")
                    + " tabbedPane=" + size("TabbedPane.font")
                    + " login=" + login.getWidth() + "x" + login.getHeight());
        } finally {
            login.dispose();
        }
    }

    private static float size(String key) {
        Font font = UIManager.getFont(key);
        if (font == null) {
            throw new AssertionError("Fuente de UI ausente: " + key);
        }
        return font.getSize2D();
    }
}
