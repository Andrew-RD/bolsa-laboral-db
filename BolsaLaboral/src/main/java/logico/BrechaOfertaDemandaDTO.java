package logico;

/** Resultado agregado de oferta y demanda para un área laboral. */
public class BrechaOfertaDemandaDTO {

    public static final String DIAGNOSTICO_ESCASEZ = "Faltan candidatos";
    public static final String DIAGNOSTICO_EQUILIBRIO = "Equilibrio";
    public static final String DIAGNOSTICO_DISPONIBILIDAD = "Mayor disponibilidad";

    private final String areaLaboral;
    private final int ofertasActivas;
    private final int candidatosDesempleados;

    public BrechaOfertaDemandaDTO(String areaLaboral, int ofertasActivas,
                                  int candidatosDesempleados) {
        this.areaLaboral = areaLaboral;
        this.ofertasActivas = ofertasActivas;
        this.candidatosDesempleados = candidatosDesempleados;
    }

    public String getAreaLaboral() {
        return areaLaboral;
    }

    public int getOfertasActivas() {
        return ofertasActivas;
    }

    public int getCandidatosDesempleados() {
        return candidatosDesempleados;
    }

    public int getBalance() {
        return candidatosDesempleados - ofertasActivas;
    }

    public String getDiagnostico() {
        int balance = getBalance();
        if (balance < 0) {
            return DIAGNOSTICO_ESCASEZ;
        }
        if (balance == 0) {
            return DIAGNOSTICO_EQUILIBRIO;
        }
        return DIAGNOSTICO_DISPONIBILIDAD;
    }
}
