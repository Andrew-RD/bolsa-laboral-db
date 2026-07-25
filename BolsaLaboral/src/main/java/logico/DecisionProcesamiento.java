package logico;

/** Explicación central de si una oferta/resultado puede vincularse. */
public final class DecisionProcesamiento {

    private final boolean permitido;
    private final String razon;

    private DecisionProcesamiento(boolean permitido, String razon) {
        this.permitido = permitido;
        this.razon = razon;
    }

    public static DecisionProcesamiento permitir() {
        return new DecisionProcesamiento(true, "La oferta puede procesarse.");
    }

    public static DecisionProcesamiento rechazar(String razon) {
        return new DecisionProcesamiento(false, razon);
    }

    public boolean isPermitido() {
        return permitido;
    }

    public String getRazon() {
        return razon;
    }
}
