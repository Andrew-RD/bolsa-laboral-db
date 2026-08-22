package visual;

import logico.BolsaLaboral;
import logico.AutorizacionService;
import logico.Permiso;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Font;
import java.util.function.Supplier;

public class Principal extends JFrame {

    private JMenu mnGestion;
    private JMenu mnUsuarios;

    public Principal() {
        setTitle("Bolsa Laboral");
        setIconImage(UIUtils.image("icono.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
        setContentPane(new ScaledImagePanel("fondo.png"));

        UIUtils.finishFrame(this, 800, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);

        JMenu centros = menu("Centros de Trabajo", "empresa.png");
        addAuthorized(centros, Permiso.CONSULTAR_CENTROS,
                item("  Consultar", "consulta.png",
                        event -> openAuthorized(Permiso.CONSULTAR_CENTROS,
                                () -> new ConsultarCentros())));
        addAuthorized(centros, Permiso.GESTIONAR_CENTROS,
                item("  Registrar", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_CENTROS,
                                () -> new RegistroCentro(null))));
        addIfNotEmpty(menuBar, centros);

        JMenu candidatos = menu("Candidatos", "trabajador.png");
        addAuthorized(candidatos, Permiso.CONSULTAR_CANDIDATOS,
                item("  Consultar", "consulta.png",
                        event -> openAuthorized(Permiso.CONSULTAR_CANDIDATOS,
                                () -> new ConsultarCandidatos())));
        addAuthorized(candidatos, Permiso.GESTIONAR_CANDIDATOS,
                item("  Registrar", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_CANDIDATOS,
                                () -> new RegistroCandidato(null))));
        addIfNotEmpty(menuBar, candidatos);

        JMenu ofertas = menu("Catálogo de Ofertas", "conexion.png");
        addAuthorized(ofertas, Permiso.CONSULTAR_OFERTAS,
                item("  Consultar", "consulta.png",
                        event -> openAuthorized(Permiso.CONSULTAR_OFERTAS,
                                () -> new ConsultarOfertas())));
        addAuthorized(ofertas, Permiso.GESTIONAR_OFERTAS,
                item("  Registrar", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_OFERTAS,
                                () -> new RegistroOfertaLaboral((logico.OfertaLaboral) null))));
        addAuthorized(ofertas, Permiso.CONSULTAR_SOLICITUDES,
                item("  Solicitudes", "solicitud.png",
                        event -> openAuthorized(Permiso.CONSULTAR_SOLICITUDES,
                                () -> new ConsultarSolicitudes())));
        addIfNotEmpty(menuBar, ofertas);

        mnGestion = menu("Gestión de Datos", "gestion.png");
        addAuthorized(mnGestion, Permiso.USAR_PROCESAMIENTO_AVANZADO,
                item("  Procesamiento", "avanzado.png",
                        event -> openAuthorized(Permiso.USAR_PROCESAMIENTO_AVANZADO,
                                () -> new ProcesamientoAvanzado())));
        addAuthorized(mnGestion, Permiso.VER_INFORMES,
                item("  Informe", "informes.png",
                        event -> openAuthorized(Permiso.VER_INFORMES,
                                () -> new InformeGeneral())));
        addAuthorized(mnGestion, Permiso.VER_INFORMES,
                item("  Consultas gerenciales", "informes.png",
                        event -> openAuthorized(Permiso.VER_INFORMES,
                                () -> new ConsultasGerenciales())));
        addAuthorized(mnGestion, Permiso.GESTIONAR_CATALOGOS,
                item("  Catálogos", "gestion.png",
                        event -> openAuthorized(Permiso.GESTIONAR_CATALOGOS,
                                () -> new GestionCatalogos())));
        addIfNotEmpty(menuBar, mnGestion);

        mnUsuarios = menu("Gestión de Usuarios", "user.png");
        addAuthorized(mnUsuarios, Permiso.GESTIONAR_USUARIOS,
                item("  Consultar usuarios", "consulta.png",
                        event -> openAuthorized(Permiso.GESTIONAR_USUARIOS,
                                () -> new ConsultarUsuarios())));
        addAuthorized(mnUsuarios, Permiso.GESTIONAR_USUARIOS,
                item("  Registrar usuario", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_USUARIOS,
                                () -> new RegistroUsuario(null))));
        addIfNotEmpty(menuBar, mnUsuarios);

        return menuBar;
    }

    private JMenu menu(String text, String icon) {
        JMenu menu = new JMenu(text);
        menu.setIcon(UIUtils.icon(icon));
        menu.setForeground(Color.BLACK);
        menu.setFont(UIUtils.h4Font(Font.BOLD));
        return menu;
    }

    private JMenuItem item(String text, String icon, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text, UIUtils.icon(icon));
        item.setFont(UIUtils.largeFont(Font.PLAIN));
        item.addActionListener(listener);
        return item;
    }

    private void openModal(JDialog dialog) {
        dialog.setModal(true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openAuthorized(Permiso permiso, Supplier<JDialog> dialogFactory) {
        try {
            AutorizacionService.exigirPermiso(
                    BolsaLaboral.getInstancia().getUsuarioActual(), permiso);
            JDialog dialog = dialogFactory.get();
            openModal(dialog);
        } catch (SecurityException exception) {
            showUnauthorized(exception);
        }
    }

    private void addAuthorized(JMenu menu, Permiso permiso, JMenuItem item) {
        if (AutorizacionService.tienePermiso(
                BolsaLaboral.getInstancia().getUsuarioActual(), permiso)) {
            menu.add(item);
        }
    }

    private void addIfNotEmpty(JMenuBar menuBar, JMenu menu) {
        if (menu.getItemCount() > 0) {
            menuBar.add(menu);
        }
    }

    private void showUnauthorized(SecurityException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
                "Acción no autorizada", JOptionPane.WARNING_MESSAGE);
    }

}
