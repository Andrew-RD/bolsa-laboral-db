package logico;

/** Validador del formato de RNC dominicano. */
public final class RncValidator {

    private RncValidator() {
    }

    public static ResultadoDocumento validar(String valor) {
        if (valor == null) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.FORMATO_INVALIDO, null,
                    "El RNC es obligatorio.");
        }
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
        return new ResultadoDocumento(ResultadoDocumento.Estado.VALIDO, normalizado,
                "Formato de RNC correcto.");
    }

    public static boolean esValido(String valor) {
        return validar(valor).esValido();
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El RNC es obligatorio.");
        }
        String normalizado = valor.replace(" ", "").replace("-", "");
        if (!normalizado.matches("[0-9]*")) {
            throw new IllegalArgumentException("Formato de RNC inválido.");
        }
        return normalizado;
    }
}
