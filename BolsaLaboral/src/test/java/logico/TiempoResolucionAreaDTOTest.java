package logico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TiempoResolucionAreaDTOTest {

    @Test
    public void conservaPromedioYPorcentajeProporcionados() {
        TiempoResolucionAreaDTO resultado = new TiempoResolucionAreaDTO(
                "TI", 4, 2, 2, 1, 3.5, 50.0);

        assertEquals(3.5, resultado.getDiasPromedioResolucion(), 0.001);
        assertEquals(50.0, resultado.getPorcentajeResolucion(), 0.001);
        assertEquals(TiempoResolucionAreaDTO.DIAGNOSTICO_REQUIERE_ATENCION,
                resultado.getDiagnostico());
    }

    @Test
    public void sinPendientesEstaAlDiaInclusoSinVinculaciones() {
        TiempoResolucionAreaDTO resultado = new TiempoResolucionAreaDTO(
                "Salud", 0, 0, 0, 0, 0.0, 0.0);

        assertEquals(0, resultado.getOportunidadesEnviadas());
        assertEquals(0.0, resultado.getPorcentajeResolucion(), 0.001);
        assertEquals(TiempoResolucionAreaDTO.DIAGNOSTICO_AL_DIA,
                resultado.getDiagnostico());
    }

    @Test
    public void pendientesSinAtrasoSonRecientes() {
        TiempoResolucionAreaDTO resultado = new TiempoResolucionAreaDTO(
                "Finanzas", 3, 1, 2, 0, 2.0, 33.33);

        assertEquals(TiempoResolucionAreaDTO.DIAGNOSTICO_PENDIENTES_RECIENTES,
                resultado.getDiagnostico());
    }
}
