package logico;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import exception.NotRemovableException;
import Datos.SolicitudDAO;
import Datos.ContratacionDAO;

public class BolsaLaboral implements Serializable{

	private static final long serialVersionUID = 1L;

	public static int genCodigoCandidato = 1;
	public static int genCodigoSolicitud = 1;
	public static int genCodigoOferta = 1;
	public static int genCodigoCentro = 1;
	public static int genCodigoVacanteCompletada = 1;
	private ArrayList<Candidato> candidatos;
	private ArrayList<Solicitud> solicitudes;
	private ArrayList<OfertaLaboral> ofertas;
	private ArrayList<CentroEmpleador> centros;
	private ArrayList<VacanteCompletada> vacantes;
	private ArrayList<Usuario> usuarios;
	private CatalogoDatos catalogos;
	public static BolsaLaboral instancia;
	private Usuario usuarioActual;

	private BolsaLaboral() {
		candidatos = new ArrayList<Candidato>();
		solicitudes = new ArrayList<Solicitud>();
		ofertas = new ArrayList<OfertaLaboral>();
		centros = new ArrayList<CentroEmpleador>();
		vacantes = new ArrayList<VacanteCompletada>();
		usuarios = new ArrayList<Usuario>();
		catalogos = new CatalogoDatos();
	}

	public ArrayList<Candidato> getCandidatos() {
		return candidatos;
	}

	public void setCandidatos(ArrayList<Candidato> candidatos) {
		this.candidatos = candidatos;
	}

	public ArrayList<Solicitud> getSolicitudes() {
		return solicitudes;
	}

	public void setSolicitudes(ArrayList<Solicitud> solicitudes) {
		this.solicitudes = solicitudes;
	}

	public ArrayList<OfertaLaboral> getOfertas() {
		return ofertas;
	}

	public void setOfertas(ArrayList<OfertaLaboral> ofertas) {
		this.ofertas = ofertas;
	}

	public ArrayList<CentroEmpleador> getCentros() {
		return centros;
	}

	public void setCentros(ArrayList<CentroEmpleador> centros) {
		this.centros = centros;
	}

	public ArrayList<VacanteCompletada> getVacantes() {
		return vacantes;
	}

	public void setVacantes(ArrayList<VacanteCompletada> vacantes) {
		this.vacantes = vacantes;
	}

	public ArrayList<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(ArrayList<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	public CatalogoDatos getCatalogos() {
		if (catalogos == null) {
			catalogos = new CatalogoDatos();
		}
		return catalogos;
	}

	public void setCatalogos(CatalogoDatos catalogos) {
		this.catalogos = catalogos;
	}

	public Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public void setUsuarioActual(Usuario usuarioActual) {
		this.usuarioActual = usuarioActual;
	}

	public static BolsaLaboral getInstancia() {
		if(instancia == null) {
			instancia = new BolsaLaboral();
		}
		return instancia;
	}

	public static void setInstancia(BolsaLaboral bolsa) {
		instancia = bolsa;
	}

	/**
	 * Normaliza campos agregados después de la primera versión serializada.
	 * La migración se realiza únicamente en memoria; el archivo de origen no se
	 * reemplaza durante la carga.
	 *
	 * @return cantidad de campos legados normalizados
	 */
	public int migrarDatosDeserializados() {
		if (candidatos == null) {
			candidatos = new ArrayList<Candidato>();
			System.err.println("Compatibilidad: la colección de candidatos ausente fue inicializada vacía.");
		}
		if (solicitudes == null) {
			solicitudes = new ArrayList<Solicitud>();
			System.err.println("Compatibilidad: la colección de solicitudes ausente fue inicializada vacía.");
		}
		if (vacantes == null) {
			vacantes = new ArrayList<VacanteCompletada>();
			System.err.println("Compatibilidad: la colección de vacantes completadas ausente fue inicializada vacía.");
		}
		if (ofertas == null) {
			ofertas = new ArrayList<OfertaLaboral>();
			System.err.println("Compatibilidad: la colección de ofertas ausente fue inicializada vacía.");
		}
		if (centros == null) {
			centros = new ArrayList<CentroEmpleador>();
			System.err.println("Compatibilidad: la colección de centros ausente fue inicializada vacía.");
		}
		if (usuarios == null) {
			usuarios = new ArrayList<Usuario>();
			System.err.println("Compatibilidad: la colección de usuarios ausente fue inicializada vacía.");
		}

		int datosMigrados = 0;
		if (catalogos == null) {
			catalogos = new CatalogoDatos();
			datosMigrados++;
		} else {
			datosMigrados += catalogos.migrarDatosDeserializados();
		}
		int candidatosNulos = 0;
		int solicitudesNulas = 0;
		int solicitudesIncompletas = 0;
		int vacantesNulas = 0;
		int vacantesIncompletas = 0;
		int ofertasIncompletas = 0;
		Set<Solicitud> solicitudesVisitadas = Collections.newSetFromMap(
				new IdentityHashMap<Solicitud, Boolean>());

		for (Solicitud solicitud : solicitudes) {
			if (solicitud == null) {
				solicitudesNulas++;
				continue;
			}
			if (solicitudesVisitadas.add(solicitud)) {
				if (solicitud.getSolicitante() == null
						|| solicitud.getOfertaSolicitada() == null) {
					solicitudesIncompletas++;
				}
				if (normalizarEstadoSolicitud(solicitud)) {
					datosMigrados++;
				}
			}
		}

		for (Candidato candidato : candidatos) {
			if (candidato == null) {
				candidatosNulos++;
				continue;
			}
			datosMigrados += candidato.migrarDatosDeserializados();
			if (candidato instanceof Universitario) {
				datosMigrados += ((Universitario) candidato)
						.migrarUniversidadDeserializada(catalogos);
			}
			for (Solicitud solicitud : candidato.getMisSolicitudes()) {
				if (solicitud == null) {
					solicitudesNulas++;
					continue;
				}
				if (solicitudesVisitadas.add(solicitud)) {
					if (solicitud.getSolicitante() == null
							|| solicitud.getOfertaSolicitada() == null) {
						solicitudesIncompletas++;
					}
					if (normalizarEstadoSolicitud(solicitud)) {
						datosMigrados++;
					}
				}
			}

			String estadoAnterior = candidato.getEstado();
			candidato.actualizarEstadoLaboral();
			if (!Objects.equals(estadoAnterior, candidato.getEstado())) {
				datosMigrados++;
			}
		}

		for (Usuario usuario : usuarios) {
			if (usuario == null) {
				System.err.println("Advertencia: se ignoró una entrada nula en la colección de usuarios.");
				continue;
			}
			datosMigrados += usuario.migrarDatosDeserializados();
		}

		for (CentroEmpleador centro : centros) {
			if (centro == null) {
				System.err.println("Advertencia: se ignoró una entrada nula en la colección de centros.");
				continue;
			}
			datosMigrados += centro.migrarDatosDeserializados();
		}

		for (VacanteCompletada vacante : vacantes) {
			if (vacante == null) {
				vacantesNulas++;
			} else if (vacante.getSolicitudAceptada() == null
					|| vacante.getOfertaOcupada() == null) {
				vacantesIncompletas++;
			}
		}

		for (OfertaLaboral oferta : ofertas) {
			if (oferta == null) {
				System.err.println("Advertencia: se ignoró una entrada nula en la colección de ofertas.");
				continue;
			}
			if (oferta.getOfertador() == null) {
				ofertasIncompletas++;
			}
			datosMigrados += oferta.migrarDatosDeserializados(contarVacantesOcupadas(oferta));
		}

		if (datosMigrados > 0) {
			System.err.println("Compatibilidad: se normalizaron " + datosMigrados
					+ " campo(s) del archivo deserializado en memoria.");
		}
		if (candidatosNulos > 0) {
			System.err.println("Advertencia: se ignoraron " + candidatosNulos
					+ " entrada(s) nula(s) en la colección de candidatos deserializada.");
		}
		if (solicitudesNulas > 0) {
			System.err.println("Advertencia: se ignoraron " + solicitudesNulas
					+ " referencia(s) nula(s) a solicitudes deserializadas.");
		}
		if (solicitudesIncompletas > 0) {
			System.err.println("Advertencia: se ignoraron " + solicitudesIncompletas
					+ " solicitud(es) sin candidato u oferta asociada.");
		}
		if (vacantesNulas > 0) {
			System.err.println("Advertencia: se ignoraron " + vacantesNulas
					+ " entrada(s) nula(s) de vacantes completadas deserializadas.");
		}
		if (vacantesIncompletas > 0) {
			System.err.println("Advertencia: se ignoraron " + vacantesIncompletas
					+ " vacante(s) completada(s) sin solicitud u oferta asociada.");
		}
		if (ofertasIncompletas > 0) {
			System.err.println("Advertencia: se ignoraron " + ofertasIncompletas
					+ " oferta(s) sin centro empleador asociado.");
		}
		return datosMigrados;
	}

	private boolean normalizarEstadoSolicitud(Solicitud solicitud) {
		String estado = solicitud.getEstado();
		if (estado == null) {
			return false;
		}
		String limpio = estado.trim();
		String canonico = null;
		if ("Aprovada".equalsIgnoreCase(limpio)
				|| Solicitud.ESTADO_APROBADA.equalsIgnoreCase(limpio)) {
			canonico = Solicitud.ESTADO_APROBADA;
		} else if (Solicitud.ESTADO_ENVIADA.equalsIgnoreCase(limpio)) {
			canonico = Solicitud.ESTADO_ENVIADA;
		} else if (Solicitud.ESTADO_RECHAZADA.equalsIgnoreCase(limpio)) {
			canonico = Solicitud.ESTADO_RECHAZADA;
		}
		if (canonico != null && !canonico.equals(estado)) {
			solicitud.setEstado(canonico);
			return true;
		}
		return false;
	}

	public void registrarCentroTrabajo(CentroEmpleador nuevoCentro) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_CENTROS);
		if (nuevoCentro == null) {
			throw new IllegalArgumentException("El centro empleador es obligatorio.");
		}
		nuevoCentro.setRnc(prepararRnc(null, nuevoCentro.getRnc()));
		centros.add(nuevoCentro);
		genCodigoCentro++;
	}

	public int buscarIndiceCentroByCodigo(String codigo) {
		int indice = 0;
		boolean encontrado = false;

		while(encontrado == false && indice < centros.size()) {
			CentroEmpleador centro = centros.get(indice);
			if(centro != null && centro.getCodigo() != null && codigo != null
					&& centro.getCodigo().equalsIgnoreCase(codigo)) {
				encontrado = true;
			}
			else {
				indice++;
			}
		}

		return encontrado ? indice : -1;
	}

	public boolean modificarCentroTrabajo(CentroEmpleador centroModificar) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_CENTROS);
		if (centroModificar == null) {
			throw new IllegalArgumentException("El centro empleador es obligatorio.");
		}
		int indice = buscarIndiceCentroByCodigo(centroModificar.getCodigo());
		if(indice != -1) {
			CentroEmpleador existente = centros.get(indice);
			centroModificar.setRnc(prepararRnc(existente, centroModificar.getRnc()));
			centros.set(indice,centroModificar);
			return true;
		}
		return false;
	}

	public void eliminarCentroTrabajo(CentroEmpleador centroEliminar) throws NotRemovableException{
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_CENTROS);
		if(centroEliminable(centroEliminar)) {
			centros.remove(centroEliminar);
		}
		else {
			throw new NotRemovableException("El centro de trabajo no puede ser eliminado ya que posee ofertas existentes.");
		}
	}

	public void registrarCandidato(Candidato nuevoCandidato) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_CANDIDATOS);
		if (nuevoCandidato == null) {
			throw new IllegalArgumentException("El candidato es obligatorio.");
		}
		nuevoCandidato.setIdentificacion(prepararCedula(null, nuevoCandidato.getIdentificacion()));
		nuevoCandidato.setEstado(Candidato.ESTADO_DESEMPLEADO);
		candidatos.add(nuevoCandidato);
		genCodigoCandidato++;
	}

	public boolean modificarCandidato(Candidato candidatoModificar) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_CANDIDATOS);
		if (candidatoModificar == null) {
			throw new IllegalArgumentException("El candidato es obligatorio.");
		}
		int indice = candidatos.indexOf(candidatoModificar);
		if (indice < 0 && candidatoModificar.getCodigo() != null) {
			Candidato actual = buscarCandidatoByCodigo(candidatoModificar.getCodigo());
			indice = candidatos.indexOf(actual);
		}
		if (indice < 0) {
			return false;
		}
		Candidato existente = candidatos.get(indice);
		candidatoModificar.setIdentificacion(
				prepararCedula(existente, candidatoModificar.getIdentificacion()));
		candidatoModificar.actualizarEstadoLaboral();
		candidatos.set(indice, candidatoModificar);
		return true;
	}

	public void eliminarCandidato(Candidato candidatoEliminar) throws NotRemovableException{
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_CANDIDATOS);
		if(candidatoEliminable(candidatoEliminar)) {
			candidatos.remove(candidatoEliminar);
		}
		else {
			throw new NotRemovableException("El candidato no puede ser eliminado ya que esta vinculado con una solicitud.");
		}
	}

	public Candidato buscarCandidatoByCodigo(String codigo) {
		Candidato encontrado = null;
		int indice = 0;
		while(encontrado == null && indice < candidatos.size()) {
			Candidato candidato = candidatos.get(indice);
			if(candidato != null && Objects.equals(candidato.getCodigo(), codigo)) {
				encontrado = candidato;
			}
			indice++;
		}
		return encontrado;
	}

	public CentroEmpleador buscarCentroByCodigo(String codigo) {
		CentroEmpleador encontrado = null;
		int indice = 0;
		while(encontrado == null && indice < centros.size()) {
			CentroEmpleador centro = centros.get(indice);
			if(centro != null && Objects.equals(centro.getCodigo(), codigo)) {
				encontrado = centro;
			}
			indice++;
		}
		return encontrado;
	}

	public ArrayList<ResultadoMatcheo> obtenerCandidatosOrdenadosParaOferta(OfertaLaboral oferta) {
		ArrayList<ResultadoMatcheo> ordenados = new ArrayList<ResultadoMatcheo>();
		for (Candidato candidato : candidatos) {
			if (candidato != null
					&& Candidato.ESTADO_DESEMPLEADO.equals(candidato.getEstado())) {
				int puntaje = calcularPuntaje(candidato, oferta);
				if (puntaje >= oferta.getPorcentajeMinimo()) {
					String condicion = obtenerCondicion(
							puntaje, oferta.getPorcentajeMinimo());
					ordenados.add(new ResultadoMatcheo(
							oferta, candidato, puntaje, condicion));
				}
			}
		}
		Comparator<ResultadoMatcheo> comparador =
				(a, b) -> b.getPorcentaje() - a.getPorcentaje();
		ordenados.sort(comparador);
		return ordenados;
	}


	private int calcularPuntaje(Candidato candidato, OfertaLaboral oferta) {
		int puntaje = 0;

		if (candidato.getModalidad().equalsIgnoreCase(oferta.getModalidad())) {
			puntaje += 10;
		}

		if (candidato.getJornada().equalsIgnoreCase(oferta.getJornada())) {
			puntaje += 10;
		}

		if (candidato.getAreaDeInteres().equalsIgnoreCase(oferta.getArea())) {
			puntaje += 20;
		}

		if (candidato.getProvincia().equalsIgnoreCase(oferta.getOfertador().getProvincia())) {
			puntaje += 10;
		} else if (candidato.isDisposicionMudarse() || oferta.isOfreceReubicacion()) {
			puntaje += 5;
		}

		if (candidato.getAspiracionSalarial() <= oferta.getSalario()) {
			puntaje += 10;
		} else {
			float exceso = candidato.getAspiracionSalarial() - oferta.getSalario();
			float porcentajeExceso = exceso / oferta.getSalario();

			if (porcentajeExceso <= 0.35f) {
				puntaje += Math.round(10 * (1 - porcentajeExceso / 0.35f));
			}
		}

		int idiomasRequeridos = oferta.getCantIdiomas();
		int idiomasPuntos = 0;
		for (String idioma : oferta.getIdiomasRequeridas()) {
			if (candidato.getIdiomas().contains(idioma)) {
				idiomasPuntos++;
			}
		}

		puntaje += Math.min(10, (idiomasPuntos*10)/Math.max(1, idiomasRequeridos));

		TipoCandidato tipoRequerido = oferta.getTipoCandidatoRequerido();
		if (candidato.getTipoCandidato() == TipoCandidato.UNIVERSITARIO
				&& tipoRequerido == TipoCandidato.UNIVERSITARIO) {
			Universitario u = (Universitario) candidato;
			puntaje += 5;
			if(!oferta.getRequisitos().isEmpty()
					&& Objects.equals(u.getCarrera(), oferta.getRequisitos().get(0))) {
				puntaje += 15;
			}
		} else if (candidato.getTipoCandidato() == TipoCandidato.TECNICO
				&& tipoRequerido == TipoCandidato.TECNICO) {
			TecnicoSuperior t = (TecnicoSuperior) candidato;
			puntaje += 5;
			if(!oferta.getRequisitos().isEmpty()
					&& Objects.equals(t.getAreaTecnica(), oferta.getRequisitos().get(0))) {
				puntaje += 10;
			}
			if (t.getAniosExperiencia() >= oferta.getExperienciaMinima()) {
				puntaje += 5;
			}
		} else if (candidato.getTipoCandidato() == TipoCandidato.OBRERO
				&& tipoRequerido == TipoCandidato.OBRERO) {
			Obrero o = (Obrero) candidato;
			puntaje += 10;
			int habilidadPuntos = 0;
			int habilidadesRequeridas = oferta.getCantRequisitos();
			for (String habilidad : oferta.getRequisitos()) {
				if (o.getHabilidades().contains(habilidad)) {
					habilidadPuntos++;
				}
			}
			puntaje+= Math.min(10, (habilidadPuntos*10)/Math.max(1, habilidadesRequeridas));
		}

		if (oferta.isobligatorioLicencia()) {
			if (candidato.isLicenciaConducir()) {
				puntaje += 5;
			} else {
				puntaje -= 20;
			}
		} else if (candidato.isLicenciaConducir()) {
			puntaje += 2;
		}

		if(oferta.isObligatorioMayorDeEdad()) {
			if(candidato.getEdad() >= 18) {
				puntaje += 5;
			} else {
				puntaje -= 25;
			}
		} else {
			puntaje += 5;
		}

		return Math.max(0, puntaje);
	}

	public void eliminarOfertaTrabajo(OfertaLaboral seleccionado) throws NotRemovableException{
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_OFERTAS);
		if(ofertaEliminable(seleccionado)) {
			seleccionado.getOfertador().getOfertasLaborales().remove(seleccionado);
			ofertas.remove(seleccionado);
		}
		else {
			throw new NotRemovableException("La oferta no es eliminable ya que esta vinculada con una solicitud.");
		}
	}

	public OfertaLaboral buscarOfertaByCodigo(String codigo) {
		OfertaLaboral encontrado = null;
		int indice = 0;
		while(encontrado == null && indice < ofertas.size()) {
			OfertaLaboral oferta = ofertas.get(indice);
			if(oferta != null && Objects.equals(oferta.getCodigo(), codigo)) {
				encontrado = oferta;
			}
			indice++;
		}
		return encontrado;
	}

	public int buscarIndiceOfertaByCodigo(String codigo) {
		int indice = 0;
		boolean encontrado = false;

		while(encontrado == false && indice < ofertas.size()) {
			OfertaLaboral oferta = ofertas.get(indice);
			if(oferta != null && oferta.getCodigo() != null && codigo != null
					&& oferta.getCodigo().equalsIgnoreCase(codigo)) {
				encontrado = true;
			}
			else {
				indice++;
			}
		}

		return encontrado ? indice : -1;
	}

	public void registrarOfertaLaboral(OfertaLaboral nuevaOferta) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_OFERTAS);
		if (nuevaOferta == null || nuevaOferta.getOfertador() == null) {
			throw new IllegalArgumentException("La oferta y su centro empleador son obligatorios.");
		}
		nuevaOferta.sincronizarVacantesOcupadas(0);
		ofertas.add(nuevaOferta);
		nuevaOferta.getOfertador().getOfertasLaborales().add(nuevaOferta);
		genCodigoOferta++;
	}

	public boolean modificarOfertaLaboral(OfertaLaboral ofertaModificar) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.GESTIONAR_OFERTAS);
		if (ofertaModificar == null) {
			throw new IllegalArgumentException("La oferta es obligatoria.");
		}
		int indice = buscarIndiceOfertaByCodigo(ofertaModificar.getCodigo());
		if(indice != -1) {
			ofertaModificar.sincronizarVacantesOcupadas(contarVacantesOcupadas(ofertaModificar));
			ofertas.set(indice,ofertaModificar);
			return true;
		}
		return false;
	}

	public boolean ofertaVinculada(OfertaLaboral oferta) {
		if (oferta == null) {
			return false;
		}
		boolean aux = false;
		for(Solicitud solicitud : solicitudes) {
			if(solicitud != null && solicitud.getOfertaSolicitada() != null
					&& Objects.equals(solicitud.getOfertaSolicitada().getCodigo(), oferta.getCodigo())) {
				aux = true;
			}
		}
		return aux;
	}

	public void regVacanteCompletada(Solicitud solicitudContratada) {
		contratarCandidato(solicitudContratada);
	}

	/** Alta interna usada únicamente durante la creación inicial del archivo. */
	public void regUsuario(Usuario user) {
		if (user == null) {
			throw new IllegalArgumentException("El usuario es obligatorio.");
		}
		user.migrarDatosDeserializados();
		usuarios.add(user);
	}

	public Usuario login(String nombre, String clave) {
		if (nombre == null || clave == null) {
			return null;
		}
		for(Usuario user : usuarios) {
			if(user != null && user.match(nombre, clave)) {
				return user;
			}
		}
		return null;
	}

	public boolean centroEliminable(CentroEmpleador centro) {
		if(centro.getOfertasLaborales().size() != 0) {
			return false;
		}
		return true;
	}

	public boolean candidatoEliminable(Candidato candidato) {
		if(candidato.getMisSolicitudes().size() != 0) {
			return false;
		}
		return true;
	}

	private boolean ofertaEliminable(OfertaLaboral seleccionado) {
		boolean aux = true;
		for(Solicitud sol : solicitudes) {
			if(sol != null && Objects.equals(sol.getOfertaSolicitada(), seleccionado)) {
				aux = false;
			}
		}
		return aux;
	}

	public ArrayList<OfertaLaboral> ofertasDisponibles(){
		ArrayList<OfertaLaboral> ofertasDisponibles = new ArrayList<>();
		for(OfertaLaboral ofr: ofertas) {
			if(ofr != null) {
				ofr.sincronizarVacantesOcupadas(contarVacantesOcupadas(ofr));
			}
			if(ofr != null && OfertaLaboral.ESTADO_ACTIVA.equals(ofr.getEstado())
					&& ofr.getVacantesDisponibles() > 0) {
				ofertasDisponibles.add(ofr);
			}
		}
		return ofertasDisponibles;
	}

	public ArrayList<ResultadoMatcheo> procesamientoAvanzando(){
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.USAR_PROCESAMIENTO_AVANZADO);
		ArrayList<ResultadoMatcheo> resultados = new ArrayList<>();
		for(OfertaLaboral ofr : ofertas) {
			if(evaluarProcesamiento(ofr).isPermitido()) {
				resultados.addAll(obtenerCandidatosOrdenadosParaOferta(ofr));
			}
		}

		return resultados;
	}

	public String obtenerCondicion(int puntaje, int limitePuntaje) {
		double noRecomendadoMax = Math.max(Math.min(limitePuntaje * 1.3, 65), 50);
		double aceptableMax = Math.max(Math.min(limitePuntaje * 1.6, 85), 65);

		if (puntaje < noRecomendadoMax) {
			return "No recomendado";
		} else if (puntaje < aceptableMax) {
			return "Aceptable";
		}
		return "Recomendado";
	}

	public ResultadoMatcheo buscarResultado(ArrayList<ResultadoMatcheo> resultados, String codigoOferta, String codigoCandidato) {
		ResultadoMatcheo resultado = null;

		int indice = 0;
		while(indice < resultados.size() && resultado == null) {
			if(resultados.get(indice).getOferta().getCodigo().equals(codigoOferta) && resultados.get(indice).getSolicitante().getCodigo().equals(codigoCandidato)) {
				resultado = resultados.get(indice);
			}
			else {
				indice++;
			}
		}
		return resultado;
	}

	public boolean vincularOferta(ResultadoMatcheo resMatchSelec) {
		AutorizacionService.exigirPermiso(
				usuarioActual, Permiso.USAR_PROCESAMIENTO_AVANZADO);
		DecisionProcesamiento decision = evaluarVinculacion(resMatchSelec);
		if (!decision.isPermitido()) {
			return false;
		}
		Solicitud sol = new Solicitud(null, LocalDate.now(),
				Solicitud.ESTADO_ENVIADA, resMatchSelec.getSolicitante(), resMatchSelec.getOferta());
		if (!verificarSolicitud(sol)) {
			return false;
		}
		new SolicitudDAO().agregar(sol);
		solicitudes.add(sol);
		resMatchSelec.getSolicitante().addSolicitud(sol);
		resMatchSelec.getSolicitante().actualizarEstadoLaboral();
		return true;
	}

	public boolean verificarSolicitud(Solicitud solicitud) {
		if (solicitud == null) {
			return false;
		}
		for(Solicitud sol : solicitudes) {
			if (sol == null) {
				continue;
			}
			if (Objects.equals(sol.getSolicitante(), solicitud.getSolicitante())
					&& Objects.equals(sol.getOfertaSolicitada(), solicitud.getOfertaSolicitada())) {
				return false;
			}
		}
		return true;
	}

	public boolean matchSolicitud(Solicitud s1, Solicitud s2) {
		if (s1 == null || s2 == null) {
			return false;
		}
		return Objects.equals(s1.getFechaSolicitud(), s2.getFechaSolicitud())
				&& Objects.equals(s1.getEstado(), s2.getEstado())
				&& Objects.equals(s1.getSolicitante(), s2.getSolicitante())
				&& Objects.equals(s1.getOfertaSolicitada(), s2.getOfertaSolicitada());
	}

	public boolean contratarCandidato(Solicitud solicitud) {
		AutorizacionService.exigirPermiso(
				usuarioActual,
				Permiso.PROCESAR_SOLICITUDES
		);

		if (!puedeContratarCandidato(solicitud)) {
			return false;
		}

		OfertaLaboral oferta = solicitud.getOfertaSolicitada();
		Candidato candidato = solicitud.getSolicitante();
		LocalDate fechaContratacion = LocalDate.now();

		VacanteCompletada vacante = new VacanteCompletada(
				null,
				solicitud,
				oferta,
				fechaContratacion
		);

		/*
		 * Primero se guarda todo en SQL. Si algo falla, se ejecuta
		 * rollback y ningún objeto en memoria ha sido modificado.
		 */
		new ContratacionDAO().contratarAtomico(
				solicitud,
				vacante
		);

		/*
		 * SQL ya hizo commit. Ahora se sincroniza la memoria.
		 */
		solicitud.setEstado(Solicitud.ESTADO_APROBADA);

		if (candidato.getMisSolicitudes() == null
				|| !candidato.getMisSolicitudes().contains(solicitud)) {
			candidato.addSolicitud(solicitud);
		}

		candidato.cambiarEstadoSolicitudesAEmpleado();

		if (vacantes == null) {
			vacantes = new ArrayList<VacanteCompletada>();
		}

		vacantes.add(vacante);

		oferta.sincronizarVacantesOcupadas(
				contarVacantesOcupadas(oferta)
		);

		return true;
	}

	public void rechazarCandidato(Solicitud solicitud) {
		AutorizacionService.exigirPermiso(usuarioActual, Permiso.PROCESAR_SOLICITUDES);
		if (!esProcesable(solicitud)) {
			return;
		}
		solicitud.setEstado(Solicitud.ESTADO_RECHAZADA);
		new SolicitudDAO().actualizarEstado(solicitud, LocalDate.now());
		Candidato candidato = solicitud.getSolicitante();
		if (candidato != null) {
			if (candidato.getMisSolicitudes() == null || !candidato.getMisSolicitudes().contains(solicitud)) {
				candidato.addSolicitud(solicitud);
			}
			candidato.cambiarEstadoSolicitudesADesempleado();
		}
	}

	public Solicitud buscarSolicitudByCodigo(String codigo) {
		Solicitud encontrado = null;
		int indice = 0;
		while(encontrado == null && indice < solicitudes.size()) {
			Solicitud solicitud = solicitudes.get(indice);
			if(solicitud != null && Objects.equals(solicitud.getCodigo(), codigo)) {
				encontrado = solicitud;
			}
			indice++;
		}
		return encontrado;
	}

	public boolean esProcesable(Solicitud seleccionado) {
		if (seleccionado == null || seleccionado.getEstado() == null) {
			return false;
		}
		return Solicitud.ESTADO_ENVIADA.equals(seleccionado.getEstado().trim());
	}

	public boolean puedeContratarCandidato(Solicitud solicitud) {
		if (!AutorizacionService.tienePermiso(usuarioActual, Permiso.PROCESAR_SOLICITUDES)) {
			return false;
		}
		if (!esProcesable(solicitud) || solicitud.getOfertaSolicitada() == null
				|| solicitud.getSolicitante() == null) {
			return false;
		}
		OfertaLaboral oferta = solicitud.getOfertaSolicitada();
		oferta.sincronizarVacantesOcupadas(contarVacantesOcupadas(oferta));
		if (!OfertaLaboral.ESTADO_ACTIVA.equals(oferta.getEstado())
				|| oferta.getVacantesDisponibles() <= 0) {
			return false;
		}
		if (vacantes == null) {
			return true;
		}
		for (VacanteCompletada vacante : vacantes) {
			if (vacante == null || vacante.getSolicitudAceptada() == null) {
				continue;
			}
			Solicitud aceptada = vacante.getSolicitudAceptada();
			if (aceptada == solicitud || (solicitud.getCodigo() != null
					&& solicitud.getCodigo().equals(aceptada.getCodigo()))) {
				return false;
			}
		}
		return true;
	}

	public DecisionProcesamiento evaluarProcesamiento(OfertaLaboral oferta) {
		if (oferta == null) {
			return DecisionProcesamiento.rechazar("Debe seleccionar una oferta.");
		}
		if (usuarioActual == null) {
			return DecisionProcesamiento.rechazar(
					"No hay un usuario autenticado para procesar la oferta.");
		}
		if (!usuarioActual.isActivo()) {
			return DecisionProcesamiento.rechazar(
					"El usuario actual está inactivo.");
		}
		if (!AutorizacionService.tienePermiso(
				usuarioActual, Permiso.USAR_PROCESAMIENTO_AVANZADO)) {
			return DecisionProcesamiento.rechazar(
					"No tiene permiso para utilizar el procesamiento avanzado.");
		}
		oferta.sincronizarVacantesOcupadas(contarVacantesOcupadas(oferta));
		if (oferta.getVacantesDisponibles() <= 0) {
			return DecisionProcesamiento.rechazar(
					"La oferta no tiene vacantes disponibles.");
		}
		if (!OfertaLaboral.ESTADO_ACTIVA.equals(oferta.getEstado())) {
			return DecisionProcesamiento.rechazar(
					"La oferta no está activa; su estado actual es "
							+ oferta.getEstado() + ".");
		}
		ArrayList<ResultadoMatcheo> candidatosElegibles =
				obtenerCandidatosOrdenadosParaOferta(oferta);
		if (candidatosElegibles.isEmpty()) {
			return DecisionProcesamiento.rechazar(
					"No existe ningún candidato elegible que alcance "
							+ "el porcentaje mínimo de la oferta.");
		}
		for (ResultadoMatcheo resultado : candidatosElegibles) {
			if (resultado != null && resultado.getSolicitante() != null
					&& !existeSolicitud(resultado.getSolicitante(), oferta)) {
				return DecisionProcesamiento.permitir();
			}
		}
		return DecisionProcesamiento.rechazar(
				"Todos los candidatos elegibles ya tienen una solicitud "
						+ "para esta oferta.");
	}

	public DecisionProcesamiento evaluarVinculacion(ResultadoMatcheo resultado) {
		if (resultado == null || resultado.getOferta() == null
				|| resultado.getSolicitante() == null) {
			return DecisionProcesamiento.rechazar(
					"Debe seleccionar un candidato elegible.");
		}
		DecisionProcesamiento ofertaDecision = evaluarProcesamiento(resultado.getOferta());
		if (!ofertaDecision.isPermitido()) {
			return ofertaDecision;
		}
		Candidato candidato = resultado.getSolicitante();
		if (!candidatos.contains(candidato)
				|| !Candidato.ESTADO_DESEMPLEADO.equals(candidato.getEstado())) {
			return DecisionProcesamiento.rechazar(
					"El candidato ya no está elegible para una nueva solicitud.");
		}
		if (resultado.getPorcentaje() < resultado.getOferta().getPorcentajeMinimo()) {
			return DecisionProcesamiento.rechazar(
					"El candidato no alcanza el porcentaje mínimo de la oferta.");
		}
		if (existeSolicitud(candidato, resultado.getOferta())) {
			return DecisionProcesamiento.rechazar(
					"Ya existe una solicitud para este candidato y esta oferta.");
		}
		return DecisionProcesamiento.permitir();
	}

	public String prepararCedula(Candidato existente, String valor) {
		ResultadoDocumento resultado = CedulaValidator.validar(valor);
		if (!resultado.esValido()) {
			if (existente != null && Objects.equals(existente.getIdentificacion(), valor)) {
				return valor;
			}
			throw new IllegalArgumentException(resultado.getMensaje());
		}
		if (existeCedulaNormalizada(resultado.getNormalizado(), existente)) {
			throw new IllegalArgumentException(
					"Ya existe un candidato con esa cédula.");
		}
		return resultado.getNormalizado();
	}

	public String prepararRnc(CentroEmpleador existente, String valor) {
		ResultadoDocumento resultado = RncValidator.validar(valor);
		if (!resultado.esValido()) {
			if (existente != null && Objects.equals(existente.getRnc(), valor)) {
				return valor;
			}
			throw new IllegalArgumentException(resultado.getMensaje());
		}
		if (existeRncNormalizado(resultado.getNormalizado(), existente)) {
			throw new IllegalArgumentException("Ya existe un centro empleador con ese RNC.");
		}
		return resultado.getNormalizado();
	}

	public boolean existeCedulaNormalizada(String normalizada, Candidato excluir) {
		for (Candidato candidato : candidatos) {
			if (candidato == null || candidato == excluir) {
				continue;
			}
			try {
				if (CedulaValidator.normalizar(candidato.getIdentificacion()).equals(normalizada)) {
					return true;
				}
			} catch (IllegalArgumentException exception) {
				// Un valor legado no normalizable no puede coincidir con uno válido.
			}
		}
		return false;
	}

	public boolean existeRncNormalizado(String normalizado, CentroEmpleador excluir) {
		for (CentroEmpleador centro : centros) {
			if (centro == null || centro == excluir) {
				continue;
			}
			try {
				if (RncValidator.normalizar(centro.getRnc()).equals(normalizado)) {
					return true;
				}
			} catch (IllegalArgumentException exception) {
				// Un valor legado no normalizable no puede coincidir con uno válido.
			}
		}
		return false;
	}

	public int contarVacantesOcupadas(OfertaLaboral oferta) {
		if (oferta == null || vacantes == null) {
			return 0;
		}
		Set<String> solicitudesContadas = new HashSet<String>();
		int ocupadas = 0;
		for (VacanteCompletada vacante : vacantes) {
			if (vacante == null) {
				continue;
			}
			Solicitud solicitud = vacante.getSolicitudAceptada();
			OfertaLaboral asociada = vacante.getOfertaOcupada();
			if (solicitud == null || asociada == null
					|| !mismaOferta(asociada, oferta)
					|| !Solicitud.ESTADO_APROBADA.equals(solicitud.getEstado())) {
				continue;
			}
			String clave = solicitud.getCodigo() == null
					? "identidad:" + System.identityHashCode(solicitud)
					: "codigo:" + solicitud.getCodigo();
			if (solicitudesContadas.add(clave)) {
				ocupadas++;
			}
		}
		return ocupadas;
	}

	private boolean existeSolicitud(Candidato candidato, OfertaLaboral oferta) {
		for (Solicitud solicitud : solicitudes) {
			if (solicitud != null && Objects.equals(candidato, solicitud.getSolicitante())
					&& mismaOferta(oferta, solicitud.getOfertaSolicitada())) {
				return true;
			}
		}
		return false;
	}

	private boolean mismaOferta(OfertaLaboral izquierda, OfertaLaboral derecha) {
		if (izquierda == derecha) {
			return true;
		}
		return izquierda != null && derecha != null && izquierda.getCodigo() != null
				&& izquierda.getCodigo().equals(derecha.getCodigo());
	}

	public ArrayList<Solicitud> obtenerSolicitudesVinculadas(OfertaLaboral oferta){
		ArrayList<Solicitud> solicitudesV = new ArrayList<Solicitud>();
		if (oferta == null) {
			return solicitudesV;
		}
		for(Solicitud sol : solicitudes) {
			if(sol != null && sol.getOfertaSolicitada() != null
					&& Objects.equals(sol.getOfertaSolicitada().getCodigo(), oferta.getCodigo())) {
				solicitudesV.add(sol);
			}
		}

		return solicitudesV;
	}

	public int calcularTasaCovertura() {
		long puestosOcupados = 0;
		long puestosTotales = 0;
		if (ofertas != null) {
			for (OfertaLaboral oferta : ofertas) {
				if (oferta != null) {
					int ocupadas = contarVacantesOcupadas(oferta);
					oferta.sincronizarVacantesOcupadas(ocupadas);
					puestosOcupados += ocupadas;
					puestosTotales += Math.max(0, oferta.getVacantesTotales());
				}
			}
		}
		if (puestosTotales == 0) {
			return 0;
		}
		int cobertura = Math.round(puestosOcupados * 100.0f / puestosTotales);
		return Math.max(0, Math.min(100, cobertura));
	}

	public int contarVacantesOcupadasTotales() {
		long ocupadas = 0;
		if (ofertas != null) {
			for (OfertaLaboral oferta : ofertas) {
				if (oferta != null) {
					ocupadas += contarVacantesOcupadas(oferta);
				}
			}
		}
		return ocupadas > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ocupadas;
	}


	public int obtenerOfertasVacias() {
		int cantidad = 0;
		for(OfertaLaboral ofr: ofertas) {
			if (ofr == null) {
				continue;
			}
			boolean encontrado = false;

			int indice = 0;
			while(indice < solicitudes.size() && encontrado == false) {
				Solicitud solicitud = solicitudes.get(indice);
				if(solicitud != null && Objects.equals(solicitud.getOfertaSolicitada(), ofr)) {
					encontrado = true;
				}
				else {
					indice++;
				}
			}

			if(encontrado == false) {
				cantidad++;
			}
		}

		return cantidad;
	}
}