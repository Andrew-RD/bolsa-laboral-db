package visual;

import Datos.DashboardDAO;
import logico.AutorizacionService;
import logico.BolsaLaboral;
import logico.BrechaOfertaDemandaDTO;
import logico.Permiso;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

public class ConsultasGerenciales extends JDialog {

    private static final String TARJETA_BRECHA = "brecha-oferta-demanda";

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel tarjetas = new JPanel(cardLayout);
    private BrechaOfertaDemandaDialog dialogoResultados;

    public ConsultasGerenciales() {
        AutorizacionService.exigirPermiso(
                BolsaLaboral.getInstancia().getUsuarioActual(), Permiso.VER_INFORMES);
        setTitle("Consultas gerenciales");
        setIconImage(UIUtils.image("icono.png"));

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UIUtils.SURFACE);
        setContentPane(content);
        content.add(crearEncabezado(), BorderLayout.NORTH);
        content.add(crearMenu(), BorderLayout.WEST);

        tarjetas.setBackground(UIUtils.SURFACE);
        tarjetas.add(crearConsultaBrecha(), TARJETA_BRECHA);
        content.add(tarjetas, BorderLayout.CENTER);

        JButton cerrar = UIUtils.button("Cerrar", "cerrar.png");
        cerrar.addActionListener(event -> dispose());
        JPanel pie = UIUtils.buttonBar(UIUtils.TEAL);
        pie.add(cerrar);
        content.add(pie, BorderLayout.SOUTH);

        cardLayout.show(tarjetas, TARJETA_BRECHA);
        UIUtils.finishDialog(this, getOwner(), 900, 600);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(UIUtils.TEAL_DARK);
        encabezado.setBorder(UIUtils.emptyBorder(14, 18, 14, 18));
        JLabel titulo = new JLabel("Consultas gerenciales");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        encabezado.add(titulo, BorderLayout.WEST);
        return encabezado;
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(UIUtils.DARK_BACKGROUND);
        menu.setBorder(UIUtils.emptyBorder(16, 12, 16, 12));
        menu.setPreferredSize(new Dimension(UIUtils.scale(230), UIUtils.scale(400)));

        JLabel titulo = new JLabel("Consultas");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(UIUtils.h4Font(Font.BOLD));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        menu.add(titulo);
        menu.add(Box.createVerticalStrut(UIUtils.scale(12)));

        JButton brecha = UIUtils.button("Brecha oferta-demanda", "informes.png");
        brecha.setAlignmentX(LEFT_ALIGNMENT);
        brecha.setHorizontalAlignment(JButton.LEFT);
        brecha.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.scale(44)));
        brecha.setBackground(UIUtils.TEAL);
        brecha.setForeground(Color.WHITE);
        brecha.addActionListener(event -> cardLayout.show(tarjetas, TARJETA_BRECHA));
        menu.add(brecha);
        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private JPanel crearConsultaBrecha() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(34, 40, 34, 40));

        JLabel titulo = new JLabel("Brecha oferta vs. demanda por área laboral");
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        titulo.setForeground(UIUtils.TEAL_DARK);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));

        panel.add(crearTexto("Compara las ofertas activas con los candidatos desempleados " +
                "de cada área laboral."));
        panel.add(Box.createVerticalStrut(UIUtils.scale(12)));
        panel.add(crearEtiqueta("Decisión que apoya"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto("Permite identificar áreas con escasez de candidatos, equilibrio " +
                "o mayor disponibilidad de talento."));
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));
        panel.add(crearEtiqueta("Cómo interpretar el resultado"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto("Un balance negativo indica que faltan candidatos; cero representa " +
                "equilibrio; y un balance positivo muestra mayor disponibilidad."));
        panel.add(Box.createVerticalGlue());

        JButton verResultados = UIUtils.button("Ver resultados", "consulta.png");
        verResultados.setAlignmentX(LEFT_ALIGNMENT);
        verResultados.addActionListener(event -> verResultados());
        panel.add(verResultados);
        return panel;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(UIUtils.largeFont(Font.BOLD));
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        return etiqueta;
    }

    private JLabel crearTexto(String texto) {
        JLabel etiqueta = new JLabel("<html><div style='width:500px'>" + texto + "</div></html>");
        etiqueta.setFont(UIUtils.largeFont(Font.PLAIN));
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        return etiqueta;
    }

    private void verResultados() {
        if (dialogoResultados != null && dialogoResultados.isVisible()) {
            dialogoResultados.toFront();
            return;
        }

        Cursor anterior = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<BrechaOfertaDemandaDTO> resultados =
                    dashboardDAO.consultarBrechaPorAreaLaboral();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No existen áreas laborales para mostrar.",
                        "Consultas gerenciales", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            dialogoResultados = new BrechaOfertaDemandaDialog(this, resultados);
            dialogoResultados.setVisible(true);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(anterior);
        }
    }
}
