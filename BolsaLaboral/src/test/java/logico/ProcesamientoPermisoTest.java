package logico;

import exception.AutorizacionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProcesamientoPermisoTest {

    private BolsaLaboral bolsa;
    private Usuario administrador;
    private CentroEmpleador centro;

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        limpiar();
        administrador = new Usuario("Administrador", "admin-procesamiento",
                "admin-procesamiento@example.test", RolUsuario.ADMINISTRADOR,
                true, "ClaveTemporal1".toCharArray());
        bolsa.regUsuario(administrador);
        bolsa.setUsuarioActual(administrador);
        centro = new CentroEmpleador("CEN-P", "Centro de prueba", "Servicios",
                "Distrito Nacional", "Santo Domingo de Guzmán", "8095550000",
                "centro-procesamiento@example.test",
                DocumentoDominicanoTest.generarRnc("10101010"));
        bolsa.registrarCentroTrabajo(centro);
    }

    @After
    public void tearDown() {
        limpiar();
    }

    @Test
    public void administradorConOfertaVacanteYCandidatoElegiblePuedeProcesar() {
        OfertaLaboral oferta = oferta("OFR-PERMITIDA", 1);
        candidato("CAN-PERMITIDO", "0010000034");

        DecisionProcesamiento decision = bolsa.evaluarProcesamiento(oferta);

        assertTrue(decision.getRazon(), decision.isPermitido());
        assertEquals("La oferta puede procesarse.", decision.getRazon());
    }

    @Test
    public void ofertaActivaSinCandidatosExplicaElBloqueo() {
        OfertaLaboral oferta = oferta("OFR-SIN-CANDIDATOS", 1);

        DecisionProcesamiento decision = bolsa.evaluarProcesamiento(oferta);

        assertFalse(decision.isPermitido());
        assertTrue(decision.getRazon().contains("candidato elegible"));
        assertTrue(decision.getRazon().contains("porcentaje mínimo"));
    }

    @Test
    public void ofertaCompletadaOSinVacantesExplicaElBloqueo() {
        OfertaLaboral oferta = oferta("OFR-COMPLETADA", 0);

        DecisionProcesamiento decision = bolsa.evaluarProcesamiento(oferta);

        assertFalse(decision.isPermitido());
        assertEquals(OfertaLaboral.ESTADO_COMPLETADA, oferta.getEstado());
        assertTrue(decision.getRazon().contains("vacantes disponibles"));
    }

    @Test
    public void permisoAvanzadoEsIndependienteDeProcesarSolicitudes() {
        OfertaLaboral oferta = oferta("OFR-EMPLEADO", 1);
        candidato("CAN-EMPLEADO", "0010000042");
        Usuario empleado = new Usuario("Empleado", "empleado-procesamiento",
                "empleado-procesamiento@example.test", RolUsuario.EMPLEADO,
                true, "ClaveTemporal1".toCharArray());
        bolsa.regUsuario(empleado);
        bolsa.setUsuarioActual(empleado);

        assertTrue(empleado.tienePermiso(Permiso.PROCESAR_SOLICITUDES));
        assertFalse(empleado.tienePermiso(Permiso.USAR_PROCESAMIENTO_AVANZADO));
        DecisionProcesamiento sinPermiso = bolsa.evaluarProcesamiento(oferta);
        assertFalse(sinPermiso.isPermitido());
        assertTrue(sinPermiso.getRazon().contains("procesamiento avanzado"));

        EnumSet<Permiso> personalizados = empleado.getPermisos();
        personalizados.add(Permiso.USAR_PROCESAMIENTO_AVANZADO);
        personalizados.remove(Permiso.PROCESAR_SOLICITUDES);
        empleado.setPermisos(personalizados);

        assertFalse(empleado.tienePermiso(Permiso.PROCESAR_SOLICITUDES));
        DecisionProcesamiento autorizado = bolsa.evaluarProcesamiento(oferta);
        assertTrue(autorizado.getRazon(), autorizado.isPermitido());
    }

    @Test
    public void usuarioInactivoYAccionDirectaSinPermisoSonRechazados() {
        OfertaLaboral oferta = oferta("OFR-DIRECTA", 1);
        Obrero candidato = candidato("CAN-DIRECTO", "0010000059");
        ResultadoMatcheo resultado =
                bolsa.obtenerCandidatosOrdenadosParaOferta(oferta).get(0);

        administrador.setActivo(false);
        DecisionProcesamiento inactivo = bolsa.evaluarProcesamiento(oferta);
        assertFalse(inactivo.isPermitido());
        assertTrue(inactivo.getRazon().contains("inactivo"));
        administrador.setActivo(true);

        Usuario empleado = new Usuario("Empleado", "empleado-directo",
                "empleado-directo@example.test", RolUsuario.EMPLEADO,
                true, "ClaveTemporal1".toCharArray());
        bolsa.regUsuario(empleado);
        bolsa.setUsuarioActual(empleado);
        int solicitudesAntes = bolsa.getSolicitudes().size();
        try {
            bolsa.vincularOferta(resultado);
            fail("La vinculación directa debía requerir procesamiento avanzado.");
        } catch (AutorizacionException expected) {
            assertTrue(expected.getMessage().contains(
                    Permiso.USAR_PROCESAMIENTO_AVANZADO.name()));
        }
        assertEquals(solicitudesAntes, bolsa.getSolicitudes().size());
        assertEquals(Candidato.ESTADO_DESEMPLEADO, candidato.getEstado());
    }

    @Test
    public void solicitudDuplicadaRechazadaNoHabilitaOtraVinculacion() {
        OfertaLaboral oferta = oferta("OFR-DUPLICADA", 1);
        Obrero candidato = candidato("CAN-DUPLICADO", "0010000067");
        Solicitud anterior = new Solicitud("SOL-ANTERIOR", LocalDate.now(),
                Solicitud.ESTADO_RECHAZADA, candidato, oferta);
        bolsa.getSolicitudes().add(anterior);
        candidato.addSolicitud(anterior);
        candidato.actualizarEstadoLaboral();

        DecisionProcesamiento decision = bolsa.evaluarProcesamiento(oferta);

        assertFalse(decision.isPermitido());
        assertTrue(decision.getRazon().contains(
                "ya tienen una solicitud"));
    }

    private Obrero candidato(String codigo, String baseCedula) {
        Obrero candidato = new Obrero(codigo,
                DocumentoDominicanoTest.generarCedula(baseCedula),
                "Persona", "Candidata", LocalDate.of(1990, 1, 1),
                "Femenino", "Distrito Nacional", "Santo Domingo de Guzmán",
                "8095550101", codigo.toLowerCase() + "@example.test",
                "Tiempo Completo", "Presencial", "Limpieza", 20000,
                false, false, lista("Español"), lista("Limpieza"),
                Candidato.ESTADO_DESEMPLEADO);
        bolsa.registrarCandidato(candidato);
        return candidato;
    }

    private OfertaLaboral oferta(String codigo, int vacantes) {
        OfertaLaboral oferta = new OfertaLaboral(codigo, "Auxiliar",
                "Oferta de prueba", "Limpieza", "Presencial",
                "Tiempo Completo", OfertaLaboral.ESTADO_ACTIVA, 25000,
                0, vacantes, centro, false, false, false,
                TipoCandidato.OBRERO.getEtiqueta(), lista("Limpieza"),
                lista("Español"), 0);
        oferta.setTipoCandidatoRequerido(TipoCandidato.OBRERO);
        bolsa.registrarOfertaLaboral(oferta);
        return oferta;
    }

    private ArrayList<String> lista(String valor) {
        ArrayList<String> valores = new ArrayList<String>();
        valores.add(valor);
        return valores;
    }

    private void limpiar() {
        if (bolsa == null) {
            return;
        }
        bolsa.getCandidatos().clear();
        bolsa.getSolicitudes().clear();
        bolsa.getOfertas().clear();
        bolsa.getCentros().clear();
        bolsa.getVacantes().clear();
        bolsa.getUsuarios().clear();
        bolsa.setUsuarioActual(null);
    }
}
