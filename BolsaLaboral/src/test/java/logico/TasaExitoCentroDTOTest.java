package logico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TasaExitoCentroDTOTest {

    @Test
    public void tasaCeroIndicaSinConversiones() {
        TasaExitoCentroDTO resultado = new TasaExitoCentroDTO("Centro A", 10, 0, 0.0);

        assertEquals(TasaExitoCentroDTO.DIAGNOSTICO_SIN_CONVERSIONES,
                resultado.getDiagnostico());
    }

    @Test
    public void tasaIntermediaIndicaConversionParcial() {
        TasaExitoCentroDTO resultado = new TasaExitoCentroDTO("Centro B", 3, 1, 33.33);

        assertEquals(33.33, resultado.getTasaConversion(), 0.001);
        assertEquals(TasaExitoCentroDTO.DIAGNOSTICO_CONVERSION_PARCIAL,
                resultado.getDiagnostico());
    }

    @Test
    public void tasaCienIndicaConversionTotal() {
        TasaExitoCentroDTO resultado = new TasaExitoCentroDTO("Centro C", 4, 4, 100.0);

        assertEquals(4, resultado.getOportunidadesEnviadas());
        assertEquals(TasaExitoCentroDTO.DIAGNOSTICO_CONVERSION_TOTAL,
                resultado.getDiagnostico());
    }
}
