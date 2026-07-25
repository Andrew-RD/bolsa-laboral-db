package logico;

public final class GestionCatalogoService {

    private final BolsaLaboral bolsa;

    public GestionCatalogoService(BolsaLaboral bolsa) {
        this.bolsa = bolsa;
    }

    public ElementoCatalogo agregar(TipoCatalogo tipo, String nombre) {
        exigirPermiso();
        return bolsa.getCatalogos().agregar(tipo, nombre);
    }

    public ElementoCatalogo agregarUniversidad(
            String siglas, String nombreCompleto) {
        exigirPermiso();
        return bolsa.getCatalogos().agregarUniversidad(
                siglas, nombreCompleto);
    }

    public void modificarUniversidad(ElementoCatalogo universidad,
            String siglas, String nombreCompleto) {
        exigirPermiso();
        bolsa.getCatalogos().modificarUniversidad(
                universidad, siglas, nombreCompleto);
        for (Candidato candidato : bolsa.getCandidatos()) {
            if (candidato instanceof Universitario) {
                Universitario universitario = (Universitario) candidato;
                if (universidad.getIdentificador().equals(
                        universitario.getUniversidadIdentificador())) {
                    universitario.setUniversidad(
                            universidad.getNombreCompleto());
                }
            }
        }
    }

    public void cambiarEstado(TipoCatalogo tipo, ElementoCatalogo elemento, boolean activo) {
        exigirPermiso();
        if (elemento == null || !bolsa.getCatalogos().getElementos(tipo).contains(elemento)) {
            throw new IllegalArgumentException("Debe seleccionar un elemento del catálogo.");
        }
        elemento.setActivo(activo);
    }

    private void exigirPermiso() {
        AutorizacionService.exigirPermiso(
                bolsa.getUsuarioActual(), Permiso.GESTIONAR_CATALOGOS);
    }
}
