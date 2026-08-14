package logico;

import Datos.CatalogoDAO;
import Datos.RequerimientoDAO;
import Datos.UniversidadDAO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class CatalogoDatos implements Serializable {

    private static final long serialVersionUID = 1L;

    private EnumMap<TipoCatalogo, ArrayList<ElementoCatalogo>> elementos;

    public CatalogoDatos() {
        this(new CatalogoDAO("idiomas", "id_idioma"),
                new CatalogoDAO("sectores", "id_sector"),
                new CatalogoDAO("areasLaborales", "id_areaLaboral"),
                new RequerimientoDAO("carreras"),
                new RequerimientoDAO("areasTecnicas"),
                new RequerimientoDAO("habilidades"),
                new UniversidadDAO());
    }

    public CatalogoDatos(CatalogoDAO idiomasDAO, CatalogoDAO sectoresDAO, CatalogoDAO areasLaboralesDAO,
                         RequerimientoDAO carrerasDAO, RequerimientoDAO areasTecnicasDAO,
                         RequerimientoDAO habilidadesDAO, UniversidadDAO universidadDAO) {
        elementos = new EnumMap<TipoCatalogo, ArrayList<ElementoCatalogo>>(TipoCatalogo.class);
        elementos.put(TipoCatalogo.IDIOMAS, idiomasDAO.listarTodos());
        elementos.put(TipoCatalogo.SECTORES_EMPRESARIALES, sectoresDAO.listarTodos());
        elementos.put(TipoCatalogo.AREAS_LABORALES, areasLaboralesDAO.listarTodos());
        elementos.put(TipoCatalogo.CARRERAS, carrerasDAO.listarTodos());
        elementos.put(TipoCatalogo.AREAS_TECNICAS, areasTecnicasDAO.listarTodos());
        elementos.put(TipoCatalogo.HABILIDADES, habilidadesDAO.listarTodos());
        elementos.put(TipoCatalogo.UNIVERSIDADES, universidadDAO.listarTodos());
        migrarDatosDeserializados();
    }

    public int migrarDatosDeserializados() {
        int cambios = 0;
        if (elementos == null) {
            elementos = new EnumMap<TipoCatalogo, ArrayList<ElementoCatalogo>>(TipoCatalogo.class);
            cambios++;
        }
        for (TipoCatalogo tipo : TipoCatalogo.values()) {
            ArrayList<ElementoCatalogo> lista = elementos.get(tipo);
            if (lista == null) {
                lista = new ArrayList<ElementoCatalogo>();
                elementos.put(tipo, lista);
                cambios++;
            }
            for (ElementoCatalogo elemento : lista) {
                if (elemento != null) {
                    cambios += elemento.migrarDatosDeserializados(tipo);
                }
            }
        }
        return cambios;
    }

    public List<ElementoCatalogo> getElementos(TipoCatalogo tipo) {
        ArrayList<ElementoCatalogo> lista = elementos.get(tipo);
        if (lista == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(lista);
    }

    public List<ElementoCatalogo> getUniversidadesActivas() {
        ArrayList<ElementoCatalogo> universidades =
                new ArrayList<ElementoCatalogo>();
        for (ElementoCatalogo elemento : getElementos(
                TipoCatalogo.UNIVERSIDADES)) {
            if (elemento != null && elemento.isActivo()) {
                universidades.add(elemento);
            }
        }
        return Collections.unmodifiableList(universidades);
    }

    public List<ElementoCatalogo> getUniversidadesParaEdicion(
            Integer id, String valorHistorico) {
        ArrayList<ElementoCatalogo> universidades =
                new ArrayList<ElementoCatalogo>(getUniversidadesActivas());
        ElementoCatalogo historica = buscarPorId(
                TipoCatalogo.UNIVERSIDADES, id);
        if (historica == null) {
            historica = buscarUniversidad(valorHistorico);
        }
        if (historica != null && !universidades.contains(historica)) {
            universidades.add(0, historica);
        }
        return universidades;
    }

    public List<String> getValoresActivos(TipoCatalogo tipo) {
        ArrayList<String> valores = new ArrayList<String>();
        for (ElementoCatalogo elemento : getElementos(tipo)) {
            if (elemento != null && elemento.isActivo()) {
                valores.add(elemento.getNombre());
            }
        }
        return valores;
    }

    public List<String> getValoresParaEdicion(TipoCatalogo tipo, String valorHistorico) {
        ArrayList<String> valores = new ArrayList<String>(getValoresActivos(tipo));
        if (valorHistorico != null && !valorHistorico.trim().isEmpty()
                && !contieneNormalizado(valores, valorHistorico)) {
            valores.add(0, valorHistorico);
        }
        return valores;
    }

    public ElementoCatalogo buscarPorId(
            TipoCatalogo tipo, Integer id) {
        if (tipo == null || id == null) {
            return null;
        }
        for (ElementoCatalogo elemento : getElementos(tipo)) {
            if (elemento != null && id.equals(elemento.getId())) {
                return elemento;
            }
        }
        return null;
    }

    public ElementoCatalogo buscarUniversidad(String texto) {
        return buscarUniversidad(texto, texto, null);
    }

    ElementoCatalogo agregar(TipoCatalogo tipo, String nombre) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de catálogo es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del elemento es obligatorio.");
        }
        if (tipo == TipoCatalogo.UNIVERSIDADES) {
            return agregarUniversidad("", nombre);
        }
        if (buscar(tipo, nombre) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un elemento equivalente, ignorando espacios, mayúsculas y acentos.");
        }
        ElementoCatalogo elemento = new ElementoCatalogo(nombre);
        elementos.get(tipo).add(elemento);
        return elemento;
    }

    ElementoCatalogo agregarUniversidad(String siglas, String nombreCompleto) {
        validarUniversidad(siglas, nombreCompleto, null);
        ElementoCatalogo universidad =
                ElementoCatalogo.universidad(siglas, nombreCompleto);
        elementos.get(TipoCatalogo.UNIVERSIDADES).add(universidad);
        return universidad;
    }

    void modificarUniversidad(ElementoCatalogo universidad,
                              String siglas, String nombreCompleto) {
        if (universidad == null || !getElementos(TipoCatalogo.UNIVERSIDADES)
                .contains(universidad)) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una universidad del catálogo.");
        }
        validarUniversidad(siglas, nombreCompleto, universidad);
        universidad.actualizarDatosUniversidad(siglas, nombreCompleto);
    }

    void quitar(TipoCatalogo tipo, ElementoCatalogo elemento) {
        ArrayList<ElementoCatalogo> lista = elementos.get(tipo);
        if (lista != null) {
            lista.remove(elemento);
        }
    }

    ElementoCatalogo buscar(TipoCatalogo tipo, String nombre) {
        if (tipo == TipoCatalogo.UNIVERSIDADES) {
            return buscarUniversidad(nombre);
        }
        String buscado = TextoNormalizer.normalizar(nombre);
        ArrayList<ElementoCatalogo> lista = elementos.get(tipo);
        if (lista == null) {
            return null;
        }
        for (ElementoCatalogo elemento : lista) {
            if (elemento != null
                    && TextoNormalizer.normalizar(elemento.getNombre()).equals(buscado)) {
                return elemento;
            }
        }
        return null;
    }

    private void validarUniversidad(String siglas, String nombreCompleto,
                                    ElementoCatalogo excluir) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre completo de la universidad es obligatorio.");
        }
        if (buscarUniversidad(siglas, nombreCompleto, excluir) != null) {
            throw new IllegalArgumentException(
                    "Ya existe una universidad con siglas o nombre completo equivalentes.");
        }
    }

    private ElementoCatalogo buscarUniversidad(String siglas,
                                               String nombreCompleto, ElementoCatalogo excluir) {
        String corto = TextoNormalizer.normalizar(siglas);
        String completo = TextoNormalizer.normalizar(nombreCompleto);
        for (ElementoCatalogo elemento : getElementos(
                TipoCatalogo.UNIVERSIDADES)) {
            if (elemento == null || elemento == excluir) {
                continue;
            }
            String cortoExistente =
                    TextoNormalizer.normalizar(elemento.getSiglas());
            String completoExistente =
                    TextoNormalizer.normalizar(elemento.getNombreCompleto());
            String nombreLegado =
                    TextoNormalizer.normalizar(elemento.getNombre());
            String mostrado =
                    TextoNormalizer.normalizar(elemento.getNombreMostrado());
            if ((!corto.isEmpty() && (corto.equals(cortoExistente)
                    || corto.equals(completoExistente)
                    || corto.equals(nombreLegado)
                    || corto.equals(mostrado)))
                    || (!completo.isEmpty() && (completo.equals(completoExistente)
                    || completo.equals(cortoExistente)
                    || completo.equals(nombreLegado)
                    || completo.equals(mostrado)))) {
                return elemento;
            }
        }
        return null;
    }

    private static boolean contieneNormalizado(List<String> valores, String buscado) {
        String clave = TextoNormalizer.normalizar(buscado);
        for (String valor : valores) {
            if (TextoNormalizer.normalizar(valor).equals(clave)) {
                return true;
            }
        }
        return false;
    }
}