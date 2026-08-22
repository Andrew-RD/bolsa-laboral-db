package logico;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PersistenciaModeloTest {

    @Test
    public void solicitudNuevaMantieneFechaDecisionNulaYLaSobrecargaLaRestaura() {
        Solicitud nueva = new Solicitud(
                null, LocalDate.of(2026, 8, 20), Solicitud.ESTADO_ENVIADA, null, null
        );
        assertNull(nueva.getFechaDecision());

        LocalDate fechaDecision = LocalDate.of(2026, 8, 21);
        Solicitud restaurada = new Solicitud(
                "SOL-1", LocalDate.of(2026, 8, 20), fechaDecision,
                Solicitud.ESTADO_RECHAZADA, null, null
        );
        assertEquals(fechaDecision, restaurada.getFechaDecision());
    }

    @Test
    public void cierreDeSolicitudesSincronizaFechaDecisionEnMemoria() {
        Candidato candidato = candidato();
        Solicitud aprobada = new Solicitud(
                "SOL-1", LocalDate.of(2026, 8, 20), Solicitud.ESTADO_APROBADA,
                candidato, null
        );
        Solicitud pendiente = new Solicitud(
                "SOL-2", LocalDate.of(2026, 8, 20), Solicitud.ESTADO_ENVIADA,
                candidato, null
        );
        candidato.addSolicitud(aprobada);
        candidato.addSolicitud(pendiente);

        LocalDate fechaDecision = LocalDate.of(2026, 8, 21);
        candidato.cambiarEstadoSolicitudesAEmpleado(fechaDecision);

        assertEquals(Solicitud.ESTADO_RECHAZADA, pendiente.getEstado());
        assertEquals(fechaDecision, pendiente.getFechaDecision());
        assertEquals(Candidato.ESTADO_EMPLEADO, candidato.getEstado());
    }

    @Test
    public void ofertaRestauradaConservaTipoPersistidoYNivelAcademico() {
        OfertaLaboral oferta = new OfertaLaboral(
                "OFR-1", "Puesto", "Descripción", "Tecnología", "Presencial",
                "Tiempo Completo", OfertaLaboral.ESTADO_ACTIVA, 30000.0f, 0, 1,
                null, false, false, false, "Grado", TipoCandidato.UNIVERSITARIO,
                new ArrayList<String>(), new ArrayList<String>(), 70
        );

        assertEquals(TipoCandidato.UNIVERSITARIO, oferta.getTipoCandidatoRequerido());
        assertEquals("Grado", oferta.getNivelAcademico());
    }

    @Test
    public void universitarioConservaIdPersistido() {
        Universitario universitario = new Universitario(
                "CAN-1", "00100000009", "Ana", "Pérez",
                LocalDate.of(1990, 1, 1), "Femenino", "Distrito Nacional",
                "Santo Domingo", "8095550101", "ana@example.test",
                "Tiempo Completo", "Presencial", "Tecnología", 30000.0f,
                false, false, new ArrayList<String>(), "PUCMM",
                "Ingeniería de Sistemas", "Grado", SituacionAcademica.GRADUADO,
                Candidato.ESTADO_DESEMPLEADO
        );

        universitario.setUniversidadId(7);

        assertEquals(Integer.valueOf(7), universitario.getUniversidadId());
    }

    @Test
    public void reconoceAprobadaCanonicaYLegadaSinCambiarLaEscrituraCanonica() {
        assertTrue(Solicitud.esEstadoAprobada(Solicitud.ESTADO_APROBADA));
        assertTrue(Solicitud.esEstadoAprobada("  Aprovada  "));
        assertFalse(Solicitud.esEstadoAprobada(Solicitud.ESTADO_ENVIADA));
        assertEquals("Aprobada", Solicitud.ESTADO_APROBADA);

        Candidato candidato = candidato();
        Solicitud legado = new Solicitud(
                "SOL-LEGADO", LocalDate.of(2026, 8, 20), "Aprovada", candidato, null
        );
        candidato.addSolicitud(legado);
        candidato.actualizarEstadoLaboral();

        assertEquals(Candidato.ESTADO_EMPLEADO, candidato.getEstado());

        Solicitud nuevaAprobacion = new Solicitud(
                "SOL-NUEVA", LocalDate.of(2026, 8, 21),
                Solicitud.ESTADO_APROBADA, candidato, null
        );
        assertEquals("Aprobada", nuevaAprobacion.getEstado());
    }

    @Test
    public void sincronizarOcupadasNoAumentaElTotalConfigurado() {
        OfertaLaboral oferta = new OfertaLaboral(
                "OFR-1", "Puesto", "Descripción", "Tecnología", "Presencial",
                "Tiempo Completo", OfertaLaboral.ESTADO_ACTIVA, 30000.0f, 0, 2,
                null, false, false, false, "Grado", TipoCandidato.UNIVERSITARIO,
                new ArrayList<String>(), new ArrayList<String>(), 70
        );

        oferta.sincronizarVacantesOcupadas(3);

        assertEquals(2, oferta.getVacantesTotales());
        assertEquals(3, oferta.getVacantesOcupadas());
        assertEquals(0, oferta.getVacantesDisponibles());
        assertEquals(OfertaLaboral.ESTADO_COMPLETADA, oferta.getEstado());
    }

    private Candidato candidato() {
        return new Obrero(
                "CAN-1", "00100000009", "Ana", "Pérez",
                LocalDate.of(1990, 1, 1), "Femenino", "Distrito Nacional",
                "Santo Domingo", "8095550101", "ana@example.test",
                "Tiempo Completo", "Presencial", "Limpieza", 20000.0f,
                false, false, new ArrayList<String>(), new ArrayList<String>(),
                Candidato.ESTADO_EN_ESPERA
        );
    }
}
