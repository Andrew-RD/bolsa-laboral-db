package logico;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Obrero extends Candidato implements Serializable{

	private ArrayList <String> habilidades;
	private static final long serialVersionUID = 1L;

	public Obrero(String codigo, String identificacion, String nombres, String apellidos, LocalDate fechaNacimiento,
			String genero, String provincia, String municipio, String telefono, String correo, String jornada,
			String modalidad, String areaDeInteres, float aspiracionSalarial, boolean licenciaConducir,
			boolean disposicionMudarse, ArrayList<String> idiomas,ArrayList<String> habilidades, String estado) {
		super(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero, provincia, municipio, telefono,
				correo, jornada, modalidad, areaDeInteres, aspiracionSalarial, licenciaConducir, disposicionMudarse,
				idiomas,estado);
		this.habilidades = habilidades;
	}

	public ArrayList<String> getHabilidades() {
		return habilidades;
	}

	public void setHabilidades(ArrayList<String> habilidades) {
		this.habilidades = habilidades;
	}

	@Override
	public String getSobreMi() {
		StringBuilder sb = new StringBuilder();
		sb.append("Soy un trabajador con experiencia práctica en el área de ")
		.append(getAreaDeInteres().toLowerCase()).append(". ");

		if (!habilidades.isEmpty()) {
			sb.append("Cuento con habilidades como ");
			for (int i = 0; i < habilidades.size(); i++) {
				sb.append(habilidades.get(i).toLowerCase());
				if (i < habilidades.size() - 2) sb.append(", ");
				else if (i == habilidades.size() - 2) sb.append(" y ");
			}
			sb.append(". ");
		}

		if (isLicenciaConducir()) {
			sb.append("Poseo licencia de conducir. ");
		}
		if (isDisposicionMudarse()) {
			sb.append("Estoy dispuesto a mudarme por razones laborales. ");
		}

		sb.append("Busco una oportunidad de trabajo en modalidad ")
		.append(getModalidad().toLowerCase())
		.append(" y jornada ").append(getJornada().toLowerCase())
		.append(", con una aspiración salarial de RD$").append(getAspiracionSalarial()).append(". ");

		if (!getIdiomas().isEmpty()) {
			sb.append("También tengo conocimientos en los siguientes idiomas: ");
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
		sb.append("Mi formación se basa principalmente en la experiencia práctica adquirida en el área de ")
		.append(getAreaDeInteres().toLowerCase()).append(". ");

		if (!habilidades.isEmpty()) {
			sb.append("He desarrollado habilidades como ");
			for (int i = 0; i < habilidades.size(); i++) {
				sb.append(habilidades.get(i).toLowerCase());
				if (i < habilidades.size() - 2) sb.append(", ");
				else if (i == habilidades.size() - 2) sb.append(" y ");
			}
			sb.append(", que me permiten desempeñar eficientemente labores técnicas. ");
		}

		return sb.toString().trim();
	}


}
