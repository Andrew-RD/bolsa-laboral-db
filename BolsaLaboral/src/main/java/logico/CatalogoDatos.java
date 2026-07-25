package logico;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/** Estado serializable de los catálogos empresariales editables. */
public class CatalogoDatos implements Serializable {

    private static final long serialVersionUID = 1L;

    private EnumMap<TipoCatalogo, ArrayList<ElementoCatalogo>> elementos;

    public CatalogoDatos() {
        elementos = new EnumMap<TipoCatalogo, ArrayList<ElementoCatalogo>>(TipoCatalogo.class);
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
            if (tipo == TipoCatalogo.UNIVERSIDADES) {
                for (String[] universidad : universidadesPredeterminadas()) {
                    ElementoCatalogo existente = buscarUniversidad(
                            universidad[0], universidad[1], null);
                    if (existente == null) {
                        lista.add(ElementoCatalogo.universidadPredeterminada(
                                universidad[0], universidad[1]));
                        cambios++;
                    } else if (!universidad[0].equals(existente.getSiglas())
                            || !universidad[1].equals(existente.getNombreCompleto())) {
                        existente.actualizarDatosUniversidad(
                                universidad[0], universidad[1]);
                        cambios++;
                    }
                }
            } else {
                for (String predeterminado : predeterminados(tipo)) {
                    if (buscar(tipo, predeterminado) == null) {
                        lista.add(new ElementoCatalogo(predeterminado));
                        cambios++;
                    }
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
            String identificador, String valorHistorico) {
        ArrayList<ElementoCatalogo> universidades =
                new ArrayList<ElementoCatalogo>(getUniversidadesActivas());
        ElementoCatalogo historica = buscarPorIdentificador(
                TipoCatalogo.UNIVERSIDADES, identificador);
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

    public ElementoCatalogo buscarPorIdentificador(
            TipoCatalogo tipo, String identificador) {
        if (tipo == null || identificador == null
                || identificador.trim().isEmpty()) {
            return null;
        }
        for (ElementoCatalogo elemento : getElementos(tipo)) {
            if (elemento != null
                    && identificador.equals(elemento.getIdentificador())) {
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

    private static String[] predeterminados(TipoCatalogo tipo) {
        switch (tipo) {
            case UNIVERSIDADES:
                return new String[0];
            case CARRERAS:
                return new String[]{"Administración de Empresas", "Arquitectura", "Contabilidad",
                        "Derecho", "Ingeniería Civil", "Ingeniería Industrial",
                        "Ingeniería de Sistemas", "Medicina", "Mercadeo", "Psicología"};
            case AREAS_TECNICAS:
                return new String[]{"Administración", "Contabilidad", "Electricidad",
                        "Electrónica", "Enfermería", "Informática", "Mecánica", "Refrigeración"};
            case HABILIDADES:
                return new String[]{"Albañilería", "Carpintería", "Conducción", "Electricidad",
                        "Jardinería", "Limpieza", "Plomería", "Seguridad"};
            case IDIOMAS:
                return new String[]{"Español", "Inglés", "Francés", "Portugués", "Italiano",
                        "Alemán", "Mandarín"};
            case SECTORES_EMPRESARIALES:
                return new String[]{"Agricultura", "Comercio", "Construcción", "Educación",
                        "Finanzas", "Industria", "Salud", "Servicios", "Tecnología", "Turismo"};
            case AREAS_LABORALES:
                return new String[]{"Administración", "Agricultura", "Arte", "Atención al Cliente",
                        "Comercio", "Construcción", "Educación", "Finanzas", "Jurídico", "Limpieza",
                        "Marketing", "Operaciones", "Recursos Humanos", "Salud", "Seguridad",
                        "Tecnología", "TI", "Transporte", "Turismo"};
            default:
                throw new IllegalArgumentException("Tipo de catálogo no soportado: " + tipo);
        }
    }

    private static String[][] universidadesPredeterminadas() {
        return new String[][]{
                {"PUCMM", "Pontificia Universidad Católica Madre y Maestra"},
                {"UASD", "Universidad Autónoma de Santo Domingo"},
                {"INTEC", "Instituto Tecnológico de Santo Domingo"},
                {"UNPHU", "Universidad Nacional Pedro Henríquez Ureña"},
                {"UTESA", "Universidad Tecnológica de Santiago"},
                {"UNAPEC", "Universidad APEC"},
                {"O&M", "Universidad Dominicana O&M"},
                {"UCE", "Universidad Central del Este"},
                {"UCNE", "Universidad Católica Nordestana"},
                {"ISA", "Universidad ISA"}
        };
    }
}
