package logico;

import Datos.CatalogoDAO;
import Datos.RequerimientoDAO;
import Datos.UniversidadDAO;

import java.util.Objects;

public final class GestionCatalogoService {

    private final BolsaLaboral bolsa;

    private final CatalogoDAO idiomasDAO;
    private final CatalogoDAO sectoresDAO;
    private final CatalogoDAO areasLaboralesDAO;
    private final RequerimientoDAO carrerasDAO;
    private final RequerimientoDAO areasTecnicasDAO;
    private final RequerimientoDAO habilidadesDAO;
    private final UniversidadDAO universidadDAO;

    public GestionCatalogoService(BolsaLaboral bolsa) {
        this(bolsa,
                new CatalogoDAO("idiomas", "id_idioma"),
                new CatalogoDAO("sectores", "id_sector"),
                new CatalogoDAO("areasLaborales", "id_areaLaboral"),
                new RequerimientoDAO("carreras"),
                new RequerimientoDAO("areasTecnicas"),
                new RequerimientoDAO("habilidades"),
                new UniversidadDAO());
    }

    public GestionCatalogoService(BolsaLaboral bolsa, CatalogoDAO idiomasDAO, CatalogoDAO sectoresDAO,
                                  CatalogoDAO areasLaboralesDAO, RequerimientoDAO carrerasDAO, RequerimientoDAO areasTecnicasDAO,
                                  RequerimientoDAO habilidadesDAO, UniversidadDAO universidadDAO) {
        if (bolsa == null) {
            throw new IllegalArgumentException("La bolsa laboral es obligatoria.");
        }
        this.bolsa = bolsa;
        this.idiomasDAO = idiomasDAO;
        this.sectoresDAO = sectoresDAO;
        this.areasLaboralesDAO = areasLaboralesDAO;
        this.carrerasDAO = carrerasDAO;
        this.areasTecnicasDAO = areasTecnicasDAO;
        this.habilidadesDAO = habilidadesDAO;
        this.universidadDAO = universidadDAO;
    }

    public ElementoCatalogo agregar(TipoCatalogo tipo, String nombre) {
        exigirPermiso();
        ElementoCatalogo elemento = bolsa.getCatalogos().agregar(tipo, nombre);
        try {
            agregarEnDao(tipo, elemento);
        } catch (RuntimeException e) {
            bolsa.getCatalogos().quitar(tipo, elemento);
            throw e;
        }
        return elemento;
    }

    public ElementoCatalogo agregarUniversidad(
            String siglas, String nombreCompleto) {
        exigirPermiso();
        ElementoCatalogo universidad = bolsa.getCatalogos().agregarUniversidad(
                siglas, nombreCompleto);
        try {
            universidadDAO.agregar(universidad);
        } catch (RuntimeException e) {
            bolsa.getCatalogos().quitar(TipoCatalogo.UNIVERSIDADES, universidad);
            throw e;
        }
        return universidad;
    }

    public void modificarUniversidad(ElementoCatalogo universidad,
                                     String siglas, String nombreCompleto) {
        exigirPermiso();

        String siglasAnteriores = universidad.getSiglas();
        String nombreAnterior = universidad.getNombreCompleto();

        bolsa.getCatalogos().modificarUniversidad(
                universidad, siglas, nombreCompleto);

        try {
            universidadDAO.modificar(universidad);
        } catch (RuntimeException e) {
            bolsa.getCatalogos().modificarUniversidad(
                    universidad, siglasAnteriores, nombreAnterior);
            throw e;
        }

        for (Candidato candidato : bolsa.getCandidatos()) {
            if (candidato instanceof Universitario) {
                Universitario universitario = (Universitario) candidato;
                if (Objects.equals(universidad.getId(),
                        universitario.getUniversidadId())) {
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

        boolean activoAnterior = elemento.isActivo();
        elemento.setActivo(activo);

        try {
            cambiarEstadoEnDao(tipo, elemento, activo);
        } catch (RuntimeException e) {
            elemento.setActivo(activoAnterior);
            throw e;
        }
    }

    private void agregarEnDao(TipoCatalogo tipo, ElementoCatalogo elemento) {
        switch (tipo) {
            case IDIOMAS:
                idiomasDAO.agregar(elemento);
                break;
            case SECTORES_EMPRESARIALES:
                sectoresDAO.agregar(elemento);
                break;
            case AREAS_LABORALES:
                areasLaboralesDAO.agregar(elemento);
                break;
            case CARRERAS:
                carrerasDAO.agregar(elemento);
                break;
            case AREAS_TECNICAS:
                areasTecnicasDAO.agregar(elemento);
                break;
            case HABILIDADES:
                habilidadesDAO.agregar(elemento);
                break;
            case UNIVERSIDADES:
                universidadDAO.agregar(elemento);
                break;
            default:
                throw new IllegalStateException("Tipo de catálogo no soportado: " + tipo);
        }
    }

    private void cambiarEstadoEnDao(TipoCatalogo tipo, ElementoCatalogo elemento, boolean activo) {
        switch (tipo) {
            case IDIOMAS:
                idiomasDAO.cambiarEstado(elemento, activo);
                break;
            case SECTORES_EMPRESARIALES:
                sectoresDAO.cambiarEstado(elemento, activo);
                break;
            case AREAS_LABORALES:
                areasLaboralesDAO.cambiarEstado(elemento, activo);
                break;
            case CARRERAS:
                carrerasDAO.cambiarEstado(elemento, activo);
                break;
            case AREAS_TECNICAS:
                areasTecnicasDAO.cambiarEstado(elemento, activo);
                break;
            case HABILIDADES:
                habilidadesDAO.cambiarEstado(elemento, activo);
                break;
            case UNIVERSIDADES:
                universidadDAO.cambiarEstado(elemento, activo);
                break;
            default:
                throw new IllegalStateException("Tipo de catálogo no soportado: " + tipo);
        }
    }

    private void exigirPermiso() {
        AutorizacionService.exigirPermiso(
                bolsa.getUsuarioActual(), Permiso.GESTIONAR_CATALOGOS);
    }
}