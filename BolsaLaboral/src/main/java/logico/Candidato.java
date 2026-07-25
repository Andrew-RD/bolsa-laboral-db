package logico;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public abstract class Candidato implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String ESTADO_DESEMPLEADO = "Desempleado";
	public static final String ESTADO_EN_ESPERA = "En Espera";
	public static final String ESTADO_EMPLEADO = "Empleado";
	
	private String codigo;
	private String identificacion;
	private String nombres;
	private String apellidos;
	private LocalDate fechaNacimiento;
	private String genero;
	private String provincia;
	private String municipio;
	private String telefono;
	private String correo;
	private String jornada;
	private String modalidad;
	private String areaDeInteres;
	private float aspiracionSalarial;
	private boolean licenciaConducir;
	private boolean disposicionMudarse;
	private String estado;
	private ArrayList<String> idiomas;
	private ArrayList<Solicitud> misSolicitudes;
	private TipoCandidato tipoCandidato;

	public Candidato(String codigo, String identificacion, String nombres, String apellidos, LocalDate fechaNacimiento,
			String genero, String provincia, String municipio, String telefono, String correo, String jornada,
			String modalidad, String areaDeInteres, float aspiracionSalarial, boolean licenciaConducir,
			boolean disposicionMudarse, ArrayList<String> idiomas, String estado) {
		super();
		this.codigo = codigo;
		this.identificacion = identificacion;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.fechaNacimiento = fechaNacimiento;
		this.genero = genero;
		this.provincia = provincia;
		this.municipio = municipio;
		this.telefono = telefono;
		this.correo = correo;
		this.jornada = jornada;
		this.modalidad = modalidad;
		this.areaDeInteres = areaDeInteres;
		this.aspiracionSalarial = aspiracionSalarial;
		this.licenciaConducir = licenciaConducir;
		this.disposicionMudarse = disposicionMudarse;
		this.idiomas = idiomas;
		this.misSolicitudes =  new ArrayList<Solicitud> ();
		this.estado = estado;
	}

	public int migrarDatosDeserializados() {
		int cambios = 0;
		if (idiomas == null) {
			idiomas = new ArrayList<String>();
			cambios++;
		}
		if (misSolicitudes == null) {
			misSolicitudes = new ArrayList<Solicitud>();
			cambios++;
		}
		if (tipoCandidato == null) {
			if (this instanceof Universitario) {
				tipoCandidato = TipoCandidato.UNIVERSITARIO;
			} else if (this instanceof TecnicoSuperior) {
				tipoCandidato = TipoCandidato.TECNICO;
			} else {
				tipoCandidato = TipoCandidato.OBRERO;
			}
			cambios++;
		}
		if (this instanceof Universitario) {
			cambios += ((Universitario) this).migrarSituacionDeserializada();
		}
		return cambios;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getJornada() {
		return jornada;
	}

	public void setJornada(String jornada) {
		this.jornada = jornada;
	}

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}

	public float getAspiracionSalarial() {
		return aspiracionSalarial;
	}

	public void setAspiracionSalarial(float aspiracionSalarial) {
		this.aspiracionSalarial = aspiracionSalarial;
	}

	public boolean isLicenciaConducir() {
		return licenciaConducir;
	}

	public void setLicenciaConducir(boolean licenciaConducir) {
		this.licenciaConducir = licenciaConducir;
	}

	public boolean isDisposicionMudarse() {
		return disposicionMudarse;
	}

	public void setDisposicionMudarse(boolean disposicionMudarse) {
		this.disposicionMudarse = disposicionMudarse;
	}

	public ArrayList<String> getIdiomas() {
		return idiomas;
	}

	public void setIdiomas(ArrayList<String> idiomas) {
		this.idiomas = idiomas;
	}

	public ArrayList<Solicitud> getMisSolicitudes() {
		return misSolicitudes;
	}

	public void setMisSolicitudes(ArrayList<Solicitud> misSolicitudes) {
		this.misSolicitudes = misSolicitudes;
	}

	public String getAreaDeInteres() {
		return areaDeInteres;
	}

	public void setAreaDeInteres(String areaDeInteres) {
		this.areaDeInteres = areaDeInteres;
	}
	
	public abstract String getSobreMi();
	
	public abstract String getFormacion();
	
	/**
	 * Cierra las solicitudes que siguen pendientes después de una contratación.
	 * La solicitud aprobada debe marcarse antes de llamar este método y nunca se
	 * modifica aquí.
	 */
	public void cambiarEstadoSolicitudesAEmpleado() {
		if (misSolicitudes != null) {
			for (Solicitud solicitud : misSolicitudes) {
				if (solicitud != null && Solicitud.ESTADO_ENVIADA.equals(solicitud.getEstado())) {
					solicitud.setEstado(Solicitud.ESTADO_RECHAZADA);
				}
			}
		}
		actualizarEstadoLaboral();
	}

	/**
	 * Conserva la firma histórica, pero ya no cambia solicitudes en bloque.
	 * El estado laboral se deriva de los estados individuales vigentes.
	 */
	public void cambiarEstadoSolicitudesADesempleado() {
		actualizarEstadoLaboral();
	}

	public void actualizarEstadoLaboral() {
		boolean tieneAprobada = false;
		boolean tieneEnviada = false;
		if (misSolicitudes != null) {
			for (Solicitud solicitud : misSolicitudes) {
				if (solicitud == null) {
					continue;
				}
				String estadoSolicitud = solicitud.getEstado();
				String estadoNormalizado = estadoSolicitud == null ? null : estadoSolicitud.trim();
				if (Solicitud.ESTADO_APROBADA.equals(estadoNormalizado)) {
					tieneAprobada = true;
				} else if (Solicitud.ESTADO_ENVIADA.equals(estadoNormalizado)) {
					tieneEnviada = true;
				}
			}
		}

		if (tieneAprobada) {
			estado = ESTADO_EMPLEADO;
		} else if (tieneEnviada) {
			estado = ESTADO_EN_ESPERA;
		} else {
			estado = ESTADO_DESEMPLEADO;
		}
	}
	
	public int getEdad() {
		return Period.between(fechaNacimiento, LocalDate.now()).getYears();
	}

	public String getEstado() {
		return estado;
	}

	public String getDescripcionEstadoLaboral() {
		return descripcionEstadoLaboral(estado);
	}

	public static String descripcionEstadoLaboral(String estado) {
		if (ESTADO_EMPLEADO.equals(estado)) {
			return "Empleado — se deriva de una solicitud aprobada";
		}
		if (ESTADO_EN_ESPERA.equals(estado)) {
			return "En Espera — se deriva de solicitudes enviadas";
		}
		if (estado != null && !estado.trim().isEmpty()
				&& !ESTADO_DESEMPLEADO.equals(estado)) {
			return estado.trim()
					+ " — valor legado; se actualiza mediante las solicitudes";
		}
		return "Desempleado — se actualiza automáticamente al aprobar una solicitud";
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public TipoCandidato getTipoCandidato() {
		if (tipoCandidato == null) {
			if (this instanceof Universitario) {
				tipoCandidato = TipoCandidato.UNIVERSITARIO;
			} else if (this instanceof TecnicoSuperior) {
				tipoCandidato = TipoCandidato.TECNICO;
			} else {
				tipoCandidato = TipoCandidato.OBRERO;
			}
		}
		return tipoCandidato;
	}
	
	public void addSolicitud(Solicitud solicitud) {
		if (misSolicitudes == null) {
			misSolicitudes = new ArrayList<Solicitud>();
		}
		misSolicitudes.add(solicitud);
	}
	
}
