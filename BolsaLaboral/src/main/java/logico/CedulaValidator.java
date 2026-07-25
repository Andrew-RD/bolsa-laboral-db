package logico;

/**
 * Valida únicamente la estructura matemática de una cédula dominicana.
 *
 * <p>Después de retirar espacios y guiones se exigen once dígitos ASCII. Sobre
 * los primeros diez se aplican, de izquierda a derecha, factores alternos 1 y
 * 2. Cuando un producto tiene dos dígitos se suman sus dígitos, operación
 * equivalente a restarle 9. El dígito esperado es
 * {@code (10 - suma % 10) % 10} y se compara con el undécimo dígito. Esta
 * comprobación no acredita que una persona o identidad exista realmente.</p>
 */
public final class CedulaValidator {

    private CedulaValidator() {
    }

    public static ResultadoDocumento validar(String valor) {
        String normalizado;
        try {
            normalizado = normalizar(valor);
        } catch (IllegalArgumentException exception) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.FORMATO_INVALIDO, null,
                    exception.getMessage());
        }
        if (normalizado.length() != 11) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.LONGITUD_INVALIDA, normalizado,
                    "La cédula debe contener exactamente 11 dígitos.");
        }
        if (esSecuenciaRepetida(normalizado) || "00000000000".equals(normalizado)) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.SECUENCIA_INVALIDA, normalizado,
                    "La cédula contiene una secuencia evidentemente inválida.");
        }
        int suma = 0;
        for (int index = 0; index < 10; index++) {
            int producto = Character.digit(normalizado.charAt(index), 10)
                    * (index % 2 == 0 ? 1 : 2);
            suma += producto >= 10 ? producto - 9 : producto;
        }
        int esperado = (10 - (suma % 10)) % 10;
        int recibido = Character.digit(normalizado.charAt(10), 10);
        if (esperado != recibido) {
            return new ResultadoDocumento(ResultadoDocumento.Estado.DIGITO_VERIFICADOR_INVALIDO,
                    normalizado, "El dígito verificador de la cédula no es válido.");
        }
        return new ResultadoDocumento(ResultadoDocumento.Estado.VALIDO, normalizado,
                "La estructura y el dígito verificador son válidos; "
                        + "esto no confirma identidad ni existencia.");
    }

    public static boolean esValida(String valor) {
        return validar(valor).esValido();
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("La cédula es obligatoria.");
        }
        String normalizado = valor.trim().replace("-", "").replaceAll("\\s+", "");
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException("La cédula es obligatoria.");
        }
        if (!normalizado.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "La cédula solo puede contener dígitos, espacios y guiones.");
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
