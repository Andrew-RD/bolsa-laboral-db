package logico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoberturaOfertaDTOTest {

    @Test
    public void calculaVacantesPendientesYConservaPorcentaje() {
        CoberturaOfertaDTO resultado = new CoberturaOfertaDTO(
                "Analista", "Centro", "TI", 5, 2, 3, 40.0);

        assertEquals(3, resultado.getVacantesPendientes());
        assertEquals(40.0, resultado.getPorcentajeCobertura(), 0.001);
        assertEquals(CoberturaOfertaDTO.DIAGNOSTICO_COBERTURA_PARCIAL,
                resultado.getDiagnostico());
    }

    @Test
    public void reconoceOfertaSinCandidatosVinculados() {
        CoberturaOfertaDTO resultado = new CoberturaOfertaDTO(
                "Analista", "Centro", "TI", 2, 0, 0, 0.0);

        assertEquals(CoberturaOfertaDTO.DIAGNOSTICO_SIN_CANDIDATOS,
                resultado.getDiagnostico());
    }

    @Test
    public void reconoceOfertaSinConversiones() {
        CoberturaOfertaDTO resultado = new CoberturaOfertaDTO(
                "Analista", "Centro", "TI", 2, 0, 4, 0.0);

        assertEquals(CoberturaOfertaDTO.DIAGNOSTICO_SIN_CONVERSIONES,
                resultado.getDiagnostico());
    }

    @Test
    public void reconoceOfertaCompletada() {
        CoberturaOfertaDTO resultado = new CoberturaOfertaDTO(
                "Analista", "Centro", "TI", 2, 2, 2, 100.0);

        assertEquals(0, resultado.getVacantesPendientes());
        assertEquals(CoberturaOfertaDTO.DIAGNOSTICO_COMPLETADA,
                resultado.getDiagnostico());
    }
}
