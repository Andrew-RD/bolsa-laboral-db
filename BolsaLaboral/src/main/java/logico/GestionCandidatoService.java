package logico;

import Datos.CandidatoDAO;
import exception.NotRemovableException;

import java.util.ArrayList;

public final class GestionCandidatoService {

    private final BolsaLaboral bolsa;
    private final CandidatoDAO candidatoDAO;

    public GestionCandidatoService(BolsaLaboral bolsa) {
        this(bolsa, new CandidatoDAO());
    }

    public GestionCandidatoService(BolsaLaboral bolsa, CandidatoDAO candidatoDAO) {
        if (bolsa == null) {
            throw new IllegalArgumentException("La bolsa laboral es obligatoria.");
        }
        if (candidatoDAO == null) {
            throw new IllegalArgumentException("El DAO de candidatos es obligatorio.");
        }
        this.bolsa = bolsa;
        this.candidatoDAO = candidatoDAO;
    }

    public ArrayList<Candidato> listarDesdeBaseDeDatos() {
        return candidatoDAO.listarTodos();
    }

    public Candidato registrar(Candidato nuevoCandidato) {
        exigirGestionCandidatos();
        if (nuevoCandidato == null) {
            throw new IllegalArgumentException("El candidato es obligatorio.");
        }

        nuevoCandidato.setIdentificacion(bolsa.prepararCedula(null, nuevoCandidato.getIdentificacion()));
        nuevoCandidato.setEstado(Candidato.ESTADO_DESEMPLEADO);

        candidatoDAO.agregar(nuevoCandidato);
        bolsa.getCandidatos().add(nuevoCandidato);
        return nuevoCandidato;
    }

    public void modificar(Candidato candidatoModificar) {
        exigirGestionCandidatos();
        if (candidatoModificar == null) {
            throw new IllegalArgumentException("El candidato es obligatorio.");
        }

        int indice = bolsa.getCandidatos().indexOf(candidatoModificar);
        if (indice < 0 && candidatoModificar.getCodigo() != null) {
            Candidato actual = bolsa.buscarCandidatoByCodigo(candidatoModificar.getCodigo());
            indice = bolsa.getCandidatos().indexOf(actual);
        }
        if (indice < 0) {
            throw new IllegalArgumentException("Debe seleccionar un candidato registrado.");
        }
        Candidato existente = bolsa.getCandidatos().get(indice);

        candidatoModificar.setIdentificacion(
                bolsa.prepararCedula(existente, candidatoModificar.getIdentificacion()));
        candidatoModificar.actualizarEstadoLaboral();

        candidatoDAO.modificar(candidatoModificar);
        bolsa.getCandidatos().set(indice, candidatoModificar);
    }

    public int contarSolicitudesVinculadas(Candidato candidato) {
        if (candidato == null) {
            throw new IllegalArgumentException("El candidato es obligatorio.");
        }
        return candidatoDAO.contarSolicitudes(candidato);
    }

    public void eliminar(Candidato candidatoEliminar) throws NotRemovableException {
        exigirGestionCandidatos();
        if (candidatoEliminar == null) {
            throw new IllegalArgumentException("El candidato es obligatorio.");
        }

        candidatoDAO.eliminar(candidatoEliminar);
        bolsa.getCandidatos().remove(candidatoEliminar);
    }

    private void exigirGestionCandidatos() {
        AutorizacionService.exigirPermiso(bolsa.getUsuarioActual(), Permiso.GESTIONAR_CANDIDATOS);
    }
}