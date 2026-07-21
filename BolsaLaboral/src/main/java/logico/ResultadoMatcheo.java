package logico;

public class ResultadoMatcheo {
	private OfertaLaboral oferta;
	private Candidato solicitante;
	private int porcentaje;
	private String condicion;

	public ResultadoMatcheo(OfertaLaboral oferta, Candidato solicitante, int porcentaje, String condicion) {
		super();
		this.oferta = oferta;
		this.solicitante = solicitante;
		this.porcentaje = porcentaje;
		this.condicion = condicion;
	}

	public OfertaLaboral getOferta() {
		return oferta;
	}

	public void setOferta(OfertaLaboral oferta) {
		this.oferta = oferta;
	}

	public Candidato getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(Candidato solicitante) {
		this.solicitante = solicitante;
	}

	public int getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(int porcentaje) {
		this.porcentaje = porcentaje;
	}

	public String getCondicion() {
		return condicion;
	}

	public void setCondicion(String condicion) {
		this.condicion = condicion;
	}

}
