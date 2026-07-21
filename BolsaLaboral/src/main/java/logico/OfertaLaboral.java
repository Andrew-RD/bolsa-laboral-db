package logico;

import java.io.Serializable;
import java.util.ArrayList;

public class OfertaLaboral implements Serializable{

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
	private int vacantes;
	private CentroEmpleador ofertador;
	private boolean ofreceReubicacion;
	private boolean obligatorioMayorDeEdad;
	private boolean obligatorioLicencia;
	private String nivelAcademico;
	private ArrayList<String> requisitos;
	private ArrayList<String> idiomasRequeridas;
	private int porcentajeMinimo;
	
	public OfertaLaboral(String codigo, String puesto, String descripcion, String area,
			String modalidad, String jornada, String estado, float salario, int experienciaMinima, int vacantes,
			CentroEmpleador ofertador, boolean ofreceReubicacion, boolean mayorDeEdadObligatorio, boolean obligatorioLicencia,
			String nivelAcademico, ArrayList<String> requisitos, ArrayList<String> idiomasRequeridas, int porcentajeMinimo) {
		super();
		this.codigo = codigo;
		this.puesto = puesto;
		this.area = area;
		this.descripcion = descripcion;
		this.modalidad = modalidad;
		this.jornada = jornada;
		this.estado = estado;
		this.salario = salario;
		this.experienciaMinima = experienciaMinima;
		this.vacantes = Math.max(0, vacantes);
		this.ofertador = ofertador;
		this.ofreceReubicacion = ofreceReubicacion;
		this.obligatorioMayorDeEdad = mayorDeEdadObligatorio;
		this.obligatorioLicencia = obligatorioLicencia;
		this.nivelAcademico = nivelAcademico;
		this.requisitos = requisitos;
		this.idiomasRequeridas = idiomasRequeridas;
		this.porcentajeMinimo = porcentajeMinimo;
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

	public int getVacantes() {
		return vacantes;
	}

	public void setVacantes(int vacantes) {
		this.vacantes = Math.max(0, vacantes);
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
	}

	public ArrayList<String> getRequisitos() {
		return requisitos;
	}

	public void setRequisitos(ArrayList<String> requisitos) {
		this.requisitos = requisitos;
	}

	public ArrayList<String> getIdiomasRequeridas() {
		return idiomasRequeridas;
	}

	public void setIdiomasRequeridas(ArrayList<String> idiomasRequeridas) {
		this.idiomasRequeridas = idiomasRequeridas;
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
		idiomasRequeridas.add(idioma);
	}
	
	public void agregarRequisito(String requisito) {
		requisitos.add(requisito);
	}
	
	public void clearRequisitos() {
		requisitos.clear();
	}
	
	public int getCantIdiomas() {
		return idiomasRequeridas.size();
	}
	
	public int getCantRequisitos() {
		return requisitos.size();
	}

	public int getPorcentajeMinimo() {
		return porcentajeMinimo;
	}

	public void setPorcentajeMinimo(int porcentajeMinimo) {
		this.porcentajeMinimo = porcentajeMinimo;
	}
	
}
