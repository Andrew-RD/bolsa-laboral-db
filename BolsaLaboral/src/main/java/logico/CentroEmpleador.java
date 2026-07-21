package logico;

import java.io.Serializable;
import java.util.ArrayList;

public class CentroEmpleador implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String codigo;
	private String rnc;
	private String nombre;
	private String sector;
	private String provincia;
	private String municipio;
	private String telefono;
	private String correo;
	private ArrayList<OfertaLaboral> ofertasLaborales;

	public CentroEmpleador(String codigo, String nombre, String sector, String provincia, String municipio, String telefono, String correo, String rnc) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.sector = sector;
		this.provincia = provincia;
		this.municipio = municipio;
		this.telefono = telefono;
		this.correo = correo;
		this.rnc = rnc;
		this.ofertasLaborales = new ArrayList<>();
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String ciudad) {
		this.provincia = ciudad;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String direccion) {
		this.municipio = direccion;
	}

	public ArrayList<OfertaLaboral> getOfertasLaborales() {
		return ofertasLaborales;
	}

	public void setOfertasLaborales(ArrayList<OfertaLaboral> ofertasLaborales) {
		this.ofertasLaborales = ofertasLaborales;
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

	public String getRnc() {
		return rnc;
	}

	public void setRnc(String rnc) {
		this.rnc = rnc;
	}

}
