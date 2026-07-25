package logico;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CandidatoCentroOfertaTest {

    private BolsaLaboral bolsa;
    private CentroEmpleador centro;

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        limpiar();
        Usuario admin = new Usuario("Administrador", "admin-dominio", "dominio@example.test",
                RolUsuario.ADMINISTRADOR, true, "ClaveTemporal1".toCharArray());
        bolsa.regUsuario(admin);
        bolsa.setUsuarioActual(admin);
        centro = new CentroEmpleador("CEN-1", "Centro", "Servicios",
                "Distrito Nacional", "Santo Domingo de Guzmán", "8095550000",
                "centro@example.test", DocumentoDominicanoTest.generarRnc("10101010"));
        bolsa.registrarCentroTrabajo(centro);
    }

    @After
    public void tearDown() {
        limpiar();
    }

    @Test
    public void universitarioAdmiteEstudianteEgresadoYGraduado() {
        for (SituacionAcademica situacion : new SituacionAcademica[]{
                SituacionAcademica.ESTUDIANTE, SituacionAcademica.EGRESADO,
                SituacionAcademica.GRADUADO}) {
            Universitario candidato = universitario("CAN-" + situacion.name(),
                    DocumentoDominicanoTest.generarCedula(baseCedula(situacion.ordinal())),
                    situacion);
            assertEquals(TipoCandidato.UNIVERSITARIO, candidato.getTipoCandidato());
            assertEquals(situacion, candidato.getSituacionAcademica());
        }
    }

    @Test
    public void migracionUniversitarioNoInventaSituacion() throws Exception {
        Universitario candidato = universitario("CAN-L", "00100000009",
                SituacionAcademica.ESTUDIANTE);
        Field field = Universitario.class.getDeclaredField("situacionAcademica");
        field.setAccessible(true);
        field.set(candidato, null);

        assertEquals(1, candidato.migrarSituacionDeserializada());
        assertEquals(SituacionAcademica.NO_ESPECIFICADO, candidato.getSituacionAcademica());
        assertEquals(0, candidato.migrarSituacionDeserializada());
    }

    @Test
    public void matchingUniversitarioUsaTipoInternoTrasCambioDeEtiqueta() {
        Universitario candidato = universitario("CAN-U", "00100000009",
                SituacionAcademica.GRADUADO);
        bolsa.registrarCandidato(candidato);
        OfertaLaboral oferta = oferta("OFR-U", 1, TipoCandidato.UNIVERSITARIO,
                "Ingeniería de Sistemas", 0);

        assertEquals(1, bolsa.obtenerCandidatosOrdenadosParaOferta(oferta).size());
        assertEquals(TipoCandidato.UNIVERSITARIO,
                oferta.getTipoCandidatoRequerido());
    }

    @Test
    public void tecnicoYObreroConservanTiposYEstadoInicialDesempleado() {
        ArrayList<String> idiomas = lista("Español");
        TecnicoSuperior tecnico = new TecnicoSuperior("CAN-T",
                DocumentoDominicanoTest.generarCedula("0010000018"), "Tania", "Técnica",
                LocalDate.of(1990, 1, 1), "Femenino", "Santiago", "Baitoa", "8095550001",
                "tecnica@example.test", "Tiempo Completo", "Presencial", "Tecnología", 20000,
                false, false, idiomas, "Informática", 2, Candidato.ESTADO_EMPLEADO);
        Obrero obrero = new Obrero("CAN-O",
                DocumentoDominicanoTest.generarCedula("0010000026"), "Oscar", "Obrero",
                LocalDate.of(1990, 1, 1), "Masculino", "Santiago", "Baitoa", "8095550002",
                "obrero@example.test", "Tiempo Completo", "Presencial", "Limpieza", 20000,
                false, false, idiomas, lista("Limpieza"), Candidato.ESTADO_EMPLEADO);

        bolsa.registrarCandidato(tecnico);
        bolsa.registrarCandidato(obrero);

        assertEquals(TipoCandidato.TECNICO, tecnico.getTipoCandidato());
        assertEquals(TipoCandidato.OBRERO, obrero.getTipoCandidato());
        assertEquals(Candidato.ESTADO_DESEMPLEADO, tecnico.getEstado());
        assertEquals(Candidato.ESTADO_DESEMPLEADO, obrero.getEstado());
    }

    @Test
    public void cedulaYRNCDuplicadosSeComparanNormalizados() {
        Universitario primero = universitario("CAN-1", "001-0000000-9",
                SituacionAcademica.ESTUDIANTE);
        bolsa.registrarCandidato(primero);
        int candidatosAntes = bolsa.getCandidatos().size();
        try {
            bolsa.registrarCandidato(universitario("CAN-2", "00100000009",
                    SituacionAcademica.EGRESADO));
            fail("La cédula duplicada debía rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("cédula"));
        }
        assertEquals(candidatosAntes, bolsa.getCandidatos().size());

        int centrosAntes = bolsa.getCentros().size();
        try {
            bolsa.registrarCentroTrabajo(new CentroEmpleador("CEN-2", "Duplicado", "Servicios",
                    "Distrito Nacional", "Santo Domingo de Guzmán", "8095550003",
                    "duplicado@example.test", "1-01-01010-1"));
            fail("El RNC duplicado debía rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("RNC"));
        }
        assertEquals(centrosAntes, bolsa.getCentros().size());
    }

    @Test
    public void vacantesSeDerivanDeContratacionesYReactivanAlAumentarTotal() {
        OfertaLaboral oferta = oferta("OFR-V", 10, TipoCandidato.OBRERO, "Limpieza", 0);
        assertVacantes(oferta, 10, 0, 10, OfertaLaboral.ESTADO_ACTIVA);
        agregarContrataciones(oferta, 5);
        oferta.sincronizarVacantesOcupadas(bolsa.contarVacantesOcupadas(oferta));
        assertVacantes(oferta, 10, 5, 5, OfertaLaboral.ESTADO_ACTIVA);
        agregarContrataciones(oferta, 5);
        oferta.sincronizarVacantesOcupadas(bolsa.contarVacantesOcupadas(oferta));
        assertVacantes(oferta, 10, 10, 0, OfertaLaboral.ESTADO_COMPLETADA);

        oferta.setVacantesTotales(12);
        assertVacantes(oferta, 12, 10, 2, OfertaLaboral.ESTADO_ACTIVA);

        try {
            oferta.setVacantesTotales(9);
            fail("No debe reducirse por debajo de ocupadas.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("ocupadas"));
        }
        assertEquals(12, oferta.getVacantesTotales());
    }

    @Test
    public void ofertaLegadaInfiereTotalYLaMigracionEsIdempotente() throws Exception {
        OfertaLaboral oferta = oferta("OFR-L", 4, TipoCandidato.OBRERO, "Limpieza", 0);
        agregarContrataciones(oferta, 3);
        set(oferta, "esquemaVacantesTotales", null);
        set(oferta, "vacantes", 4);
        set(oferta, "vacantesTotales", 0);
        set(oferta, "vacantesOcupadas", 0);

        assertTrue(oferta.migrarDatosDeserializados(3) > 0);
        assertVacantes(oferta, 7, 3, 4, OfertaLaboral.ESTADO_ACTIVA);
        assertEquals(0, oferta.migrarDatosDeserializados(3));
    }

    @Test
    public void procesamientoRechazaSinVacantesSinElegiblesYDuplicados() {
        OfertaLaboral sinVacantes = oferta("OFR-0", 0, TipoCandidato.OBRERO, "Limpieza", 0);
        assertFalse(bolsa.evaluarProcesamiento(sinVacantes).isPermitido());

        OfertaLaboral sinCandidatos = oferta("OFR-S", 1, TipoCandidato.OBRERO, "Limpieza", 0);
        assertFalse(bolsa.evaluarProcesamiento(sinCandidatos).isPermitido());

        Obrero candidato = obrero("CAN-P", "00100000009");
        bolsa.registrarCandidato(candidato);
        OfertaLaboral procesable = oferta("OFR-P", 1, TipoCandidato.OBRERO, "Limpieza", 0);
        ResultadoMatcheo resultado =
                bolsa.obtenerCandidatosOrdenadosParaOferta(procesable).get(0);
        assertTrue(bolsa.vincularOferta(resultado));
        int solicitudes = bolsa.getSolicitudes().size();
        assertFalse(bolsa.vincularOferta(resultado));
        assertEquals(solicitudes, bolsa.getSolicitudes().size());
        assertTrue(procesable.getVacantesDisponibles() >= 0);
    }

    private Universitario universitario(String codigo, String cedula,
            SituacionAcademica situacion) {
        return new Universitario(codigo, cedula, "Ana", "Universitaria",
                LocalDate.of(1990, 1, 1), "Femenino", "Distrito Nacional",
                "Santo Domingo de Guzmán", "8095550101", codigo.toLowerCase() + "@example.test",
                "Tiempo Completo", "Presencial", "Tecnología", 20000, false, false,
                lista("Español"), "PUCMM", "Ingeniería de Sistemas", "Grado",
                situacion, Candidato.ESTADO_DESEMPLEADO);
    }

    private Obrero obrero(String codigo, String cedula) {
        return new Obrero(codigo, cedula, "Olga", "Obrera", LocalDate.of(1990, 1, 1),
                "Femenino", "Distrito Nacional", "Santo Domingo de Guzmán", "8095550102",
                codigo.toLowerCase() + "@example.test", "Tiempo Completo", "Presencial",
                "Limpieza", 20000, false, false, lista("Español"), lista("Limpieza"),
                Candidato.ESTADO_DESEMPLEADO);
    }

    private OfertaLaboral oferta(String codigo, int total, TipoCandidato tipo,
            String requisito, int minimo) {
        OfertaLaboral oferta = new OfertaLaboral(codigo, "Puesto", "Descripción", "Limpieza",
                "Presencial", "Tiempo Completo", OfertaLaboral.ESTADO_ACTIVA, 25000, 0, total,
                centro, false, false, false, tipo.getEtiqueta(), lista(requisito),
                lista("Español"), minimo);
        oferta.setTipoCandidatoRequerido(tipo);
        bolsa.registrarOfertaLaboral(oferta);
        return oferta;
    }

    private void agregarContrataciones(OfertaLaboral oferta, int cantidad) {
        int inicio = bolsa.getVacantes().size();
        for (int index = 0; index < cantidad; index++) {
            Solicitud solicitud = new Solicitud("SOL-V-" + (inicio + index), LocalDate.now(),
                    Solicitud.ESTADO_APROBADA, null, oferta);
            bolsa.getVacantes().add(new VacanteCompletada("VAC-V-" + (inicio + index),
                    solicitud, oferta, LocalDate.now()));
        }
    }

    private void assertVacantes(OfertaLaboral oferta, int total, int ocupadas,
            int disponibles, String estado) {
        assertEquals(total, oferta.getVacantesTotales());
        assertEquals(ocupadas, oferta.getVacantesOcupadas());
        assertEquals(disponibles, oferta.getVacantesDisponibles());
        assertEquals(estado, oferta.getEstado());
    }

    private ArrayList<String> lista(String valor) {
        ArrayList<String> lista = new ArrayList<String>();
        lista.add(valor);
        return lista;
    }

    private String baseCedula(int indice) {
        return String.format("00100000%02d", indice + 1);
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
