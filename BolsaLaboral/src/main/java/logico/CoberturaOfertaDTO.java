package logico;

/** Resultado agregado de cobertura para una oferta laboral activa. */
public class CoberturaOfertaDTO {

    public static final String DIAGNOSTICO_SIN_CANDIDATOS = "Sin candidatos vinculados";
    public static final String DIAGNOSTICO_SIN_CONVERSIONES = "Sin conversiones";
    public static final String DIAGNOSTICO_COBERTURA_PARCIAL = "Cobertura parcial";
    public static final String DIAGNOSTICO_COMPLETADA = "Completada";

    private final String oferta;
    private final String centroEmpleador;
    private final String areaLaboral;
    private final int vacantesTotales;
    private final int vacantesOcupadas;
    private final int oportunidadesEnviadas;
    private final double porcentajeCobertura;

    public CoberturaOfertaDTO(String oferta, String centroEmpleador, String areaLaboral,
                              int vacantesTotales, int vacantesOcupadas,
                              int oportunidadesEnviadas, double porcentajeCobertura) {
        this.oferta = oferta;
        this.centroEmpleador = centroEmpleador;
        this.areaLaboral = areaLaboral;
        this.vacantesTotales = vacantesTotales;
        this.vacantesOcupadas = vacantesOcupadas;
        this.oportunidadesEnviadas = oportunidadesEnviadas;
        this.porcentajeCobertura = porcentajeCobertura;
    }

    public String getOferta() {
        return oferta;
    }

    public String getCentroEmpleador() {
        return centroEmpleador;
    }

    public String getAreaLaboral() {
        return areaLaboral;
    }

    public int getVacantesTotales() {
        return vacantesTotales;
    }

    public int getVacantesOcupadas() {
        return vacantesOcupadas;
    }

    public int getVacantesPendientes() {
        return Math.max(0, vacantesTotales - vacantesOcupadas);
    }

    public int getOportunidadesEnviadas() {
        return oportunidadesEnviadas;
    }

    public double getPorcentajeCobertura() {
        return porcentajeCobertura;
    }

    public String getDiagnostico() {
        if (getVacantesPendientes() == 0) {
            return DIAGNOSTICO_COMPLETADA;
        }
        if (oportunidadesEnviadas == 0) {
            return DIAGNOSTICO_SIN_CANDIDATOS;
        }
        if (vacantesOcupadas == 0) {
            return DIAGNOSTICO_SIN_CONVERSIONES;
        }
        return DIAGNOSTICO_COBERTURA_PARCIAL;
    }
}
