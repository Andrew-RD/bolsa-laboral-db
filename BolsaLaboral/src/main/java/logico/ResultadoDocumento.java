package logico;

/** Resultado detallado y reutilizable de una validación de documento. */
public final class ResultadoDocumento {

    public enum Estado {
        VALIDO,
        FORMATO_INVALIDO,
        LONGITUD_INVALIDA,
        SECUENCIA_INVALIDA,
        DIGITO_VERIFICADOR_INVALIDO
    }

    private final Estado estado;
    private final String normalizado;
    private final String mensaje;

    public ResultadoDocumento(Estado estado, String normalizado, String mensaje) {
        this.estado = estado;
        this.normalizado = normalizado;
        this.mensaje = mensaje;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getNormalizado() {
        return normalizado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean esValido() {
        return estado == Estado.VALIDO;
    }
}
