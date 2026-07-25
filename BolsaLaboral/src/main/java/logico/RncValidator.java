package logico;

/** Validador del dígito verificador de RNC dominicano. */
public final class RncValidator {

    private static final int[] PESOS = {7, 9, 8, 6, 5, 4, 3, 2};

    private RncValidator() {
    }

    public static ResultadoDocumento validar(String valor) {
        String normalizado;
        try {
            normalizado = normalizar(valor);
        } catch (IllegalArgumentException exception) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.FORMATO_INVALIDO, null,
                    "El RNC solo puede contener 9 dígitos, espacios y guiones.");
        }
        if (normalizado.length() != 9) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.LONGITUD_INVALIDA, normalizado,
                    "El RNC debe contener exactamente 9 dígitos.");
        }
        if (esSecuenciaRepetida(normalizado) || "000000000".equals(normalizado)) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.SECUENCIA_INVALIDA, normalizado,
                    "El RNC contiene una secuencia evidentemente inválida.");
        }
        int suma = 0;
        for (int index = 0; index < PESOS.length; index++) {
            suma += Character.digit(normalizado.charAt(index), 10) * PESOS[index];
        }
        int esperado = 11 - (suma % 11);
        if (esperado == 11) {
            esperado = 2;
        } else if (esperado == 10) {
            esperado = 1;
        }
        int recibido = Character.digit(normalizado.charAt(8), 10);
        if (esperado != recibido) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.DIGITO_VERIFICADOR_INVALIDO,
                    normalizado, "El dígito verificador del RNC no es válido.");
        }
        return new ResultadoDocumento(ResultadoDocumento.Estado.VALIDO, normalizado, "RNC válido.");
    }

    public static boolean esValido(String valor) {
        return validar(valor).esValido();
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El RNC es obligatorio.");
        }
        String normalizado = valor.trim().replace("-", "").replaceAll("\\s+", "");
        if (!normalizado.matches("\\d*")) {
            throw new IllegalArgumentException("Formato de RNC inválido.");
        }
        return normalizado;
    }

    private static boolean esSecuenciaRepetida(String valor) {
        char primero = valor.charAt(0);
        for (int index = 1; index < valor.length(); index++) {
            if (valor.charAt(index) != primero) {
                return false;
            }
        }
        return true;
    }
}
