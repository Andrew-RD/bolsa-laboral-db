package visual;

import Datos.DashboardDAO;
import logico.AutorizacionService;
import logico.BolsaLaboral;
import logico.BrechaOfertaDemandaDTO;
import logico.CoberturaOfertaDTO;
import logico.ManoObraMunicipioDTO;
import logico.Permiso;
import logico.TasaExitoCentroDTO;
import logico.TiempoResolucionAreaDTO;

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
    private static final String TARJETA_MANO_OBRA = "mano-obra-municipio";
    private static final String TARJETA_TASA_EXITO = "tasa-exito-centro";
    private static final String TARJETA_COBERTURA = "cobertura-ofertas";
    private static final String TARJETA_TIEMPO_RESOLUCION = "tiempo-resolucion-area";

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel tarjetas = new JPanel(cardLayout);
    private BrechaOfertaDemandaDialog dialogoResultadosBrecha;
    private ManoObraMunicipioDialog dialogoResultadosManoObra;
    private TasaExitoCentroDialog dialogoResultadosTasaExito;
    private CoberturaOfertaDialog dialogoResultadosCobertura;
    private TiempoResolucionAreaDialog dialogoResultadosTiempoResolucion;

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
        tarjetas.add(crearConsultaManoObra(), TARJETA_MANO_OBRA);
        tarjetas.add(crearConsultaTasaExito(), TARJETA_TASA_EXITO);
        tarjetas.add(crearConsultaCobertura(), TARJETA_COBERTURA);
        tarjetas.add(crearConsultaTiempoResolucion(), TARJETA_TIEMPO_RESOLUCION);
        content.add(tarjetas, BorderLayout.CENTER);

        JButton cerrar = UIUtils.button("Cerrar", "cerrar.png");
        cerrar.addActionListener(event -> dispose());
        JPanel pie = UIUtils.buttonBar(UIUtils.TEAL);
        pie.add(cerrar);
        content.add(pie, BorderLayout.SOUTH);

        cardLayout.show(tarjetas, TARJETA_BRECHA);
        UIUtils.finishDialog(this, getOwner(), 1040, 680);
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
        menu.setPreferredSize(new Dimension(UIUtils.scale(310), UIUtils.scale(440)));

        JLabel titulo = new JLabel("Consultas");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(UIUtils.h4Font(Font.BOLD));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        menu.add(titulo);
        menu.add(Box.createVerticalStrut(UIUtils.scale(12)));

        menu.add(crearBotonMenu("Balance por área",
                "Brecha oferta vs. demanda por área laboral", TARJETA_BRECHA));
        menu.add(Box.createVerticalStrut(UIUtils.scale(8)));

        menu.add(crearBotonMenu("Mano de obra por municipio",
                "Candidatos desempleados vs. vacantes disponibles por municipio",
                TARJETA_MANO_OBRA));
        menu.add(Box.createVerticalStrut(UIUtils.scale(8)));

        menu.add(crearBotonMenu("Conversión por centro",
                "Tasa de conversión de oportunidades por centro empleador",
                TARJETA_TASA_EXITO));
        menu.add(Box.createVerticalStrut(UIUtils.scale(8)));

        menu.add(crearBotonMenu("Cobertura de ofertas",
                "Ofertas activas con mayor dificultad de cobertura", TARJETA_COBERTURA));
        menu.add(Box.createVerticalStrut(UIUtils.scale(8)));

        menu.add(crearBotonMenu("Tiempo de resolución",
                "Tiempo promedio de resolución de vinculaciones por área laboral",
                TARJETA_TIEMPO_RESOLUCION));
        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private JButton crearBotonMenu(String texto, String tooltip, String tarjeta) {
        JButton boton = UIUtils.button(texto, "informes.png");
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.scale(44)));
        boton.setBackground(UIUtils.TEAL);
        boton.setForeground(Color.WHITE);
        boton.setToolTipText(tooltip);
        boton.addActionListener(event -> cardLayout.show(tarjetas, tarjeta));
        return boton;
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
        panel.add(crearTexto("El índice indica cuántos candidatos desempleados hay por cada oferta " +
                "activa: menor a 1 significa que faltan candidatos, 0 es equilibrio, y mayor a 1 " +
                "muestra mayor disponibilidad de talento."));
        panel.add(Box.createVerticalGlue());

        JButton verResultados = UIUtils.button("Ver resultados", "consulta.png");
        verResultados.setAlignmentX(LEFT_ALIGNMENT);
        verResultados.addActionListener(event -> verResultadosBrecha());
        panel.add(verResultados);
        return panel;
    }

    private JPanel crearConsultaManoObra() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(34, 40, 34, 40));

        JLabel titulo = new JLabel("Candidatos desempleados vs. vacantes disponibles por municipio");
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        titulo.setForeground(UIUtils.TEAL_DARK);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));

        panel.add(crearTexto("Compara, por municipio, la cantidad de candidatos desempleados " +
                "con la cantidad de vacantes disponibles en ofertas activas."));
        panel.add(Box.createVerticalStrut(UIUtils.scale(12)));
        panel.add(crearEtiqueta("Decisión que apoya"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto("Permite detectar zonas con exceso de mano de obra sin " +
                "oportunidades locales, para orientar campañas de reclutamiento o reubicación."));
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));
        panel.add(crearEtiqueta("Cómo interpretar el resultado"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto("El índice indica cuántos candidatos desempleados hay por cada vacante " +
                "disponible: mayor a 1 significa exceso de mano de obra, 1 es equilibrio, y menor a 1 " +
                "muestra más vacantes que candidatos."));
        panel.add(Box.createVerticalGlue());

        JButton verResultados = UIUtils.button("Ver resultados", "consulta.png");
        verResultados.setAlignmentX(LEFT_ALIGNMENT);
        verResultados.addActionListener(event -> verResultadosManoObra());
        panel.add(verResultados);
        return panel;
    }

    private JPanel crearConsultaTasaExito() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(34, 40, 34, 40));

        JLabel titulo = new JLabel("Tasa de conversión de oportunidades por centro empleador");
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        titulo.setForeground(UIUtils.TEAL_DARK);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));

        panel.add(crearTexto("Compara, por centro empleador, las oportunidades enviadas a " +
                "candidatos para sus ofertas con las que terminaron en una contratación."));
        panel.add(Box.createVerticalStrut(UIUtils.scale(12)));
        panel.add(crearEtiqueta("Decisión que apoya"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto("Permite identificar qué centros convierten mejor sus oportunidades " +
                "en contrataciones y cuáles generan vinculaciones sin resultados."));
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));
        panel.add(crearEtiqueta("Cómo interpretar el resultado"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto("La tasa de conversión es el porcentaje de oportunidades enviadas " +
                "para las ofertas del centro que terminaron en una contratación. Fórmula: " +
                "contrataciones logradas / oportunidades enviadas × 100."));
        panel.add(Box.createVerticalGlue());

        JButton verResultados = UIUtils.button("Ver resultados", "consulta.png");
        verResultados.setAlignmentX(LEFT_ALIGNMENT);
        verResultados.addActionListener(event -> verResultadosTasaExito());
        panel.add(verResultados);
        return panel;
    }

    private JPanel crearConsultaCobertura() {
        JPanel panel = crearPanelConsulta(
                "Ofertas activas con mayor dificultad de cobertura",
                "Identifica ofertas activas con vacantes pendientes, pocas oportunidades " +
                        "enviadas o ninguna contratación.",
                "Permite priorizar ofertas que necesitan mayor promoción, búsqueda activa de " +
                        "candidatos o revisión de sus requisitos.",
                "Una oferta presenta mayor dificultad cuando conserva vacantes disponibles y " +
                        "recibe pocas oportunidades o no logra contrataciones.");
        JButton verResultados = UIUtils.button("Ver resultados", "consulta.png");
        verResultados.setAlignmentX(LEFT_ALIGNMENT);
        verResultados.addActionListener(event -> verResultadosCobertura());
        panel.add(verResultados);
        return panel;
    }

    private JPanel crearConsultaTiempoResolucion() {
        JPanel panel = crearPanelConsulta(
                "Tiempo promedio de resolución de vinculaciones por área laboral",
                "Mide el tiempo transcurrido desde que la Bolsa Laboral envía una oportunidad " +
                        "al candidato hasta que se registra su resultado.",
                "Permite detectar áreas donde las vinculaciones permanecen pendientes durante " +
                        "demasiado tiempo y requieren seguimiento.",
                "El promedio considera solamente vinculaciones con fecha de decisión. Las " +
                        "vinculaciones que todavía están Enviadas se muestran separadamente " +
                        "como pendientes.");
        JButton verResultados = UIUtils.button("Ver resultados", "consulta.png");
        verResultados.setAlignmentX(LEFT_ALIGNMENT);
        verResultados.addActionListener(event -> verResultadosTiempoResolucion());
        panel.add(verResultados);
        return panel;
    }

    private JPanel crearPanelConsulta(String titulo, String descripcion,
                                      String decision, String interpretacion) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(34, 40, 34, 40));

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(UIUtils.h2Font(Font.BOLD));
        etiquetaTitulo.setForeground(UIUtils.TEAL_DARK);
        etiquetaTitulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(etiquetaTitulo);
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));
        panel.add(crearTexto(descripcion));
        panel.add(Box.createVerticalStrut(UIUtils.scale(12)));
        panel.add(crearEtiqueta("Decisión que apoya"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto(decision));
        panel.add(Box.createVerticalStrut(UIUtils.scale(18)));
        panel.add(crearEtiqueta("Cómo interpretar el resultado"));
        panel.add(Box.createVerticalStrut(UIUtils.scale(4)));
        panel.add(crearTexto(interpretacion));
        panel.add(Box.createVerticalGlue());
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

    private void verResultadosBrecha() {
        if (dialogoResultadosBrecha != null && dialogoResultadosBrecha.isVisible()) {
            dialogoResultadosBrecha.toFront();
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
            dialogoResultadosBrecha = new BrechaOfertaDemandaDialog(this, resultados);
            dialogoResultadosBrecha.setVisible(true);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(anterior);
        }
    }

    private void verResultadosManoObra() {
        if (dialogoResultadosManoObra != null && dialogoResultadosManoObra.isVisible()) {
            dialogoResultadosManoObra.toFront();
            return;
        }

        Cursor anterior = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<ManoObraMunicipioDTO> resultados =
                    dashboardDAO.consultarManoObraPorMunicipio();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No existen municipios para mostrar.",
                        "Consultas gerenciales", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            dialogoResultadosManoObra = new ManoObraMunicipioDialog(this, resultados);
            dialogoResultadosManoObra.setVisible(true);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(anterior);
        }
    }

    private void verResultadosTasaExito() {
        if (dialogoResultadosTasaExito != null && dialogoResultadosTasaExito.isVisible()) {
            dialogoResultadosTasaExito.toFront();
            return;
        }

        Cursor anterior = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<TasaExitoCentroDTO> resultados =
                    dashboardDAO.consultarTasaExitoPorCentro();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No existen centros empleadores con oportunidades para mostrar.",
                        "Consultas gerenciales", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            dialogoResultadosTasaExito = new TasaExitoCentroDialog(this, resultados);
            dialogoResultadosTasaExito.setVisible(true);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(anterior);
        }
    }

    private void verResultadosCobertura() {
        if (dialogoResultadosCobertura != null && dialogoResultadosCobertura.isVisible()) {
            dialogoResultadosCobertura.toFront();
            return;
        }

        Cursor anterior = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<CoberturaOfertaDTO> resultados =
                    dashboardDAO.consultarCoberturaOfertasActivas();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No existen ofertas activas para mostrar.",
                        "Consultas gerenciales", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            dialogoResultadosCobertura = new CoberturaOfertaDialog(this, resultados);
            dialogoResultadosCobertura.setVisible(true);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(anterior);
        }
    }

    private void verResultadosTiempoResolucion() {
        if (dialogoResultadosTiempoResolucion != null
                && dialogoResultadosTiempoResolucion.isVisible()) {
            dialogoResultadosTiempoResolucion.toFront();
            return;
        }

        Cursor anterior = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<TiempoResolucionAreaDTO> resultados =
                    dashboardDAO.consultarTiempoResolucionPorArea();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No existen áreas laborales para mostrar.",
                        "Consultas gerenciales", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            dialogoResultadosTiempoResolucion =
                    new TiempoResolucionAreaDialog(this, resultados);
            dialogoResultadosTiempoResolucion.setVisible(true);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(anterior);
        }
    }
}
