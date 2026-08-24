package logico;

/** Resultado agregado del tiempo de resolución de vinculaciones por área laboral. */
public class TiempoResolucionAreaDTO {

    public static final String DIAGNOSTICO_AL_DIA = "Al día";
    public static final String DIAGNOSTICO_PENDIENTES_RECIENTES = "Pendientes recientes";
    public static final String DIAGNOSTICO_REQUIERE_ATENCION = "Requiere atención";

    private final String areaLaboral;
    private final int oportunidadesEnviadas;
    private final int vinculacionesResueltas;
    private final int vinculacionesPendientes;
    private final int pendientesMasSieteDias;
    private final double diasPromedioResolucion;
    private final double porcentajeResolucion;

    public TiempoResolucionAreaDTO(String areaLaboral, int oportunidadesEnviadas,
                                   int vinculacionesResueltas, int vinculacionesPendientes,
                                   int pendientesMasSieteDias, double diasPromedioResolucion,
                                   double porcentajeResolucion) {
        this.areaLaboral = areaLaboral;
        this.oportunidadesEnviadas = oportunidadesEnviadas;
        this.vinculacionesResueltas = vinculacionesResueltas;
        this.vinculacionesPendientes = vinculacionesPendientes;
        this.pendientesMasSieteDias = pendientesMasSieteDias;
        this.diasPromedioResolucion = diasPromedioResolucion;
        this.porcentajeResolucion = porcentajeResolucion;
    }

    public String getAreaLaboral() {
        return areaLaboral;
    }

    public int getOportunidadesEnviadas() {
        return oportunidadesEnviadas;
    }

    public int getVinculacionesResueltas() {
        return vinculacionesResueltas;
    }

    public int getVinculacionesPendientes() {
        return vinculacionesPendientes;
    }

    public int getPendientesMasSieteDias() {
        return pendientesMasSieteDias;
    }

    public double getDiasPromedioResolucion() {
        return diasPromedioResolucion;
    }

    public double getPorcentajeResolucion() {
        return porcentajeResolucion;
    }

    public String getDiagnostico() {
        if (vinculacionesPendientes == 0) {
            return DIAGNOSTICO_AL_DIA;
        }
        if (pendientesMasSieteDias == 0) {
            return DIAGNOSTICO_PENDIENTES_RECIENTES;
        }
        return DIAGNOSTICO_REQUIERE_ATENCION;
    }
}
