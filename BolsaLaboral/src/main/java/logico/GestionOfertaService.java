package logico;

import Datos.OfertaLaboralDAO;
import exception.NotRemovableException;

import java.util.ArrayList;

public final class GestionOfertaService {

    private final BolsaLaboral bolsa;
    private final OfertaLaboralDAO ofertaDAO;

    public GestionOfertaService(BolsaLaboral bolsa) {
        this(bolsa, new OfertaLaboralDAO());
    }

    public GestionOfertaService(BolsaLaboral bolsa, OfertaLaboralDAO ofertaDAO) {
        if (bolsa == null) {
            throw new IllegalArgumentException("La bolsa laboral es obligatoria.");
        }
        if (ofertaDAO == null) {
            throw new IllegalArgumentException("El DAO de ofertas laborales es obligatorio.");
        }
        this.bolsa = bolsa;
        this.ofertaDAO = ofertaDAO;
    }

    public ArrayList<OfertaLaboral> listarDesdeBaseDeDatos() {
        return ofertaDAO.listarTodos();
    }

    public OfertaLaboral registrar(OfertaLaboral nuevaOferta) {
        exigirGestionOfertas();
        if (nuevaOferta == null || nuevaOferta.getOfertador() == null) {
            throw new IllegalArgumentException("La oferta y su centro empleador son obligatorios.");
        }

        nuevaOferta.sincronizarVacantesOcupadas(0);

        ofertaDAO.agregar(nuevaOferta);
        bolsa.getOfertas().add(nuevaOferta);
        nuevaOferta.getOfertador().getOfertasLaborales().add(nuevaOferta);
        return nuevaOferta;
    }

    public void modificar(OfertaLaboral ofertaModificar) {
        exigirGestionOfertas();
        if (ofertaModificar == null) {
            throw new IllegalArgumentException("La oferta es obligatoria.");
        }

        int indice = bolsa.buscarIndiceOfertaByCodigo(ofertaModificar.getCodigo());
        if (indice == -1) {
            throw new IllegalArgumentException("Debe seleccionar una oferta laboral registrada.");
        }

        ofertaDAO.modificar(ofertaModificar);
        bolsa.getOfertas().set(indice, ofertaModificar);
    }

    public int contarSolicitudesVinculadas(OfertaLaboral oferta) {
        if (oferta == null) {
            throw new IllegalArgumentException("La oferta es obligatoria.");
        }
        return ofertaDAO.contarSolicitudes(oferta);
    }

    public void eliminar(OfertaLaboral ofertaEliminar) throws NotRemovableException {
        exigirGestionOfertas();
        if (ofertaEliminar == null) {
            throw new IllegalArgumentException("La oferta es obligatoria.");
        }

        ofertaDAO.eliminar(ofertaEliminar);
        bolsa.getOfertas().remove(ofertaEliminar);
        if (ofertaEliminar.getOfertador() != null) {
            ofertaEliminar.getOfertador().getOfertasLaborales().remove(ofertaEliminar);
        }
    }

    private void exigirGestionOfertas() {
        AutorizacionService.exigirPermiso(bolsa.getUsuarioActual(), Permiso.GESTIONAR_OFERTAS);
    }
}