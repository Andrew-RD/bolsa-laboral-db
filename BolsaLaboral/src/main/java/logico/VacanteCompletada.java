package logico;

import java.io.Serializable;
import java.time.LocalDate;

public class VacanteCompletada implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String codigo;
	private Solicitud solicitudAceptada;
	private OfertaLaboral ofertaOcupada;
	private LocalDate fechaContratacion;

	public VacanteCompletada(String codigo, Solicitud solicitudAceptada, OfertaLaboral ofertaOcupada,
			LocalDate fechaContratacion) {
		super();
		this.codigo = codigo;
		this.solicitudAceptada = solicitudAceptada;
		this.ofertaOcupada = ofertaOcupada;
		this.fechaContratacion = fechaContratacion;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Solicitud getSolicitudAceptada() {
		return solicitudAceptada;
	}

	public void setSolicitudAceptada(Solicitud solicitudAceptada) {
		this.solicitudAceptada = solicitudAceptada;
	}

	public OfertaLaboral getOfertaOcupada() {
		return ofertaOcupada;
	}

	public void setOfertaOcupada(OfertaLaboral ofertaOcupada) {
		this.ofertaOcupada = ofertaOcupada;
	}

	public LocalDate getFechaContratacion() {
		return fechaContratacion;
	}

	public void setFechaContratacion(LocalDate fechaContratacion) {
		this.fechaContratacion = fechaContratacion;
	}

}
