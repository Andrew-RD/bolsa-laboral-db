package logico;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Universitario extends Candidato implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String universidad;
	private String carrera;
	private String nivelAcademico;

	public Universitario(String codigo, String identificacion, String nombres, String apellidos,
			LocalDate fechaNacimiento, String genero, String provincia, String municipio, String telefono,
			String correo, String jornada, String modalidad, String areaDeInteres, float aspiracionSalarial,
			boolean licenciaConducir, boolean disposicionMudarse, ArrayList<String> idiomas,String universidad, String carrera, String nivelAcademico, String estado) {
		super(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero, provincia, municipio, telefono,
				correo, jornada, modalidad, areaDeInteres, aspiracionSalarial, licenciaConducir, disposicionMudarse,
				idiomas, estado);
		this.universidad = universidad;
		this.carrera = carrera;
		this.nivelAcademico = nivelAcademico;
	}


	public String getUniversidad() {
		return universidad;
	}

	public void setUniversidad(String universidad) {
		this.universidad = universidad;
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


	@Override
	public String getSobreMi() {
		StringBuilder sb = new StringBuilder();
		sb.append("Soy estudiante de ").append(getCarrera().toLowerCase())
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
		sb.append("Estoy cursando estudios universitarios en la carrera de ").append(getCarrera().toLowerCase())
		.append(" en la universidad ").append(getUniversidad()).append(". ");
		sb.append("Mi nivel académico actual es ").append(getNivelAcademico().toLowerCase()).append(". ");

		return sb.toString().trim();
	}
}
