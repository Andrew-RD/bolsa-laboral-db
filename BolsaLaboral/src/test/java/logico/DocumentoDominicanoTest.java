package logico;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DocumentoDominicanoTest {

    @Test
    public void cedulaGeneradaEsValidaConYSinFormato() {
        String cedula = generarCedula("0010000000");
        assertEquals("00100000009", cedula);
        assertTrue(CedulaValidator.esValida(cedula));
        assertTrue(CedulaValidator.esValida(
                cedula.substring(0, 3) + "-" + cedula.substring(3, 10) + "-" + cedula.substring(10)));
        assertTrue(CedulaValidator.esValida(
                "  " + cedula.substring(0, 3) + " - "
                        + cedula.substring(3, 10) + " - "
                        + cedula.substring(10) + "  "));
        assertTrue(CedulaValidator.validar(cedula).getMensaje()
                .contains("no confirma identidad"));
    }

    @Test
    public void cedulaDistingueFormatoDigitoYSecuencia() {
        assertEquals(ResultadoDocumento.Estado.FORMATO_INVALIDO,
                CedulaValidator.validar("001A0000009").getEstado());
        assertEquals(ResultadoDocumento.Estado.DIGITO_VERIFICADOR_INVALIDO,
                CedulaValidator.validar("00100000008").getEstado());
        assertEquals(ResultadoDocumento.Estado.SECUENCIA_INVALIDA,
                CedulaValidator.validar("11111111111").getEstado());
    }

    @Test
    public void cedulaGeneradaDetectaLongitudVacioYDigitoAlterado() {
        for (String base : new String[]{
                "0010000034", "0010000042", "0010000059"}) {
            String cedula = generarCedula(base);
            assertTrue(CedulaValidator.esValida(cedula));
            int ultimo = Character.digit(cedula.charAt(10), 10);
            String alterada = cedula.substring(0, 10) + ((ultimo + 1) % 10);
            assertEquals(ResultadoDocumento.Estado.DIGITO_VERIFICADOR_INVALIDO,
                    CedulaValidator.validar(alterada).getEstado());
        }
        assertEquals(ResultadoDocumento.Estado.FORMATO_INVALIDO,
                CedulaValidator.validar("   -  - ").getEstado());
        assertEquals(ResultadoDocumento.Estado.LONGITUD_INVALIDA,
                CedulaValidator.validar("0010000000").getEstado());
        assertEquals(ResultadoDocumento.Estado.LONGITUD_INVALIDA,
                CedulaValidator.validar("001000000090").getEstado());
    }

    @Test
    public void rncAceptaNueveDigitosSinValidarDigitoVerificador() {
        ResultadoDocumento resultado = RncValidator.validar("101010102");

        assertTrue(resultado.esValido());
        assertEquals(ResultadoDocumento.Estado.VALIDO, resultado.getEstado());
        assertEquals("101010102", resultado.getNormalizado());
        assertEquals("Formato de RNC correcto.", resultado.getMensaje());
    }

    @Test
    public void rncAceptaGuionesYEspaciosYLosNormaliza() {
        assertTrue(RncValidator.esValido("1-01-01010-2"));
        assertEquals("101010102", RncValidator.normalizar("1-01-01010-2"));

        assertTrue(RncValidator.esValido(" 1 01 01010 2 "));
        assertEquals("101010102", RncValidator.normalizar(" 1 01 01010 2 "));
    }

    @Test
    public void rncRechazaLongitudesDistintasDeNueveDigitos() {
        assertEquals(ResultadoDocumento.Estado.LONGITUD_INVALIDA,
                RncValidator.validar("12345678").getEstado());
        assertEquals(ResultadoDocumento.Estado.LONGITUD_INVALIDA,
                RncValidator.validar("1234567890").getEstado());
    }

    @Test
    public void rncRechazaLetrasSimbolosYDigitosNoAscii() {
        assertEquals(ResultadoDocumento.Estado.FORMATO_INVALIDO,
                RncValidator.validar("101A10101").getEstado());
        assertEquals(ResultadoDocumento.Estado.FORMATO_INVALIDO,
                RncValidator.validar("101.010101").getEstado());
        assertEquals(ResultadoDocumento.Estado.FORMATO_INVALIDO,
                RncValidator.validar("١٢٣٤٥٦٧٨٩").getEstado());
    }

    @Test
    public void rncRechazaNull() {
        assertFalse(RncValidator.esValido(null));
        assertEquals(ResultadoDocumento.Estado.FORMATO_INVALIDO,
                RncValidator.validar(null).getEstado());
    }

    static String generarCedula(String baseDiezDigitos) {
        int suma = 0;
        for (int index = 0; index < 10; index++) {
            int producto = Character.digit(baseDiezDigitos.charAt(index), 10)
                    * (index % 2 == 0 ? 1 : 2);
            suma += producto >= 10 ? producto - 9 : producto;
        }
        return baseDiezDigitos + ((10 - suma % 10) % 10);
    }

    static String generarRnc(String baseOchoDigitos) {
        int[] pesos = {7, 9, 8, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int index = 0; index < pesos.length; index++) {
            suma += Character.digit(baseOchoDigitos.charAt(index), 10) * pesos[index];
        }
        int digito = 11 - suma % 11;
        digito = digito == 11 ? 2 : digito == 10 ? 1 : digito;
        return baseOchoDigitos + digito;
    }
}
