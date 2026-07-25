package visual;

import logico.BolsaLaboral;
import logico.CentroEmpleador;
import logico.OfertaLaboral;
import logico.Permiso;
import logico.Solicitud;
import logico.Universitario;
import logico.Usuario;
import logico.RolUsuario;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;

/** Smoke test gráfico manual; no modifica datos persistidos ni lógica de negocio. */
public final class VisualSmoke {

    private static CentroEmpleador centro;
    private static Universitario candidato;
    private static OfertaLaboral oferta;

    private VisualSmoke() {
    }

    public static void main(String[] args) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                throw new IllegalStateException("El smoke test visual requiere un entorno gráfico.");
            }
            UIUtils.initializeLookAndFeel();
            System.out.println("SCREEN bounds=" + GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds()
                    + " maximum=" + GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            seedData();
            if (args.length == 2 && "--capture-login".equals(args[0])) {
                captureLogin(args[1]);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        runAllWindows();
                    }
                });
            }
            System.out.println("SMOKE_OK");
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
    }

    private static void seedData() {
        BolsaLaboral bolsa = BolsaLaboral.getInstancia();
        bolsa.getCandidatos().clear();
        bolsa.getCentros().clear();
        bolsa.getOfertas().clear();
        bolsa.getSolicitudes().clear();
        bolsa.getVacantes().clear();
        bolsa.getUsuarios().clear();
        Usuario admin = new Usuario("Admin", "Admin", "Admin");
        bolsa.regUsuario(admin);
        bolsa.setUsuarioActual(admin);

        centro = new CentroEmpleador("CEN-TEST", "Centro de Pruebas", "Tecnología",
                "Distrito Nacional", "Santo Domingo", "8095551234",
                "contacto@example.com", "101010101");
        bolsa.registrarCentroTrabajo(centro);

        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        idiomas.add("Inglés");
        candidato = new Universitario("CAN-TEST", "00100000009", "Ana María", "Pérez Gómez",
                LocalDate.of(1995, 5, 20), "Femenino", "Distrito Nacional", "Santo Domingo",
                "8095559876", "ana@example.com", "Tiempo Completo", "Presencial", "TI",
                45000.0f, true, false, idiomas, "PUCMM", "Ingeniería de Sistemas",
                "Grado", "Desempleado");
        bolsa.registrarCandidato(candidato);
        candidato.migrarUniversidadDeserializada(bolsa.getCatalogos());

        ArrayList<String> requisitos = new ArrayList<String>();
        requisitos.add("Ingeniería de Sistemas");
        oferta = new OfertaLaboral("OFR-TEST", "Desarrollador de Software",
                "Construcción y mantenimiento de aplicaciones.", "TI", "Presencial",
                "Tiempo Completo", "Activa", 65000.0f, 0, 2, centro,
                false, true, false, "Estudiante Universitario", requisitos, idiomas, 40);
        bolsa.registrarOfertaLaboral(oferta);
    }

    private static void runAllWindows() {
        checkWindow("Login", new Login());
        Principal principalAdmin = new Principal();
        assertMenu(principalAdmin, "Gestión de Datos", true);
        assertMenu(principalAdmin, "Gestión de Usuarios", true);
        checkWindow("Principal admin", principalAdmin);
        checkWindow("CV", new CV(candidato));
        checkWindow("ConsultarCandidatos", new ConsultarCandidatos());
        checkWindow("ConsultarCentros", new ConsultarCentros());
        checkWindow("ConsultarOfertas", new ConsultarOfertas());
        checkWindow("ConsultarSolicitudes", new ConsultarSolicitudes());
        checkWindow("InformeGeneral", new InformeGeneral());
        checkWindow("InformeOferta", new InformeOferta(oferta));
        checkWindow("ProcesamientoAvanzado", new ProcesamientoAvanzado());
        RegistroCandidato registroCandidato = new RegistroCandidato(null);
        assertLabel(registroCandidato,
                "Desempleado — se actualiza automáticamente al aprobar una solicitud");
        assertComboItem(registroCandidato,
                "PUCMM — Pontificia Universidad Católica Madre y Maestra");
        checkWindow("RegistroCandidato", registroCandidato);
        RegistroCandidato modificarCandidato =
                new RegistroCandidato(candidato);
        assertLabel(modificarCandidato,
                candidato.getDescripcionEstadoLaboral());
        checkWindow("ModificarCandidato", modificarCandidato);
        checkWindow("RegistroCentro", new RegistroCentro(null));
        checkWindow("RegistroOfertaLaboral", new RegistroOfertaLaboral((OfertaLaboral) null));
        checkWindow("RegistroUsuario", new RegistroUsuario(null));
        checkWindow("ConsultarUsuarios", new ConsultarUsuarios());
        checkWindow("GestionCatalogos", new GestionCatalogos());
        checkWindow("ResultadosVinculacion", new ResultadosVinculacion(oferta));
        checkWindow("VistaCentro", new VistaCentro(centro));

        BolsaLaboral bolsa = BolsaLaboral.getInstancia();
        Usuario admin = bolsa.getUsuarioActual();
        Usuario empleado = new Usuario("Empleado de prueba", "empleado-menu",
                "empleado-menu@example.test", RolUsuario.EMPLEADO, true,
                "ClaveTemporal1".toCharArray());
        bolsa.regUsuario(empleado);
        bolsa.setUsuarioActual(empleado);
        Principal principalEmpleado = new Principal();
        assertMenu(principalEmpleado, "Gestión de Datos", false);
        assertMenu(principalEmpleado, "Gestión de Usuarios", false);
        checkWindow("Principal empleado", principalEmpleado);

        ConsultarOfertas ofertasEmpleado = new ConsultarOfertas();
        assertButtonVisible(ofertasEmpleado, "Procesar", false);
        checkWindow("ConsultarOfertas empleado sin permiso avanzado",
                ofertasEmpleado);

        EnumSet<Permiso> permisosEmpleado = empleado.getPermisos();
        permisosEmpleado.add(Permiso.USAR_PROCESAMIENTO_AVANZADO);
        empleado.setPermisos(permisosEmpleado);
        Principal principalEmpleadoAutorizado = new Principal();
        assertMenu(principalEmpleadoAutorizado, "Gestión de Datos", true);
        checkWindow("Principal empleado con procesamiento avanzado",
                principalEmpleadoAutorizado);
        ConsultarOfertas ofertasEmpleadoAutorizado = new ConsultarOfertas();
        assertButtonVisible(ofertasEmpleadoAutorizado, "Procesar", true);
        checkWindow("ConsultarOfertas empleado autorizado",
                ofertasEmpleadoAutorizado);
        bolsa.setUsuarioActual(admin);

        bolsa.getCandidatos().clear();
        bolsa.getCentros().clear();
        bolsa.getOfertas().clear();
        bolsa.getSolicitudes().clear();
        bolsa.getVacantes().clear();
        bolsa.getCandidatos().add(null);
        bolsa.getCentros().add(null);
        bolsa.getOfertas().add(null);
        bolsa.getSolicitudes().add(null);
        bolsa.getSolicitudes().add(new Solicitud(
                "SOL-LEGADA-INCOMPLETA", LocalDate.now(), "Enviada", null, null));
        checkWindow("ConsultarCandidatos vacío", new ConsultarCandidatos());
        checkWindow("ConsultarCentros vacío", new ConsultarCentros());
        checkWindow("ConsultarOfertas vacío", new ConsultarOfertas());
        checkWindow("ConsultarSolicitudes vacío", new ConsultarSolicitudes());
        checkWindow("InformeGeneral vacío", new InformeGeneral());
    }

    private static void checkWindow(String name, Window window) {
        int constructedWidth = window.getWidth();
        int constructedHeight = window.getHeight();
        if (window instanceof Dialog) {
            ((Dialog) window).setModal(false);
        }
        window.setVisible(true);
        window.validate();
        if (window.getWidth() <= 0 || window.getHeight() <= 0) {
            throw new AssertionError(name + " tiene un tamaño inválido.");
        }
        if (window instanceof JDialog && !((JDialog) window).isResizable()) {
            throw new AssertionError(name + " no permite redimensionar.");
        }
        int components = countComponents(window);
        if (components == 0) {
            throw new AssertionError(name + " no contiene componentes.");
        }
        System.out.println(name + " constructed=" + constructedWidth + "x" + constructedHeight
                + " visible=" + window.getWidth() + "x" + window.getHeight()
                + " components=" + components);
        window.dispose();
    }

    private static int countComponents(Component component) {
        int count = 1;
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                count += countComponents(child);
            }
        }
        return count;
    }

    private static void assertMenu(Principal principal, String nombre, boolean esperado) {
        boolean encontrado = false;
        for (int index = 0; index < principal.getJMenuBar().getMenuCount(); index++) {
            if (principal.getJMenuBar().getMenu(index) != null
                    && nombre.equals(principal.getJMenuBar().getMenu(index).getText())) {
                encontrado = true;
                break;
            }
        }
        if (encontrado != esperado) {
            throw new AssertionError("Visibilidad incorrecta del menú " + nombre
                    + ": esperado=" + esperado + ", actual=" + encontrado);
        }
    }

    private static void assertLabel(Container container, String texto) {
        if (!containsLabel(container, texto)) {
            throw new AssertionError("No se encontró el texto visible: " + texto);
        }
    }

    private static boolean containsLabel(Container container, String texto) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel
                    && texto.equals(((JLabel) component).getText())) {
                return true;
            }
            if (component instanceof Container
                    && containsLabel((Container) component, texto)) {
                return true;
            }
        }
        return false;
    }

    private static void assertButtonVisible(
            Container container, String texto, boolean esperado) {
        JButton boton = findButton(container, texto);
        if (boton == null || boton.isVisible() != esperado) {
            throw new AssertionError("Visibilidad incorrecta del botón " + texto
                    + ": esperado=" + esperado + ", botón=" + boton);
        }
    }

    private static JButton findButton(Container container, String texto) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton
                    && texto.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton encontrado = findButton(
                        (Container) component, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private static void assertComboItem(Container container, String texto) {
        if (!containsComboItem(container, texto)) {
            throw new AssertionError(
                    "No se encontró el elemento de ComboBox: " + texto);
        }
    }

    private static boolean containsComboItem(
            Container container, String texto) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox) component;
                for (int index = 0; index < combo.getItemCount(); index++) {
                    if (texto.equals(String.valueOf(combo.getItemAt(index)))) {
                        return true;
                    }
                }
            }
            if (component instanceof Container
                    && containsComboItem((Container) component, texto)) {
                return true;
            }
        }
        return false;
    }

    private static void captureLogin(final String outputPath) throws Exception {
        final Login[] login = new Login[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                login[0] = new Login();
                login[0].setVisible(true);
                login[0].toFront();
            }
        });
        Robot robot = new Robot();
        robot.delay(800);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        BufferedImage image = robot.createScreenCapture(screen);
        ImageIO.write(image, "png", new File(outputPath));
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                login[0].dispose();
            }
        });
        System.out.println("CAPTURE_OK " + outputPath + " " + screen.width + "x" + screen.height);
    }
}
