package logico;

import java.io.Serializable;
import java.time.LocalDate;

public class Solicitud implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String ESTADO_ENVIADA = "Enviada";
	public static final String ESTADO_APROBADA = "Aprobada";
	public static final String ESTADO_RECHAZADA = "Rechazada";
	private static final String ESTADO_APROBADA_LEGADO = "Aprovada";
	
	private String codigo;
	private LocalDate fechaSolicitud;
	private LocalDate fechaDecision;
	private String estado;
	private Candidato solicitante;
	private OfertaLaboral ofertaSolicitada;

	public Solicitud(String codigo, LocalDate fechaSolicitud, String estado, Candidato solicitante,
			OfertaLaboral ofertaSolicitada) {
		this(codigo, fechaSolicitud, null, estado, solicitante, ofertaSolicitada);
	}

	public Solicitud(String codigo, LocalDate fechaSolicitud, LocalDate fechaDecision, String estado,
			Candidato solicitante, OfertaLaboral ofertaSolicitada) {
		super();
		this.codigo = codigo;
		this.fechaSolicitud = fechaSolicitud;
		this.fechaDecision = fechaDecision;
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

	public LocalDate getFechaDecision() {
		return fechaDecision;
	}

	public void setFechaDecision(LocalDate fechaDecision) {
		this.fechaDecision = fechaDecision;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public static boolean esEstadoAprobada(String estado) {
		if (estado == null) {
			return false;
		}
		String normalizado = estado.trim();
		return ESTADO_APROBADA.equalsIgnoreCase(normalizado)
				|| ESTADO_APROBADA_LEGADO.equalsIgnoreCase(normalizado);
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
