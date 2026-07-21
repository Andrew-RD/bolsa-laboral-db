package visual;

import logico.BolsaLaboral;
import logico.CentroEmpleador;
import logico.OfertaLaboral;
import logico.Universitario;
import logico.Usuario;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
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
                "contacto@example.com", "123456789");
        bolsa.registrarCentroTrabajo(centro);

        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        idiomas.add("Inglés");
        candidato = new Universitario("CAN-TEST", "00100100100", "Ana María", "Pérez Gómez",
                LocalDate.of(1995, 5, 20), "Femenino", "Distrito Nacional", "Santo Domingo",
                "8095559876", "ana@example.com", "Tiempo Completo", "Presencial", "TI",
                45000.0f, true, false, idiomas, "PUCMM", "Ingeniería de Sistemas",
                "Grado", "Desempleado");
        bolsa.registrarCandidato(candidato);

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
        checkWindow("Principal", new Principal());
        checkWindow("CV", new CV(candidato));
        checkWindow("ConsultarCandidatos", new ConsultarCandidatos());
        checkWindow("ConsultarCentros", new ConsultarCentros());
        checkWindow("ConsultarOfertas", new ConsultarOfertas());
        checkWindow("ConsultarSolicitudes", new ConsultarSolicitudes());
        checkWindow("InformeGeneral", new InformeGeneral());
        checkWindow("InformeOferta", new InformeOferta(oferta));
        checkWindow("ProcesamientoAvanzado", new ProcesamientoAvanzado());
        checkWindow("RegistroCandidato", new RegistroCandidato(null));
        checkWindow("RegistroCentro", new RegistroCentro(null));
        checkWindow("RegistroOfertaLaboral", new RegistroOfertaLaboral((OfertaLaboral) null));
        checkWindow("ResultadosVinculacion", new ResultadosVinculacion(oferta));
        checkWindow("VistaCentro", new VistaCentro(centro));
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
