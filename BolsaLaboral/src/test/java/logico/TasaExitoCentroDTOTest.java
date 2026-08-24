package logico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TasaExitoCentroDTOTest {

    @Test
    public void tasaMenorA5IndicaBajoDesempeno() {
        TasaExitoCentroDTO resultado = new TasaExitoCentroDTO("Centro A", 100, 2, 2.0);

        assertEquals(TasaExitoCentroDTO.DIAGNOSTICO_BAJO, resultado.getDiagnostico());
    }

    @Test
    public void tasaEntre5Y15IndicaDesempenoModerado() {
        TasaExitoCentroDTO resultado = new TasaExitoCentroDTO("Centro B", 100, 10, 10.0);

        assertEquals(TasaExitoCentroDTO.DIAGNOSTICO_MODERADO, resultado.getDiagnostico());
    }

    @Test
    public void tasaDe15OMasIndicaAltoDesempeno() {
        TasaExitoCentroDTO resultado = new TasaExitoCentroDTO("Centro C", 100, 20, 20.0);

        assertEquals(TasaExitoCentroDTO.DIAGNOSTICO_ALTO, resultado.getDiagnostico());
    }
}