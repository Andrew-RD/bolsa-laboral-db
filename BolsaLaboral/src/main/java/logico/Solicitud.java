package logico;

import java.io.Serializable;
import java.time.LocalDate;

public class Solicitud implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String ESTADO_ENVIADA = "Enviada";
	public static final String ESTADO_APROBADA = "Aprobada";
	public static final String ESTADO_RECHAZADA = "Rechazada";
	
	private String codigo;
	private LocalDate fechaSolicitud;
	private String estado;
	private Candidato solicitante;
	private OfertaLaboral ofertaSolicitada;

	public Solicitud(String codigo, LocalDate fechaSolicitud, String estado, Candidato solicitante,
			OfertaLaboral ofertaSolicitada) {
		super();
		this.codigo = codigo;
		this.fechaSolicitud = fechaSolicitud;
		this.estado = estado;
		this.solicitante = solicitante;
		this.ofertaSolicitada = ofertaSolicitada;
	}


	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public LocalDate getFechaSolicitud() {
		return fechaSolicitud;
	}

	public void setFechaSolicitud(LocalDate fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Candidato getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(Candidato solicitante) {
		this.solicitante = solicitante;
	}

	public OfertaLaboral getOfertaSolicitada() {
		return ofertaSolicitada;
	}

	public void setOfertaSolicitada(OfertaLaboral ofertaSolicitada) {
		this.ofertaSolicitada = ofertaSolicitada;
	}
}
