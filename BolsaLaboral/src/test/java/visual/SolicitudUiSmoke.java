package visual;

import logico.BolsaLaboral;
import logico.Candidato;
import logico.CentroEmpleador;
import logico.Obrero;
import logico.OfertaLaboral;
import logico.Solicitud;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;

/** Smoke test gráfico en memoria para InformeOferta y ConsultarSolicitudes. */
public final class SolicitudUiSmoke {

    private static BolsaLaboral bolsa;
    private static Candidato candidato;
    private static CentroEmpleador centro;

    private SolicitudUiSmoke() {
    }

    public static void main(String[] args) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                throw new IllegalStateException("El smoke test requiere un entorno gráfico.");
            }
            UIUtils.initializeLookAndFeel();
            bolsa = BolsaLaboral.getInstancia();
            verificarInformeConEstadoLegado();
            verificarBotonesDeProcesamiento();
            if (args.length == 1) {
                capturarConsultarSolicitudes(args[0]);
            }
            System.out.println("SOLICITUD_UI_OK informe=true icono=aprobada.png botones=true");
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
    }

    private static void verificarInformeConEstadoLegado() throws Exception {
        limpiarDatos();
        crearParticipantes();
        OfertaLaboral oferta = oferta("OFR-LEGACY", 1);
        Solicitud legado = solicitud("SOL-LEGACY", oferta, " Aprovada ");

        if (bolsa.migrarDatosDeserializados() < 1 || !Solicitud.ESTADO_APROBADA.equals(legado.getEstado())) {
            throw new AssertionError("La solicitud legada no fue normalizada a Aprobada.");
        }
        if (UIUtils.valueIcon(legado.getEstado()) != UIUtils.icon("aprobada.png")) {
            throw new AssertionError("Aprobada no utiliza aprobada.png.");
        }
        if (UIUtils.class.getResource("/recursos/aprovada.png") != null) {
            throw new AssertionError("Existe un recurso incorrecto aprovada.png.");
        }

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                InformeOferta informe = new InformeOferta(oferta);
                informe.setModal(false);
                informe.setVisible(true);
                informe.validate();
                if (InformeOferta.modelo.getRowCount() != 1
                        || InformeOferta.modelo.getValueAt(0, 4) != UIUtils.icon("aprobada.png")) {
                    informe.dispose();
                    throw new AssertionError("InformeOferta no mostró la solicitud aprobada con el icono correcto.");
                }
                informe.dispose();
            }
        });
    }

    private static void verificarBotonesDeProcesamiento() throws Exception {
        limpiarDatos();
        crearParticipantes();
        solicitud("SOL-DISPONIBLE", oferta("OFR-DISPONIBLE", 1), Solicitud.ESTADO_ENVIADA);
        solicitud("SOL-SIN-VACANTES", oferta("OFR-SIN-VACANTES", 0), Solicitud.ESTADO_ENVIADA);
        solicitud("SOL-PROCESADA", oferta("OFR-PROCESADA", 1), Solicitud.ESTADO_RECHAZADA);
        candidato.actualizarEstadoLaboral();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    ConsultarSolicitudes dialog = new ConsultarSolicitudes();
                    dialog.setModal(false);
                    dialog.setVisible(true);
                    JButton contratar = (JButton) field("btnContratar").get(dialog);
                    JButton rechazar = (JButton) field("btnRechazar").get(dialog);

                    seleccionarFila(0);
                    comprobarBotones(contratar, rechazar, true, true, "solicitud enviada con vacante");
                    seleccionarFila(1);
                    comprobarBotones(contratar, rechazar, false, true, "solicitud enviada sin vacante");
                    seleccionarFila(2);
                    comprobarBotones(contratar, rechazar, false, false, "solicitud ya procesada");
                    dialog.dispose();
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("No se pudieron inspeccionar los botones.", exception);
                }
            }
        });
    }

    private static void capturarConsultarSolicitudes(final String ruta) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                ConsultarSolicitudes dialog = new ConsultarSolicitudes();
                dialog.setModal(false);
                dialog.setVisible(true);
                seleccionarFila(0);
                dialog.validate();
                BufferedImage imagen = new BufferedImage(dialog.getWidth(), dialog.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = imagen.createGraphics();
                try {
                    dialog.printAll(graphics);
                    ImageIO.write(imagen, "png", new File(ruta));
                } catch (Exception exception) {
                    throw new IllegalStateException("No se pudo capturar ConsultarSolicitudes.", exception);
                } finally {
                    graphics.dispose();
                    dialog.dispose();
                }
            }
        });
    }

    private static void seleccionarFila(int index) {
        ConsultarSolicitudes.table.setRowSelectionInterval(index, index);
    }

    private static void comprobarBotones(JButton contratar, JButton rechazar,
            boolean contratarEsperado, boolean rechazarEsperado, String escenario) {
        if (contratar.isEnabled() != contratarEsperado || rechazar.isEnabled() != rechazarEsperado) {
            throw new AssertionError("Estado incorrecto de botones para " + escenario
                    + ": contratar=" + contratar.isEnabled() + ", rechazar=" + rechazar.isEnabled());
        }
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = ConsultarSolicitudes.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void crearParticipantes() {
        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        ArrayList<String> habilidades = new ArrayList<String>();
        habilidades.add("Limpieza");
        candidato = new Obrero("CAN-UI", "00100000001", "Ana", "Pérez",
                LocalDate.of(1990, 1, 1), "Femenino", "Distrito Nacional", "Santo Domingo",
                "8095550101", "ana@example.com", "Tiempo Completo", "Presencial", "Limpieza",
                20000.0f, false, false, idiomas, habilidades, Candidato.ESTADO_DESEMPLEADO);
        bolsa.registrarCandidato(candidato);
        centro = new CentroEmpleador("CEN-UI", "Centro de prueba", "Servicios",
                "Distrito Nacional", "Santo Domingo", "8095550202", "centro@example.com", "101010101");
        bolsa.registrarCentroTrabajo(centro);
    }

    private static OfertaLaboral oferta(String codigo, int vacantes) {
        ArrayList<String> requisitos = new ArrayList<String>();
        requisitos.add("Limpieza");
        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        OfertaLaboral oferta = new OfertaLaboral(codigo, "Auxiliar", "Oferta de prueba", "Limpieza",
                "Presencial", "Tiempo Completo", OfertaLaboral.ESTADO_ACTIVA, 25000.0f, 0, vacantes, centro,
                false, false, false, "Obrero", requisitos, idiomas, 0);
        bolsa.registrarOfertaLaboral(oferta);
        return oferta;
    }

    private static Solicitud solicitud(String codigo, OfertaLaboral oferta, String estado) {
        Solicitud solicitud = new Solicitud(codigo, LocalDate.now(), estado, candidato, oferta);
        bolsa.getSolicitudes().add(solicitud);
        candidato.addSolicitud(solicitud);
        return solicitud;
    }

    private static void limpiarDatos() {
        bolsa.getCandidatos().clear();
        bolsa.getSolicitudes().clear();
        bolsa.getOfertas().clear();
        bolsa.getCentros().clear();
        bolsa.getVacantes().clear();
        bolsa.getUsuarios().clear();
    }
}
