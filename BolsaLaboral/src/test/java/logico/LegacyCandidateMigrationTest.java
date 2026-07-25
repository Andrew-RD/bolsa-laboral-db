package logico;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LegacyCandidateMigrationTest {

    private BolsaLaboral bolsa;

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        clearData();
        Usuario admin = new Usuario("Administrador de prueba", "admin-prueba",
                "admin@example.test", RolUsuario.ADMINISTRADOR, true, "ClaveTemporal1".toCharArray());
        bolsa.regUsuario(admin);
        bolsa.setUsuarioActual(admin);
    }

    @After
    public void tearDown() {
        clearData();
    }

    @Test
    public void migratesMissingCandidateStateBeforeMatching() {
        ArrayList<String> idiomas = new ArrayList<String>();
        idiomas.add("Español");
        ArrayList<String> habilidades = new ArrayList<String>();
        habilidades.add("Limpieza");

        Obrero candidato = new Obrero("CAN-LEGACY", "00100000009", "Candidato", "Legado",
                LocalDate.of(1990, 1, 1), "Masculino", "Distrito Nacional", "Santo Domingo",
                "8095550101", "legado@example.com", "Tiempo Completo", "Presencial", "Limpieza",
                20000.0f, false, false, idiomas, habilidades, null);
        assertNull(candidato.getEstado());
        bolsa.getCandidatos().add(candidato);

        CentroEmpleador centro = new CentroEmpleador("CEN-LEGACY", "Centro Legado", "Servicios",
                "Distrito Nacional", "Santo Domingo", "8095550202", "centro@example.com", "101010101");
        bolsa.getCentros().add(centro);
        OfertaLaboral oferta = new OfertaLaboral("OFR-LEGACY", "Auxiliar", "Oferta de prueba", "Limpieza",
                "Presencial", "Tiempo Completo", "Activa", 25000.0f, 0, 1, centro,
                false, false, false, "Obrero", habilidades, idiomas, 0);
        bolsa.getOfertas().add(oferta);
        centro.getOfertasLaborales().add(oferta);

        assertTrue(bolsa.migrarDatosDeserializados() >= 2);
        assertEquals("Desempleado", candidato.getEstado());
        assertEquals(1, bolsa.procesamientoAvanzando().size());
    }

    @Test
    public void ignoresNullCandidateEntriesDuringMigrationAndMatching() {
        bolsa.getCandidatos().add(null);

        assertEquals(0, bolsa.migrarDatosDeserializados());
        assertTrue(bolsa.procesamientoAvanzando().isEmpty());
    }

    private void clearData() {
        bolsa.getCandidatos().clear();
        bolsa.getSolicitudes().clear();
        bolsa.getOfertas().clear();
        bolsa.getCentros().clear();
        bolsa.getVacantes().clear();
        bolsa.getUsuarios().clear();
        bolsa.setUsuarioActual(null);
    }
}
