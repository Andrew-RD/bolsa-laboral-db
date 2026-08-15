package logico;

import Datos.CentroEmpleadorDAO;
import exception.NotRemovableException;

import java.util.ArrayList;

public final class GestionCentroService {

    private final BolsaLaboral bolsa;
    private final CentroEmpleadorDAO centroDAO;

    public GestionCentroService(BolsaLaboral bolsa) {
        this(bolsa, new CentroEmpleadorDAO());
    }

    public GestionCentroService(BolsaLaboral bolsa, CentroEmpleadorDAO centroDAO) {
        if (bolsa == null) {
            throw new IllegalArgumentException("La bolsa laboral es obligatoria.");
        }
        if (centroDAO == null) {
            throw new IllegalArgumentException("El DAO de centros empleadores es obligatorio.");
        }
        this.bolsa = bolsa;
        this.centroDAO = centroDAO;
    }

    public ArrayList<CentroEmpleador> listarDesdeBaseDeDatos() {
        return centroDAO.listarTodos();
    }

    public CentroEmpleador registrar(CentroEmpleador nuevoCentro) {
        exigirGestionCentros();
        if (nuevoCentro == null) {
            throw new IllegalArgumentException("El centro empleador es obligatorio.");
        }

        nuevoCentro.setRnc(bolsa.prepararRnc(null, nuevoCentro.getRnc()));

        centroDAO.agregar(nuevoCentro);
        bolsa.getCentros().add(nuevoCentro);
        return nuevoCentro;
    }

    public void modificar(CentroEmpleador centroModificar) {
        exigirGestionCentros();
        if (centroModificar == null) {
            throw new IllegalArgumentException("El centro empleador es obligatorio.");
        }

        int indice = bolsa.buscarIndiceCentroByCodigo(centroModificar.getCodigo());
        if (indice == -1) {
            throw new IllegalArgumentException("Debe seleccionar un centro empleador registrado.");
        }
        CentroEmpleador existente = bolsa.getCentros().get(indice);

        centroModificar.setRnc(bolsa.prepararRnc(existente, centroModificar.getRnc()));

        centroDAO.modificar(centroModificar);
        bolsa.getCentros().set(indice, centroModificar);
    }

    public int contarOfertasVinculadas(CentroEmpleador centro) {
        if (centro == null) {
            throw new IllegalArgumentException("El centro empleador es obligatorio.");
        }
        return centroDAO.contarOfertas(centro);
    }

    public void eliminar(CentroEmpleador centroEliminar) throws NotRemovableException {
        exigirGestionCentros();
        if (centroEliminar == null) {
            throw new IllegalArgumentException("El centro empleador es obligatorio.");
        }

        centroDAO.eliminar(centroEliminar);
        bolsa.getCentros().remove(centroEliminar);
    }

    private void exigirGestionCentros() {
        AutorizacionService.exigirPermiso(bolsa.getUsuarioActual(), Permiso.GESTIONAR_CENTROS);
    }
}