package visual;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Utilidades visuales compartidas por todas las ventanas Swing. */
public final class UIUtils {

    public static final Color DARK_BACKGROUND = new Color(4, 13, 18);
    public static final Color TEAL_DARK = new Color(24, 61, 61);
    public static final Color TEAL = new Color(4, 87, 87);
    public static final Color CANDIDATE_GREEN = new Color(92, 131, 116);
    public static final Color SURFACE = new Color(228, 228, 228);

    private static final Map<String, ImageIcon> ICONS = new HashMap<String, ImageIcon>();
    private static boolean lookAndFeelInitialized;

    private UIUtils() {
    }

    /** Debe invocarse una sola vez, antes de crear la primera ventana. */
    public static synchronized void initializeLookAndFeel() {
        if (lookAndFeelInitialized) {
            return;
        }
        FlatLaf.registerCustomDefaultsSource("visual");
        if (!FlatLightLaf.setup()) {
            throw new IllegalStateException("No fue posible inicializar FlatLaf.");
        }
        UIManager.put("Table.showHorizontalLines", Boolean.TRUE);
        UIManager.put("Table.showVerticalLines", Boolean.FALSE);
        UIManager.put("Table.rowHeight", Integer.valueOf(scale(30)));
        UIManager.put("Component.focusWidth", Integer.valueOf(scale(1)));
        lookAndFeelInitialized = true;
    }

    public static int scale(int value) {
        return UIScale.scale(value);
    }

    public static Dimension dimension(int width, int height) {
        return new Dimension(scale(width), scale(height));
    }

    public static Insets insets(int top, int left, int bottom, int right) {
        return new Insets(scale(top), scale(left), scale(bottom), scale(right));
    }

    public static EmptyBorder emptyBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(insets(top, left, bottom, right));
    }

    private static Font font(String key, int style) {
        Font font = UIManager.getFont(key);
        if (font == null) {
            font = UIManager.getFont("defaultFont");
        }
        if (font == null) {
            font = UIManager.getFont("Label.font");
        }
        if (font == null) {
            throw new IllegalStateException("FlatLaf no proporcionó la fuente de UI solicitada: " + key);
        }
        return font.deriveFont(style);
    }

    public static Font defaultFont(int style) {
        return font("defaultFont", style);
    }

    public static Font largeFont(int style) {
        return font("large.font", style);
    }

    public static Font h1Font(int style) {
        return font("h1.font", style);
    }

    public static Font h2Font(int style) {
        return font("h2.font", style);
    }

    public static Font h3Font(int style) {
        return font("h3.font", style);
    }

    public static Font h4Font(int style) {
        return font("h4.font", style);
    }

    public static URL resource(String fileName) {
        String path = "/recursos/" + fileName;
        URL url = UIUtils.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Recurso no encontrado en el classpath: " + path);
        }
        return url;
    }

    public static synchronized ImageIcon icon(String fileName) {
        ImageIcon cached = ICONS.get(fileName);
        if (cached == null) {
            cached = new ImageIcon(resource(fileName));
            ICONS.put(fileName, cached);
        }
        return cached;
    }

    public static Image image(String fileName) {
        return icon(fileName).getImage();
    }

    public static ImageIcon valueIcon(String value) {
        String normalized = value == null ? null : value.trim();
        if ("Enviada".equalsIgnoreCase(normalized)) {
            return icon("enviada.png");
        }
        if ("Aprobada".equalsIgnoreCase(normalized)) {
            return icon("aprobada.png");
        }
        if ("Rechazada".equalsIgnoreCase(normalized)) {
            return icon("rechazada.png");
        }

        try {
            return icon(toResourceKey(normalized) + ".png");
        } catch (IllegalArgumentException exception) {
            System.err.println("Advertencia: valor sin icono definido: " + String.valueOf(value)
                    + ". Se usará nodefinido.png.");
            return icon("nodefinido.png");
        } catch (IllegalStateException exception) {
            System.err.println("Advertencia: no existe un icono para el valor '" + String.valueOf(value)
                    + "'. Se usará nodefinido.png.");
            return icon("nodefinido.png");
        }
    }

    public static String toResourceKey(String value) {
        if (value == null) {
            throw new IllegalArgumentException("No se puede resolver un recurso para un valor nulo.");
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("No se puede resolver un recurso para: " + value);
        }
        return normalized;
    }

    public static JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE);
        return panel;
    }

    public static JPanel titledPanel(String title) {
        JPanel panel = formPanel();
        Border line = new LineBorder(Color.BLACK, scale(1), true);
        panel.setBorder(new TitledBorder(line, title, TitledBorder.LEADING, TitledBorder.TOP,
                h4Font(Font.PLAIN), Color.BLACK));
        return panel;
    }

    public static GridBagConstraints constraints(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    public static void addFormRow(JPanel panel, int row, String labelText, Component field) {
        addFormRow(panel, row, labelText, field, null);
    }

    public static void addFormRow(JPanel panel, int row, String labelText, Component field, Component trailing) {
        JLabel label = new JLabel(labelText);
        label.setFont(largeFont(Font.BOLD));
        GridBagConstraints labelConstraints = constraints(0, row);
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = constraints(1, row);
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, fieldConstraints);

        if (trailing != null) {
            GridBagConstraints trailingConstraints = constraints(2, row);
            trailingConstraints.insets = insets(6, 0, 6, 8);
            panel.add(trailing, trailingConstraints);
        }
    }

    public static void addFullWidth(JPanel panel, Component component, int row) {
        GridBagConstraints constraints = constraints(0, row);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, constraints);
    }

    public static void addVerticalFiller(JPanel panel, int row) {
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        GridBagConstraints constraints = constraints(0, row);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(filler, constraints);
    }

    public static JScrollPane scrollable(Component content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(scale(18));
        return scrollPane;
    }

    public static void configureTable(JTable table) {
        table.setFont(defaultFont(Font.PLAIN));
        table.setForeground(Color.BLACK);
        table.setBackground(SURFACE);
        table.setRowHeight(scale(30));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(defaultFont(Font.BOLD));
    }

    public static JPanel buttonBar(Color background) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, scale(8), scale(8)));
        panel.setBackground(background);
        panel.setBorder(emptyBorder(2, 8, 2, 8));
        return panel;
    }

    public static JButton button(String text, String iconName) {
        JButton button = new JButton(text, icon(iconName));
        button.setFont(largeFont(Font.BOLD));
        button.setBackground(Color.WHITE);
        return button;
    }

    public static JLabel iconLabel(String iconName) {
        return new JLabel(icon(iconName), SwingConstants.CENTER);
    }

    public static void finishDialog(final JDialog dialog, Window owner, int minWidth, int minHeight) {
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);
        dialog.pack();
        fitToScreen(dialog, minWidth, minHeight);
        dialog.setLocationRelativeTo(owner);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close-dialog");
        dialog.getRootPane().getActionMap().put("close-dialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.dispose();
            }
        });
    }

    public static void finishFrame(Frame frame, int minWidth, int minHeight) {
        frame.setResizable(true);
        frame.pack();
        fitToScreen(frame, minWidth, minHeight);
        frame.setLocationRelativeTo(null);
    }

    private static void fitToScreen(Window window, int minWidth, int minHeight) {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screen = environment.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        Rectangle usable = environment.getMaximumWindowBounds().intersection(screen);
        if (usable.isEmpty()) {
            usable = screen;
        }
        int margin = scale(32);
        int maximumWidth = Math.max(scale(320), usable.width - margin);
        int maximumHeight = Math.max(scale(240), usable.height - margin);
        int width = Math.min(maximumWidth, Math.max(window.getWidth(), scale(minWidth)));
        int height = Math.min(maximumHeight, Math.max(window.getHeight(), scale(minHeight)));
        window.setMinimumSize(new Dimension(Math.min(scale(minWidth), maximumWidth),
                Math.min(scale(minHeight), maximumHeight)));
        window.setSize(width, height);
    }
}
