package visual;

import logico.BolsaLaboral;
import logico.OfertaLaboral;
import logico.Solicitud;
import logico.VacanteCompletada;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IndicadoresInformeGeneralTest {

    private BolsaLaboral bolsa;

    @BeforeClass
    public static void useHeadlessToolkit() {
        System.setProperty("java.awt.headless", "true");
    }

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        bolsa.setOfertas(new ArrayList<OfertaLaboral>());
        bolsa.setVacantes(new ArrayList<VacanteCompletada>());
    }

    @After
    public void tearDown() {
        bolsa.setOfertas(new ArrayList<OfertaLaboral>());
        bolsa.setVacantes(new ArrayList<VacanteCompletada>());
    }

    @Test
    public void ceroContratadosDeDiezPuestosEsCeroPorCiento() {
        agregarOfertaConVacantes(10);

        assertEquals(0, bolsa.calcularTasaCovertura());
    }

    @Test
    public void cincoContratadosDeDiezPuestosEsCincuentaPorCiento() {
        OfertaLaboral oferta = agregarOfertaConVacantes(10);
        agregarContrataciones(oferta, 5);

        assertEquals(50, bolsa.calcularTasaCovertura());
        assertEquals(5, bolsa.contarVacantesOcupadasTotales());
    }

    @Test
    public void diezContratadosDeDiezPuestosEsCienPorCiento() {
        OfertaLaboral oferta = agregarOfertaConVacantes(10);
        agregarContrataciones(oferta, 10);

        assertEquals(100, bolsa.calcularTasaCovertura());
    }

    @Test
    public void sinPuestosEsCeroSinDivisionEntreCero() {
        assertEquals(0, bolsa.calcularTasaCovertura());
    }

    @Test
    public void unaContratacionDeDosSolicitudesEsCincuentaPorCiento() {
        assertEquals(50, InformeGeneral.calcularTasaExito(1, 2));
    }

    @Test
    public void porcentajesSeMantienenEntreCeroYCien() {
        assertEquals(0, InformeGeneral.calcularTasaExito(-1, 2));
        assertEquals(100, InformeGeneral.calcularTasaExito(5, 2));

        agregarContrataciones(null, 3);
        int cobertura = bolsa.calcularTasaCovertura();
        assertTrue(cobertura >= 0 && cobertura <= 100);
    }

    @Test
    public void coleccionesYEntradasLegadasNulasNoLanzanExcepciones() {
        bolsa.getOfertas().add(null);
        bolsa.getVacantes().add(null);
        assertEquals(0, bolsa.calcularTasaCovertura());

        ArrayList<Object> datosNulos = new ArrayList<Object>();
        datosNulos.add(null);
        assertEquals(0, InformeGeneral.contarNoNulos(datosNulos));
        assertEquals(0, InformeGeneral.contarNoNulos(null));

        bolsa.setOfertas(null);
        bolsa.setVacantes(null);
        assertEquals(0, bolsa.calcularTasaCovertura());
    }

    private void agregarContrataciones(OfertaLaboral oferta, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Solicitud solicitud = oferta == null ? null : new Solicitud("SOL-" + i,
                    LocalDate.now(), Solicitud.ESTADO_APROBADA, null, oferta);
            bolsa.getVacantes().add(new VacanteCompletada(
                    "VAC-" + i, solicitud, oferta, LocalDate.now()));
        }
    }

    private OfertaLaboral agregarOfertaConVacantes(int vacantes) {
        OfertaLaboral oferta = new OfertaLaboral("OFR-" + bolsa.getOfertas().size(), "Puesto", "Descripción",
                "TI", "Presencial", "Tiempo Completo", OfertaLaboral.ESTADO_ACTIVA, 1.0f, 0, vacantes,
                null, false, false, false, "Obrero", new ArrayList<String>(), new ArrayList<String>(), 0);
        bolsa.getOfertas().add(oferta);
        return oferta;
    }
}
