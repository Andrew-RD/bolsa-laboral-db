package logico;

public class ManoObraMunicipioDTO {

    public static final String DIAGNOSTICO_EXCESO = "Exceso de mano de obra";
    public static final String DIAGNOSTICO_EQUILIBRIO = "Equilibrio";
    public static final String DIAGNOSTICO_OPORTUNIDAD = "Más vacantes que candidatos";

    private final String provincia;
    private final String municipio;
    private final int candidatosDesempleados;
    private final int vacantesDisponibles;

    public ManoObraMunicipioDTO(String provincia, String municipio,
                                int candidatosDesempleados, int vacantesDisponibles) {
        this.provincia = provincia;
        this.municipio = municipio;
        this.candidatosDesempleados = candidatosDesempleados;
        this.vacantesDisponibles = vacantesDisponibles;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public int getCandidatosDesempleados() {
        return candidatosDesempleados;
    }

    public int getVacantesDisponibles() {
        return vacantesDisponibles;
    }

    public int getBalance() {
        return candidatosDesempleados - vacantesDisponibles;
    }

    public String getDiagnostico() {
        int balance = getBalance();
        if (balance > 0) {
            return DIAGNOSTICO_EXCESO;
        }
        if (balance == 0) {
            return DIAGNOSTICO_EQUILIBRIO;
        }
        return DIAGNOSTICO_OPORTUNIDAD;
    }
}