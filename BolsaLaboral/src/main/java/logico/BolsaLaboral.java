package logico;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import exception.NotRemovableException;

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
	public static BolsaLaboral instancia;
	private Usuario usuarioActual;

	private BolsaLaboral() {
		candidatos = new ArrayList<Candidato>();
		solicitudes = new ArrayList<Solicitud>();
		ofertas = new ArrayList<OfertaLaboral>();
		centros = new ArrayList<CentroEmpleador>();
		vacantes = new ArrayList<VacanteCompletada>();
		usuarios = new ArrayList<Usuario>();
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

		int datosMigrados = 0;
		int candidatosNulos = 0;
		int solicitudesNulas = 0;
		Set<Solicitud> solicitudesVisitadas = Collections.newSetFromMap(
				new IdentityHashMap<Solicitud, Boolean>());

		for (Solicitud solicitud : solicitudes) {
			if (solicitud == null) {
				solicitudesNulas++;
				continue;
			}
			if (solicitudesVisitadas.add(solicitud) && normalizarEstadoSolicitud(solicitud)) {
				datosMigrados++;
			}
		}

		for (Candidato candidato : candidatos) {
			if (candidato == null) {
				candidatosNulos++;
				continue;
			}
			if (candidato.getMisSolicitudes() == null) {
				candidato.setMisSolicitudes(new ArrayList<Solicitud>());
				datosMigrados++;
			}
			for (Solicitud solicitud : candidato.getMisSolicitudes()) {
				if (solicitud == null) {
					solicitudesNulas++;
					continue;
				}
				if (solicitudesVisitadas.add(solicitud) && normalizarEstadoSolicitud(solicitud)) {
					datosMigrados++;
				}
			}

			String estadoAnterior = candidato.getEstado();
			candidato.actualizarEstadoLaboral();
			if (!Objects.equals(estadoAnterior, candidato.getEstado())) {
				datosMigrados++;
			}
		}

		if (ofertas != null) {
			for (OfertaLaboral oferta : ofertas) {
				if (oferta != null && oferta.getVacantes() < 0) {
					oferta.setVacantes(0);
					datosMigrados++;
				}
			}
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
		centros.add(nuevoCentro);
		genCodigoCentro++;
	}
	
	public int buscarIndiceCentroByCodigo(String codigo) {
		int indice = 0;
		boolean encontrado = false;
		
		while(encontrado == false && indice < centros.size()) {
			if(centros.get(indice).getCodigo().equalsIgnoreCase(codigo)) {
				encontrado = true;
			}
			else {
				indice++;
			}
		}
		
		return encontrado ? indice : -1;
	}
	
	public boolean modificarCentroTrabajo(CentroEmpleador centroModificar) {
		int indice = buscarIndiceCentroByCodigo(centroModificar.getCodigo());
		if(indice != -1) {
			centros.set(indice,centroModificar);
			return true;
		}
		return false;
	}
	
	public void eliminarCentroTrabajo(CentroEmpleador centroEliminar) throws NotRemovableException{
		if(centroEliminable(centroEliminar)) {
			centros.remove(centroEliminar);
		}
		else {
			throw new NotRemovableException("El centro de trabajo no puede ser eliminado ya que posee ofertas existentes.");
		}
	}
	
	public void registrarCandidato(Candidato nuevoCandidato) {
		candidatos.add(nuevoCandidato);
		genCodigoCandidato++;
	}
	
	public void eliminarCandidato(Candidato candidatoEliminar) throws NotRemovableException{
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
			if(candidatos.get(indice).getCodigo().equals(codigo)) {
				encontrado = candidatos.get(indice);
			}
			indice++;
		}
		return encontrado;
	}
	
	public CentroEmpleador buscarCentroByCodigo(String codigo) {
		CentroEmpleador encontrado = null;
		int indice = 0;
		while(encontrado == null && indice < centros.size()) {
			if(centros.get(indice).getCodigo().equals(codigo)) {
				encontrado = centros.get(indice);
			}
			indice++;
		}
		return encontrado;
	}
	
	public ArrayList<ResultadoMatcheo> obtenerCandidatosOrdenadosParaOferta(OfertaLaboral oferta) {
	    ArrayList<ResultadoMatcheo> ordenados = new ArrayList<>();

	    for (Candidato candidato : candidatos) {
	    	if(candidato != null && "Desempleado".equals(candidato.getEstado())) {
		        int puntaje = calcularPuntaje(candidato, oferta);
		        
		        if (puntaje >= oferta.getPorcentajeMinimo()) {
		            String condicion = obtenerCondicion(puntaje, oferta.getPorcentajeMinimo());

		            ResultadoMatcheo resultadoMatcheo = new ResultadoMatcheo(oferta, candidato, puntaje, condicion);

		            ordenados.add(resultadoMatcheo);
		        }
	    	}
	    }
	    
	    Comparator<ResultadoMatcheo> c = (a, b) -> b.getPorcentaje() - a.getPorcentaje();
	    
	    ordenados.sort(c);

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
	    
	    if (candidato instanceof Universitario && oferta.getNivelAcademico().equalsIgnoreCase("Estudiante Universitario")) {
	    	Universitario u = (Universitario) candidato;
	    	puntaje += 5;
	    	if(u.getCarrera().equals(oferta.getRequisitos().get(0))) {
	    		puntaje += 15;
	    	}
	    } else if (candidato instanceof TecnicoSuperior && oferta.getNivelAcademico().equalsIgnoreCase("Estudiante Tecnico")) {
	        TecnicoSuperior t = (TecnicoSuperior) candidato;
	        puntaje += 5;
	        if(t.getAreaTecnica().equals(oferta.getRequisitos().get(0))) {
	        	puntaje += 10;
	        }
	        if (t.getAniosExperiencia() >= oferta.getExperienciaMinima()) {
	            puntaje += 5;
	        }
	    } else if (candidato instanceof Obrero && oferta.getNivelAcademico().equalsIgnoreCase("Obrero")) {
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
			if(ofertas.get(indice).getCodigo().equals(codigo)) {
				encontrado = ofertas.get(indice);
			}
			indice++;
		}
		return encontrado;
	}
	
	public int buscarIndiceOfertaByCodigo(String codigo) {
		int indice = 0;
		boolean encontrado = false;
		
		while(encontrado == false && indice < ofertas.size()) {
			if(ofertas.get(indice).getCodigo().equalsIgnoreCase(codigo)) {
				encontrado = true;
			}
			else {
				indice++;
			}
		}
		
		return encontrado ? indice : -1;
	}
	
	public void registrarOfertaLaboral(OfertaLaboral nuevaOferta) {
		ofertas.add(nuevaOferta);
		nuevaOferta.getOfertador().getOfertasLaborales().add(nuevaOferta);
		genCodigoOferta++;
	}
	
	public boolean modificarOfertaLaboral(OfertaLaboral ofertaModificar) {
		int indice = buscarIndiceOfertaByCodigo(ofertaModificar.getCodigo());
		if(indice != -1) {
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
	
	public void regUsuario(Usuario user) {
		usuarios.add(user);
	}

	public Usuario login(String nombre, String clave) {
		Usuario aux = null;
		for(Usuario user : usuarios) {
			if(user.match(nombre, clave)) {
				aux = user;
			}
		}
		return aux;
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
			if(ofr.getVacantes() > 0) {
				ofertasDisponibles.add(ofr);
			}
		}
		return ofertasDisponibles;
	}
	
	public ArrayList<ResultadoMatcheo> procesamientoAvanzando(){
		ArrayList<ResultadoMatcheo> resultados = new ArrayList<>();
		for(OfertaLaboral ofr : ofertas) {
			if(ofr.getVacantes() > 0) {
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
		boolean aux = false;
		if(resMatchSelec != null && resMatchSelec.getOferta() != null
				&& resMatchSelec.getSolicitante() != null && resMatchSelec.getOferta().getVacantes() > 0) {
			Solicitud sol = new Solicitud("SOL-" + genCodigoSolicitud, LocalDate.now(),
					Solicitud.ESTADO_ENVIADA, resMatchSelec.getSolicitante(), resMatchSelec.getOferta());
			if(verificarSolicitud(sol)) {
				solicitudes.add(sol);
				resMatchSelec.getSolicitante().addSolicitud(sol);
				resMatchSelec.getSolicitante().actualizarEstadoLaboral();
				genCodigoSolicitud++;
				aux = true;
			}
		}
		return aux;
	}
	
	public boolean verificarSolicitud(Solicitud solicitud) {
		if (solicitud == null) {
			return false;
		}
		boolean aux = true;

		for(Solicitud sol : solicitudes) {
			if(matchSolicitud(sol, solicitud)) {
				aux = false;
			}
		}
		
		return aux;
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
		if (!puedeContratarCandidato(solicitud)) {
			return false;
		}

		OfertaLaboral oferta = solicitud.getOfertaSolicitada();
		Candidato candidato = solicitud.getSolicitante();
		solicitud.setEstado(Solicitud.ESTADO_APROBADA);
		oferta.setVacantes(oferta.getVacantes() - 1);
		if (candidato.getMisSolicitudes() == null || !candidato.getMisSolicitudes().contains(solicitud)) {
			candidato.addSolicitud(solicitud);
		}
		candidato.cambiarEstadoSolicitudesAEmpleado();

		if(oferta.getVacantes() == 0) {
			oferta.setEstado(OfertaLaboral.ESTADO_COMPLETADA);
		}

		VacanteCompletada vacante = new VacanteCompletada("VAC-" + genCodigoVacanteCompletada,
				solicitud, oferta, LocalDate.now());
		if (vacantes == null) {
			vacantes = new ArrayList<VacanteCompletada>();
		}
		vacantes.add(vacante);
		genCodigoVacanteCompletada++;
		return true;
	}

	public void rechazarCandidato(Solicitud solicitud) {
		if (!esProcesable(solicitud)) {
			return;
		}
		solicitud.setEstado(Solicitud.ESTADO_RECHAZADA);
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
		if (!esProcesable(solicitud) || solicitud.getOfertaSolicitada() == null
				|| solicitud.getSolicitante() == null || solicitud.getOfertaSolicitada().getVacantes() <= 0) {
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
		if (vacantes != null) {
			for (VacanteCompletada vacante : vacantes) {
				if (vacante != null) {
					puestosOcupados++;
				}
			}
		}

		long puestosDisponibles = 0;
		if (ofertas != null) {
			for (OfertaLaboral oferta : ofertas) {
				if (oferta != null) {
					puestosDisponibles += Math.max(0, oferta.getVacantes());
				}
			}
		}

		long puestosTotales = puestosOcupados + puestosDisponibles;
		if (puestosTotales == 0) {
			return 0;
		}
		int cobertura = Math.round(puestosOcupados * 100.0f / puestosTotales);
		return Math.max(0, Math.min(100, cobertura));
	}
	
	
	public int obtenerOfertasVacias() {
		int cantidad = 0;
		for(OfertaLaboral ofr: ofertas) {
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
