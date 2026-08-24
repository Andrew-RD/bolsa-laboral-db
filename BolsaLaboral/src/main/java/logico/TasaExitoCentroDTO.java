package logico;

/** Resultado agregado de tasa de éxito de contratación para un centro empleador. */
public class TasaExitoCentroDTO {

    public static final String DIAGNOSTICO_ALTO = "Alto desempeño";
    public static final String DIAGNOSTICO_MODERADO = "Desempeño moderado";
    public static final String DIAGNOSTICO_BAJO = "Bajo desempeño";

    private static final double UMBRAL_ALTO = 15;
    private static final double UMBRAL_MODERADO = 5;

    private final String centroEmpleador;
    private final int solicitudesRecibidas;
    private final int contrataciones;
    private final double tasaExito;

    public TasaExitoCentroDTO(String centroEmpleador, int solicitudesRecibidas,
                              int contrataciones, double tasaExito) {
        this.centroEmpleador = centroEmpleador;
        this.solicitudesRecibidas = solicitudesRecibidas;
        this.contrataciones = contrataciones;
        this.tasaExito = tasaExito;
    }

    public String getCentroEmpleador() {
        return centroEmpleador;
    }

    public int getSolicitudesRecibidas() {
        return solicitudesRecibidas;
    }

    public int getContrataciones() {
        return contrataciones;
    }

    public double getTasaExito() {
        return tasaExito;
    }

    public String getDiagnostico() {
        if (tasaExito >= UMBRAL_ALTO) {
            return DIAGNOSTICO_ALTO;
        }
        if (tasaExito >= UMBRAL_MODERADO) {
            return DIAGNOSTICO_MODERADO;
        }
        return DIAGNOSTICO_BAJO;
    }
}