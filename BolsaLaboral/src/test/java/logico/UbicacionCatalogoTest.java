package logico;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UbicacionCatalogoTest {

    private BolsaLaboral bolsa;
    private GestionCatalogoService servicio;

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        Usuario admin = new Usuario("Administrador", "admin-catalogo", "catalogo@example.test",
                RolUsuario.ADMINISTRADOR, true, "ClaveTemporal1".toCharArray());
        bolsa.getUsuarios().clear();
        bolsa.regUsuario(admin);
        bolsa.setUsuarioActual(admin);
        bolsa.setCatalogos(new CatalogoDatos());
        servicio = new GestionCatalogoService(bolsa);
    }

    @After
    public void tearDown() {
        bolsa.getUsuarios().clear();
        bolsa.setUsuarioActual(null);
    }

    @Test
    public void cargaTodasLasProvinciasYSusMunicipios() {
        UbicacionService ubicaciones = UbicacionService.getInstancia();
        assertEquals(32, ubicaciones.getProvincias().size());
        for (String provincia : ubicaciones.getProvincias()) {
            assertFalse("Sin municipios: " + provincia,
                    ubicaciones.getMunicipios(provincia).isEmpty());
        }
        assertTrue(ubicaciones.getMunicipios("Santiago").contains("Baitoa"));
        assertTrue(ubicaciones.getMunicipios("Santo Domingo").contains("Boca Chica"));
    }

    @Test
    public void reconoceEspaciosMayusculasAcentosYAliasLegados() {
        UbicacionService ubicaciones = UbicacionService.getInstancia();
        assertEquals("María Trinidad Sánchez",
                ubicaciones.buscarProvinciaCanonica(" maria  trinidad sanchez "));
        assertEquals("Santo Domingo de Guzmán",
                ubicaciones.buscarMunicipioCanonico("Distrito Nacional", "SANTO DOMINGO"));
        assertNull(ubicaciones.buscarMunicipioCanonico("Santiago", "Municipio inventado"));
    }

    @Test
    public void catalogoAgregaDesactivaReactivaYRechazaDuplicadoNormalizado() {
        ElementoCatalogo agregado = servicio.agregar(TipoCatalogo.IDIOMAS, "Lengua de Prueba");
        assertTrue(agregado.isActivo());
        assertTrue(bolsa.getCatalogos().getValoresActivos(TipoCatalogo.IDIOMAS)
                .contains("Lengua de Prueba"));

        servicio.cambiarEstado(TipoCatalogo.IDIOMAS, agregado, false);
        assertFalse(bolsa.getCatalogos().getValoresActivos(TipoCatalogo.IDIOMAS)
                .contains("Lengua de Prueba"));
        assertTrue(bolsa.getCatalogos().getValoresParaEdicion(
                TipoCatalogo.IDIOMAS, "Lengua de Prueba").contains("Lengua de Prueba"));

        try {
            servicio.agregar(TipoCatalogo.IDIOMAS, "  LÉNGUA   de prueba ");
            fail("El duplicado normalizado debía rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("equivalente"));
        }

        servicio.cambiarEstado(TipoCatalogo.IDIOMAS, agregado, true);
        assertTrue(agregado.isActivo());
    }

    @Test
    public void incluyeUniversidadesDominicanasRequeridas() {
        for (String requerida : new String[]{"PUCMM", "UASD", "INTEC", "UNPHU", "UTESA",
                "UNAPEC", "O&M", "UCE", "UCNE", "ISA"}) {
            ElementoCatalogo universidad =
                    bolsa.getCatalogos().buscarUniversidad(requerida);
            assertTrue("Falta " + requerida, universidad != null);
            assertEquals(requerida, universidad.getSiglas());
            assertFalse(universidad.getNombreCompleto().trim().isEmpty());
            assertEquals(requerida + " — " + universidad.getNombreCompleto(),
                    universidad.getNombreMostrado());
        }
        assertEquals(0, bolsa.getCatalogos().migrarDatosDeserializados());
    }

    @Test
    public void universidadRegistraYModificaSiglasYNombreSinCambiarIdentidad() {
        ElementoCatalogo universidad = servicio.agregarUniversidad(
                "UPR", "Universidad de Prueba");
        Integer id = universidad.getId();
        Universitario candidato = universitario("Universidad de Prueba");
        candidato.setUniversidadCatalogo(universidad);
        bolsa.getCandidatos().add(candidato);

        servicio.modificarUniversidad(universidad,
                "UPR-N", "Universidad de Prueba Renovada");

        assertEquals(id, universidad.getId());
        assertEquals("UPR-N", universidad.getSiglas());
        assertEquals("Universidad de Prueba Renovada",
                universidad.getNombreCompleto());
        assertEquals("UPR-N — Universidad de Prueba Renovada",
                universidad.getNombreMostrado());
        assertEquals(id, candidato.getUniversidadId());
        assertEquals("Universidad de Prueba Renovada",
                candidato.getUniversidad());
    }

    @Test
    public void universidadRechazaDuplicadosYNombreCompletoVacio() {
        servicio.agregarUniversidad("UPR", "Universidad de Prueba");
        try {
            servicio.agregarUniversidad("  úpr ", "Otra universidad");
            fail("Las siglas duplicadas debían rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("siglas"));
        }
        try {
            servicio.agregarUniversidad("OTRA", " universidad DE prueba ");
            fail("El nombre completo duplicado debía rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("nombre completo"));
        }
        try {
            servicio.agregarUniversidad("VACÍA", "   ");
            fail("El nombre completo vacío debía rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("obligatorio"));
        }
    }

    @Test
    public void universidadInactivaSigueDisponibleParaRegistroHistorico() {
        ElementoCatalogo universidad = servicio.agregarUniversidad(
                "", "Universidad Histórica de Prueba");
        servicio.cambiarEstado(TipoCatalogo.UNIVERSIDADES,
                universidad, false);

        assertFalse(bolsa.getCatalogos().getUniversidadesActivas()
                .contains(universidad));
        assertTrue(bolsa.getCatalogos().getUniversidadesParaEdicion(
                universidad.getId(),
                universidad.getNombreCompleto()).contains(universidad));
        assertEquals("Universidad Histórica de Prueba",
                universidad.getNombreMostrado());
    }

    @Test
    public void universidadLegadaSeEnlazaEnMemoriaDeFormaIdempotente() {
        servicio.agregarUniversidad("UPR2", "Universidad de Prueba Dos");
        Universitario conocida = universitario(" upr2 ");
        assertTrue(conocida.getUniversidadId() == null);
        assertEquals(1, conocida.migrarUniversidadDeserializada(
                bolsa.getCatalogos()));
        assertEquals("UPR2", bolsa.getCatalogos().buscarPorId(
                TipoCatalogo.UNIVERSIDADES,
                conocida.getUniversidadId()).getSiglas());
        assertEquals(0, conocida.migrarUniversidadDeserializada(
                bolsa.getCatalogos()));
        assertEquals(" upr2 ", conocida.getUniversidad());

        Universitario desconocida =
                universitario("Universidad Legada Fuera del Catálogo");
        assertEquals(0, desconocida.migrarUniversidadDeserializada(
                bolsa.getCatalogos()));
        assertTrue(desconocida.getUniversidadId() == null);
        assertEquals("Universidad Legada Fuera del Catálogo",
                desconocida.getUniversidad());
    }

    private Universitario universitario(String universidad) {
        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        return new Universitario("CAN-UNIVERSIDAD", "00100000009",
                "Persona", "Universitaria", LocalDate.of(1990, 1, 1),
                "Femenino", "Distrito Nacional", "Santo Domingo de Guzmán",
                "8095550000", "universidad@example.test", "Tiempo Completo",
                "Presencial", "Tecnología", 20000, false, false, idiomas,
                universidad, "Ingeniería de Sistemas", "Grado",
                SituacionAcademica.GRADUADO, Candidato.ESTADO_DESEMPLEADO);
    }
}