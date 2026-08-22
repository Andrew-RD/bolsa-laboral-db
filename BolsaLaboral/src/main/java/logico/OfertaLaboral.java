package logico;

import java.io.Serializable;
import java.util.ArrayList;

public class OfertaLaboral implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String ESTADO_ACTIVA = "Activa";
    public static final String ESTADO_COMPLETADA = "Completada";

    private String codigo;
    private String puesto;
    private String descripcion;
    private String area;
    private String modalidad;
    private String jornada;
    private String estado;
    private float salario;
    private int experienciaMinima;

    /**
     * Campo histórico. En objetos legados representa disponibles; en el modelo
     * actual se mantiene como caché compatible de disponibles.
     */
    private int vacantes;
    private int vacantesTotales;
    private int vacantesOcupadas;
    private Boolean esquemaVacantesTotales;

    private CentroEmpleador ofertador;
    private boolean ofreceReubicacion;
    private boolean obligatorioMayorDeEdad;
    private boolean obligatorioLicencia;
    private String nivelAcademico;
    private TipoCandidato tipoCandidatoRequerido;
    private ArrayList<String> requisitos;
    private ArrayList<String> idiomasRequeridas;
    private int porcentajeMinimo;

    public OfertaLaboral(String codigo, String puesto, String descripcion, String area,
            String modalidad, String jornada, String estado, float salario, int experienciaMinima,
            int vacantes, CentroEmpleador ofertador, boolean ofreceReubicacion,
            boolean mayorDeEdadObligatorio, boolean obligatorioLicencia, String nivelAcademico,
            ArrayList<String> requisitos, ArrayList<String> idiomasRequeridas, int porcentajeMinimo) {
        this(codigo, puesto, descripcion, area, modalidad, jornada, estado, salario,
                experienciaMinima, vacantes, ofertador, ofreceReubicacion,
                mayorDeEdadObligatorio, obligatorioLicencia, nivelAcademico,
                TipoCandidato.desdeTextoLegado(nivelAcademico), requisitos,
                idiomasRequeridas, porcentajeMinimo);
    }

    public OfertaLaboral(String codigo, String puesto, String descripcion, String area,
            String modalidad, String jornada, String estado, float salario, int experienciaMinima,
            int vacantes, CentroEmpleador ofertador, boolean ofreceReubicacion,
            boolean mayorDeEdadObligatorio, boolean obligatorioLicencia, String nivelAcademico,
            TipoCandidato tipoCandidatoRequerido, ArrayList<String> requisitos,
            ArrayList<String> idiomasRequeridas, int porcentajeMinimo) {
        this.codigo = codigo;
        this.puesto = puesto;
        this.area = area;
        this.descripcion = descripcion;
        this.modalidad = modalidad;
        this.jornada = jornada;
        this.estado = estado;
        this.salario = salario;
        this.experienciaMinima = experienciaMinima;
        this.vacantesTotales = Math.max(0, vacantes);
        this.vacantesOcupadas = 0;
        this.vacantes = this.vacantesTotales;
        this.esquemaVacantesTotales = Boolean.TRUE;
        this.ofertador = ofertador;
        this.ofreceReubicacion = ofreceReubicacion;
        this.obligatorioMayorDeEdad = mayorDeEdadObligatorio;
        this.obligatorioLicencia = obligatorioLicencia;
        this.nivelAcademico = nivelAcademico;
        this.tipoCandidatoRequerido = tipoCandidatoRequerido;
        this.requisitos = requisitos == null ? new ArrayList<String>() : requisitos;
        this.idiomasRequeridas = idiomasRequeridas == null
                ? new ArrayList<String>() : idiomasRequeridas;
        this.porcentajeMinimo = porcentajeMinimo;
        sincronizarEstado();
    }

    /**
     * Migra una oferta deserializada. Para el esquema antiguo, {@code vacantes}
     * se interpreta como disponibles y el total se infiere sumando ocupadas.
     */
    public int migrarDatosDeserializados(int ocupadasDerivadas) {
        int cambios = 0;
        int ocupadas = Math.max(0, ocupadasDerivadas);
        if (!Boolean.TRUE.equals(esquemaVacantesTotales)) {
            vacantesTotales = Math.max(0, vacantes) + ocupadas;
            esquemaVacantesTotales = Boolean.TRUE;
            cambios++;
        }
        if (vacantesOcupadas != ocupadas) {
            vacantesOcupadas = ocupadas;
            cambios++;
        }
        if (requisitos == null) {
            requisitos = new ArrayList<String>();
            cambios++;
        }
        if (idiomasRequeridas == null) {
            idiomasRequeridas = new ArrayList<String>();
            cambios++;
        }
        if (tipoCandidatoRequerido == null) {
            TipoCandidato tipoInferido = TipoCandidato.desdeTextoLegado(nivelAcademico);
            if (tipoInferido != null) {
                tipoCandidatoRequerido = tipoInferido;
                cambios++;
            }
        }
        int disponibles = getVacantesDisponibles();
        if (vacantes != disponibles) {
            vacantes = disponibles;
            cambios++;
        }
        String estadoAnterior = estado;
        sincronizarEstado();
        if (estadoAnterior == null || !estadoAnterior.equals(estado)) {
            cambios++;
        }
        return cambios;
    }

    public void sincronizarVacantesOcupadas(int ocupadasDerivadas) {
        int ocupadas = Math.max(0, ocupadasDerivadas);
        vacantesOcupadas = ocupadas;
        vacantes = getVacantesDisponibles();
        esquemaVacantesTotales = Boolean.TRUE;
        sincronizarEstado();
    }

    private void sincronizarEstado() {
        estado = getVacantesDisponibles() == 0 ? ESTADO_COMPLETADA : ESTADO_ACTIVA;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public float getSalario() {
        return salario;
    }

    public void setSalario(float salario) {
        this.salario = salario;
    }

    public int getExperienciaMinima() {
        return experienciaMinima;
    }

    public void setExperienciaMinima(int experienciaMinima) {
        this.experienciaMinima = experienciaMinima;
    }

    /** Alias histórico: retorna vacantes disponibles. */
    public int getVacantes() {
        return getVacantesDisponibles();
    }

    /**
     * Alias histórico para escenarios de compatibilidad y pruebas. Ajusta las
     * disponibles modificando el total, nunca las ocupadas.
     */
    public void setVacantes(int disponibles) {
        setVacantesTotales(vacantesOcupadas + Math.max(0, disponibles));
    }

    public int getVacantesTotales() {
        if (!Boolean.TRUE.equals(esquemaVacantesTotales)) {
            return Math.max(0, vacantes);
        }
        return Math.max(0, vacantesTotales);
    }

    public void setVacantesTotales(int total) {
        if (total < 0) {
            throw new IllegalArgumentException("Las vacantes totales no pueden ser negativas.");
        }
        if (total < vacantesOcupadas) {
            throw new IllegalArgumentException(
                    "Las vacantes totales no pueden ser menores que las vacantes ya ocupadas ("
                            + vacantesOcupadas + ").");
        }
        vacantesTotales = total;
        esquemaVacantesTotales = Boolean.TRUE;
        vacantes = getVacantesDisponibles();
        sincronizarEstado();
    }

    public int getVacantesOcupadas() {
        return Math.max(0, vacantesOcupadas);
    }

    public int getVacantesDisponibles() {
        if (!Boolean.TRUE.equals(esquemaVacantesTotales)) {
            return Math.max(0, vacantes);
        }
        return Math.max(0, vacantesTotales - Math.max(0, vacantesOcupadas));
    }

    public CentroEmpleador getOfertador() {
        return ofertador;
    }

    public void setOfertador(CentroEmpleador ofertador) {
        this.ofertador = ofertador;
    }

    public boolean isOfreceReubicacion() {
        return ofreceReubicacion;
    }

    public void setOfreceReubicacion(boolean ofreceReubicacion) {
        this.ofreceReubicacion = ofreceReubicacion;
    }

    public String getNivelAcademico() {
        return nivelAcademico;
    }

    public void setNivelAcademico(String nivelAcademico) {
        this.nivelAcademico = nivelAcademico;
        TipoCandidato inferido = TipoCandidato.desdeTextoLegado(nivelAcademico);
        if (inferido != null) {
            this.tipoCandidatoRequerido = inferido;
        }
    }

    public TipoCandidato getTipoCandidatoRequerido() {
        if (tipoCandidatoRequerido == null) {
            tipoCandidatoRequerido = TipoCandidato.desdeTextoLegado(nivelAcademico);
        }
        return tipoCandidatoRequerido;
    }

    public void setTipoCandidatoRequerido(TipoCandidato tipoCandidatoRequerido) {
        if (tipoCandidatoRequerido == null) {
            throw new IllegalArgumentException("El tipo de candidato requerido es obligatorio.");
        }
        this.tipoCandidatoRequerido = tipoCandidatoRequerido;
        this.nivelAcademico = tipoCandidatoRequerido.getEtiqueta();
    }

    public ArrayList<String> getRequisitos() {
        if (requisitos == null) {
            requisitos = new ArrayList<String>();
        }
        return requisitos;
    }

    public void setRequisitos(ArrayList<String> requisitos) {
        this.requisitos = requisitos == null ? new ArrayList<String>() : requisitos;
    }

    public ArrayList<String> getIdiomasRequeridas() {
        if (idiomasRequeridas == null) {
            idiomasRequeridas = new ArrayList<String>();
        }
        return idiomasRequeridas;
    }

    public void setIdiomasRequeridas(ArrayList<String> idiomasRequeridas) {
        this.idiomasRequeridas = idiomasRequeridas == null
                ? new ArrayList<String>() : idiomasRequeridas;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public boolean isObligatorioMayorDeEdad() {
        return obligatorioMayorDeEdad;
    }

    public void setObligatorioMayorDeEdad(boolean obligatorioMayorDeEdad) {
        this.obligatorioMayorDeEdad = obligatorioMayorDeEdad;
    }

    public boolean isobligatorioLicencia() {
        return obligatorioLicencia;
    }

    public void setobligatorioLicencia(boolean obligatorioLicencia) {
        this.obligatorioLicencia = obligatorioLicencia;
    }

    public void agregarIdioma(String idioma) {
        getIdiomasRequeridas().add(idioma);
    }

    public void agregarRequisito(String requisito) {
        getRequisitos().add(requisito);
    }

    public void clearRequisitos() {
        getRequisitos().clear();
    }

    public int getCantIdiomas() {
        return getIdiomasRequeridas().size();
    }

    public int getCantRequisitos() {
        return getRequisitos().size();
    }

    public int getPorcentajeMinimo() {
        return porcentajeMinimo;
    }

    public void setPorcentajeMinimo(int porcentajeMinimo) {
        this.porcentajeMinimo = porcentajeMinimo;
    }
}
