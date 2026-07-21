package logico;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SolicitudWorkflowTest {

    private BolsaLaboral bolsa;
    private Candidato candidato;
    private CentroEmpleador centro;

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        clearData();

        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        ArrayList<String> habilidades = new ArrayList<String>();
        habilidades.add("Limpieza");
        candidato = new Obrero("CAN-TEST", "00100000001", "Ana", "Pérez",
                LocalDate.of(1990, 1, 1), "Femenino", "Distrito Nacional", "Santo Domingo",
                "8095550101", "ana@example.com", "Tiempo Completo", "Presencial", "Limpieza",
                20000.0f, false, false, idiomas, habilidades, "Desempleado");
        bolsa.registrarCandidato(candidato);

        centro = new CentroEmpleador("CEN-TEST", "Centro de prueba", "Servicios",
                "Distrito Nacional", "Santo Domingo", "8095550202", "centro@example.com", "101010101");
        bolsa.registrarCentroTrabajo(centro);
    }

    @After
    public void tearDown() {
        clearData();
    }

    @Test
    public void rechazarPrimeraYAprobarSegundaConservaCadaResultado() {
        Solicitud primera = solicitud("SOL-1", oferta("OFR-1", 1), "Enviada");
        OfertaLaboral segundaOferta = oferta("OFR-2", 1);
        Solicitud segunda = solicitud("SOL-2", segundaOferta, "Enviada");

        bolsa.rechazarCandidato(primera);
        bolsa.contratarCandidato(segunda);

        assertEquals("Rechazada", primera.getEstado());
        assertEquals("Aprobada", segunda.getEstado());
        assertEquals("Empleado", candidato.getEstado());
        assertEquals(0, segundaOferta.getVacantes());
        assertEquals("Completada", segundaOferta.getEstado());
    }

    @Test
    public void rechazarUnaDeDosEnviadasDejaLaOtraEnEspera() {
        OfertaLaboral oferta = oferta("OFR-1", 2);
        Solicitud seleccionada = solicitud("SOL-1", oferta, "Enviada");
        Solicitud pendiente = solicitud("SOL-2", oferta("OFR-2", 1), "Enviada");

        bolsa.rechazarCandidato(seleccionada);

        assertEquals("Rechazada", seleccionada.getEstado());
        assertEquals("Enviada", pendiente.getEstado());
        assertEquals("En Espera", candidato.getEstado());
        assertEquals(2, oferta.getVacantes());
    }

    @Test
    public void aprobarConOtraRechazadaNoReabreLaRechazada() {
        Solicitud rechazada = solicitud("SOL-1", oferta("OFR-1", 1), "Rechazada");
        Solicitud seleccionada = solicitud("SOL-2", oferta("OFR-2", 1), "Enviada");

        bolsa.contratarCandidato(seleccionada);

        assertEquals("Rechazada", rechazada.getEstado());
        assertEquals("Aprobada", seleccionada.getEstado());
        assertEquals("Empleado", candidato.getEstado());
    }

    @Test
    public void aprobarCierraSoloLasOtrasEnviadasComoRechazadas() {
        Solicitud seleccionada = solicitud("SOL-1", oferta("OFR-1", 1), "Enviada");
        Solicitud pendiente = solicitud("SOL-2", oferta("OFR-2", 1), "Enviada");

        bolsa.contratarCandidato(seleccionada);

        assertEquals("Aprobada", seleccionada.getEstado());
        assertEquals("Rechazada", pendiente.getEstado());
        assertEquals("Empleado", candidato.getEstado());
    }

    @Test
    public void noApruebaNiRegistraCuandoNoHayVacantes() {
        OfertaLaboral oferta = oferta("OFR-1", 0);
        Solicitud seleccionada = solicitud("SOL-1", oferta, "Enviada");

        bolsa.contratarCandidato(seleccionada);

        assertEquals("Enviada", seleccionada.getEstado());
        assertEquals(0, oferta.getVacantes());
        assertEquals(0, bolsa.getVacantes().size());
        assertEquals("En Espera", candidato.getEstado());
    }

    @Test
    public void procesarDosVecesNoDuplicaNiDescuentaOtraVacante() {
        OfertaLaboral oferta = oferta("OFR-1", 2);
        Solicitud seleccionada = solicitud("SOL-1", oferta, "Enviada");

        bolsa.contratarCandidato(seleccionada);
        bolsa.contratarCandidato(seleccionada);

        assertEquals("Aprobada", seleccionada.getEstado());
        assertEquals(1, oferta.getVacantes());
        assertEquals(1, bolsa.getVacantes().size());
    }

    @Test
    public void migraAprovadaConEspaciosAlEstadoCanonico() {
        Solicitud legado = solicitud("SOL-LEGACY", oferta("OFR-1", 1), "  Aprovada  ");

        int migrados = bolsa.migrarDatosDeserializados();

        assertTrue(migrados >= 1);
        assertEquals("Aprobada", legado.getEstado());
        assertEquals("Empleado", candidato.getEstado());
    }

    @Test
    public void rutaLegadaDeRegistroDeVacanteDelegaSinDuplicar() {
        OfertaLaboral oferta = oferta("OFR-1", 2);
        Solicitud seleccionada = solicitud("SOL-1", oferta, "Enviada");

        bolsa.regVacanteCompletada(seleccionada);
        bolsa.regVacanteCompletada(seleccionada);

        assertEquals("Aprobada", seleccionada.getEstado());
        assertEquals(1, oferta.getVacantes());
        assertEquals(1, bolsa.getVacantes().size());
    }

    @Test
    public void soloEnviadaEsProcesableInclusoConEstadosNulos() {
        Solicitud enviada = solicitud("SOL-1", oferta("OFR-1", 1), "Enviada");
        Solicitud desconocida = solicitud("SOL-2", oferta("OFR-2", 1), null);

        assertTrue(bolsa.esProcesable(enviada));
        assertFalse(bolsa.esProcesable(desconocida));
        assertFalse(bolsa.esProcesable(null));
    }

    @Test
    public void verificarSolicitudToleraCamposLegadosNulos() {
        OfertaLaboral oferta = oferta("OFR-1", 1);
        Solicitud existente = new Solicitud("SOL-1", null, null, candidato, oferta);
        bolsa.getSolicitudes().add(existente);
        candidato.addSolicitud(existente);
        Solicitud duplicada = new Solicitud("SOL-2", null, null, candidato, oferta);

        assertFalse(bolsa.verificarSolicitud(duplicada));
    }

    @Test
    public void consultasIgnoranEntradasNulasEnLaColeccionDeSolicitudes() {
        OfertaLaboral oferta = oferta("OFR-1", 1);
        bolsa.getSolicitudes().add(null);

        assertNull(bolsa.buscarSolicitudByCodigo("INEXISTENTE"));
        assertTrue(bolsa.obtenerSolicitudesVinculadas(oferta).isEmpty());
        assertFalse(bolsa.ofertaVinculada(oferta));
    }

    @Test
    public void lasVacantesNuncaAceptanValoresNegativos() {
        OfertaLaboral oferta = oferta("OFR-1", 1);

        oferta.setVacantes(-3);

        assertEquals(0, oferta.getVacantes());
    }

    private Solicitud solicitud(String codigo, OfertaLaboral oferta, String estado) {
        Solicitud solicitud = new Solicitud(codigo, LocalDate.now(), estado, candidato, oferta);
        bolsa.getSolicitudes().add(solicitud);
        candidato.addSolicitud(solicitud);
        if ("Enviada".equals(estado)) {
            candidato.setEstado("En Espera");
        }
        return solicitud;
    }

    private OfertaLaboral oferta(String codigo, int vacantes) {
        ArrayList<String> requisitos = new ArrayList<String>();
        requisitos.add("Limpieza");
        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        OfertaLaboral oferta = new OfertaLaboral(codigo, "Auxiliar", "Oferta de prueba", "Limpieza",
                "Presencial", "Tiempo Completo", "Activa", 25000.0f, 0, vacantes, centro,
                false, false, false, "Obrero", requisitos, idiomas, 0);
        bolsa.registrarOfertaLaboral(oferta);
        return oferta;
    }

    private void clearData() {
        if (bolsa == null) {
            return;
        }
        bolsa.getCandidatos().clear();
        bolsa.getSolicitudes().clear();
        bolsa.getOfertas().clear();
        bolsa.getCentros().clear();
        bolsa.getVacantes().clear();
        bolsa.getUsuarios().clear();
    }
}
