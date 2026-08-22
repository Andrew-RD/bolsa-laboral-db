package logico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrechaOfertaDemandaDTOTest {

    @Test
    public void balanceNegativoIndicaQueFaltanCandidatos() {
        BrechaOfertaDemandaDTO resultado = new BrechaOfertaDemandaDTO("TI", 3, 1);

        assertEquals(-2, resultado.getBalance());
        assertEquals(BrechaOfertaDemandaDTO.DIAGNOSTICO_ESCASEZ,
                resultado.getDiagnostico());
    }

    @Test
    public void balanceCeroIndicaEquilibrio() {
        BrechaOfertaDemandaDTO resultado = new BrechaOfertaDemandaDTO("Salud", 2, 2);

        assertEquals(0, resultado.getBalance());
        assertEquals(BrechaOfertaDemandaDTO.DIAGNOSTICO_EQUILIBRIO,
                resultado.getDiagnostico());
    }

    @Test
    public void balancePositivoIndicaMayorDisponibilidad() {
        BrechaOfertaDemandaDTO resultado =
                new BrechaOfertaDemandaDTO("Administración", 2, 5);

        assertEquals(3, resultado.getBalance());
        assertEquals(BrechaOfertaDemandaDTO.DIAGNOSTICO_DISPONIBILIDAD,
                resultado.getDiagnostico());
    }
}
