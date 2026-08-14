package logico;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Universitario extends Candidato implements Serializable{

	private static final long serialVersionUID = 1L;

	private String universidad;
	private Integer universidadId;
	private String carrera;
	private String nivelAcademico;
	private SituacionAcademica situacionAcademica;

	public Universitario(String codigo, String identificacion, String nombres, String apellidos,
	                     LocalDate fechaNacimiento, String genero, String provincia, String municipio, String telefono,
	                     String correo, String jornada, String modalidad, String areaDeInteres, float aspiracionSalarial,
	                     boolean licenciaConducir, boolean disposicionMudarse, ArrayList<String> idiomas,String universidad, String carrera, String nivelAcademico, String estado) {
		this(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero, provincia, municipio,
				telefono, correo, jornada, modalidad, areaDeInteres, aspiracionSalarial,
				licenciaConducir, disposicionMudarse, idiomas, universidad, carrera, nivelAcademico,
				SituacionAcademica.NO_ESPECIFICADO, estado);
	}

	public Universitario(String codigo, String identificacion, String nombres, String apellidos,
	                     LocalDate fechaNacimiento, String genero, String provincia, String municipio, String telefono,
	                     String correo, String jornada, String modalidad, String areaDeInteres, float aspiracionSalarial,
	                     boolean licenciaConducir, boolean disposicionMudarse, ArrayList<String> idiomas,
	                     String universidad, String carrera, String nivelAcademico,
	                     SituacionAcademica situacionAcademica, String estado) {
		super(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero, provincia, municipio, telefono,
				correo, jornada, modalidad, areaDeInteres, aspiracionSalarial, licenciaConducir, disposicionMudarse,
				idiomas, estado);
		this.universidad = universidad;
		this.carrera = carrera;
		this.nivelAcademico = nivelAcademico;
		this.situacionAcademica = situacionAcademica;
	}

	public int migrarSituacionDeserializada() {
		if (situacionAcademica == null) {
			situacionAcademica = SituacionAcademica.NO_ESPECIFICADO;
			return 1;
		}
		return 0;
	}

	public int migrarUniversidadDeserializada(CatalogoDatos catalogos) {
		if (catalogos == null) {
			return 0;
		}
		ElementoCatalogo porId = catalogos.buscarPorId(
				TipoCatalogo.UNIVERSIDADES, universidadId);
		if (porId != null) {
			return 0;
		}
		ElementoCatalogo porTexto = catalogos.buscarUniversidad(universidad);
		if (porTexto != null && !java.util.Objects.equals(
				porTexto.getId(), universidadId)) {
			universidadId = porTexto.getId();
			return 1;
		}
		return 0;
	}


	public String getUniversidad() {
		return universidad;
	}

	public void setUniversidad(String universidad) {
		this.universidad = universidad;
	}

	public Integer getUniversidadId() {
		return universidadId;
	}

	public void setUniversidadCatalogo(ElementoCatalogo universidadCatalogo) {
		if (universidadCatalogo == null) {
			universidadId = null;
			return;
		}
		universidadId = universidadCatalogo.getId();
		universidad = universidadCatalogo.getNombreCompleto();
	}

	public void setUniversidadLegada(String universidadLegada) {
		universidadId = null;
		universidad = universidadLegada;
	}

	public String getCarrera() {
		return carrera;
	}

	public void setCarrera(String carrera) {
		this.carrera = carrera;
	}

	public String getNivelAcademico() {
		return nivelAcademico;
	}

	public void setNivelAcademico(String nivelAcademico) {
		this.nivelAcademico = nivelAcademico;
	}

	public SituacionAcademica getSituacionAcademica() {
		if (situacionAcademica == null) {
			situacionAcademica = SituacionAcademica.NO_ESPECIFICADO;
		}
		return situacionAcademica;
	}

	public void setSituacionAcademica(SituacionAcademica situacionAcademica) {
		this.situacionAcademica = situacionAcademica == null
				? SituacionAcademica.NO_ESPECIFICADO : situacionAcademica;
	}


	@Override
	public String getSobreMi() {
		StringBuilder sb = new StringBuilder();
		sb.append("Soy ").append(getSituacionAcademica().getEtiqueta().toLowerCase())
				.append(" de ").append(getCarrera().toLowerCase())
				.append(" en la universidad ").append(getUniversidad())
				.append(", con nivel académico ").append(getNivelAcademico().toLowerCase()).append(". ");
		sb.append("Mi área de interés es ").append(getAreaDeInteres().toLowerCase()).append(". ");

		if (isLicenciaConducir()) sb.append("Cuento con licencia de conducir. ");
		if (isDisposicionMudarse()) sb.append("Estoy dispuesto a mudarme si es necesario para el empleo. ");

		sb.append("Busco oportunidades en modalidad ").append(getModalidad().toLowerCase())
				.append(" y jornada ").append(getJornada().toLowerCase())
				.append(", con una aspiración salarial de RD$").append(getAspiracionSalarial()).append(". ");

		if (!getIdiomas().isEmpty()) {
			sb.append("Tengo conocimientos en los siguientes idiomas: ");
			for (int i = 0; i < getIdiomas().size(); i++) {
				sb.append(getIdiomas().get(i));
				if (i < getIdiomas().size() - 2) sb.append(", ");
				else if (i == getIdiomas().size() - 2) sb.append(" y ");
			}
			sb.append(". ");
		}

		return sb.toString().trim();
	}


	@Override
	public String getFormacion() {
		StringBuilder sb = new StringBuilder();
		sb.append("Formación universitaria en la carrera de ").append(getCarrera().toLowerCase())
				.append(" en la universidad ").append(getUniversidad()).append(". ");
		sb.append("Mi nivel académico actual es ").append(getNivelAcademico().toLowerCase()).append(". ");

		return sb.toString().trim();
	}
}