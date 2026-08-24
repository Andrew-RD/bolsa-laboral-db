package logico;

/** Resultado agregado de conversión de oportunidades para un centro empleador. */
public class TasaExitoCentroDTO {

    public static final String DIAGNOSTICO_SIN_CONVERSIONES = "Sin conversiones";
    public static final String DIAGNOSTICO_CONVERSION_PARCIAL = "Conversión parcial";
    public static final String DIAGNOSTICO_CONVERSION_TOTAL = "Conversión total";

    private final String centroEmpleador;
    private final int oportunidadesEnviadas;
    private final int contrataciones;
    private final double tasaConversion;

    public TasaExitoCentroDTO(String centroEmpleador, int oportunidadesEnviadas,
                              int contrataciones, double tasaConversion) {
        this.centroEmpleador = centroEmpleador;
        this.oportunidadesEnviadas = oportunidadesEnviadas;
        this.contrataciones = contrataciones;
        this.tasaConversion = tasaConversion;
    }

    public String getCentroEmpleador() {
        return centroEmpleador;
    }

    public int getOportunidadesEnviadas() {
        return oportunidadesEnviadas;
    }

    public int getContrataciones() {
        return contrataciones;
    }

    public double getTasaConversion() {
        return tasaConversion;
    }

    public String getDiagnostico() {
        if (tasaConversion <= 0) {
            return DIAGNOSTICO_SIN_CONVERSIONES;
        }
        if (tasaConversion >= 100) {
            return DIAGNOSTICO_CONVERSION_TOTAL;
        }
        return DIAGNOSTICO_CONVERSION_PARCIAL;
    }
}
