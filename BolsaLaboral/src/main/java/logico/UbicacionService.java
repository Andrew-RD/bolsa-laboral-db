package logico;

import Datos.UbicacionDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogo geográfico oficial, inmutable y cargado desde la base de datos. */
public final class UbicacionService {

    public static final String SELECCIONE_PROVINCIA = "Seleccione una provincia";
    public static final String SELECCIONE_MUNICIPIO = "Seleccione un municipio";
    private static final UbicacionService INSTANCIA = cargarPredeterminado();

    private final LinkedHashMap<String, List<String>> municipiosPorProvincia;

    private UbicacionService(UbicacionDAO dao) {
        municipiosPorProvincia = new LinkedHashMap<String, List<String>>();
        LinkedHashMap<String, ArrayList<String>> datos = dao.listarMunicipiosPorProvincia();
        for (Map.Entry<String, ArrayList<String>> entrada : datos.entrySet()) {
            if (entrada.getValue().isEmpty()) {
                throw new IllegalStateException(
                        "La provincia " + entrada.getKey() + " no contiene municipios.");
            }
            municipiosPorProvincia.put(entrada.getKey(),
                    Collections.unmodifiableList(new ArrayList<String>(entrada.getValue())));
        }
        if (municipiosPorProvincia.size() != 32) {
            throw new IllegalStateException(
                    "El catálogo geográfico debe contener 31 provincias y el Distrito Nacional.");
        }
    }

    public static UbicacionService getInstancia() {
        return INSTANCIA;
    }

    public List<String> getProvincias() {
        return Collections.unmodifiableList(
                new ArrayList<String>(municipiosPorProvincia.keySet()));
    }

    public List<String> getMunicipios(String provincia) {
        String canonica = buscarProvinciaCanonica(provincia);
        if (canonica == null) {
            return Collections.emptyList();
        }
        return municipiosPorProvincia.get(canonica);
    }

    public String buscarProvinciaCanonica(String valor) {
        return buscarCanonico(municipiosPorProvincia, valor);
    }

    public String buscarMunicipioCanonico(String provincia, String municipio) {
        String provinciaCanonica = buscarProvinciaCanonica(provincia);
        if (provinciaCanonica == null) {
            return null;
        }
        String buscado = clave(municipio);
        for (String canonico : municipiosPorProvincia.get(provinciaCanonica)) {
            if (clave(canonico).equals(buscado) || esAliasMunicipio(canonico, buscado)) {
                return canonico;
            }
        }
        return null;
    }

    public boolean esUbicacionValida(String provincia, String municipio) {
        return buscarMunicipioCanonico(provincia, municipio) != null;
    }

    private static String buscarCanonico(Map<String, ?> valores, String buscado) {
        String claveBuscada = clave(buscado);
        for (String canonico : valores.keySet()) {
            if (clave(canonico).equals(claveBuscada)) {
                return canonico;
            }
        }
        if ("montecristi".equals(claveBuscada)) {
            return "Monte Cristi";
        }
        if ("baoruco".equals(claveBuscada)) {
            return "Bahoruco";
        }
        return null;
    }

    private static boolean esAliasMunicipio(String canonico, String buscado) {
        String claveCanonica = clave(canonico);
        return ("santodomingodeguzman".equals(claveCanonica) && "santodomingo".equals(buscado))
                || ("concepciondelavega".equals(claveCanonica) && "lavega".equals(buscado))
                || ("eugeniomariadehostos".equals(claveCanonica) && "hostos".equals(buscado))
                || ("sanfelipedepuertoplata".equals(claveCanonica) && "puertoplata".equals(buscado))
                || ("sanignaciodesabaneta".equals(claveCanonica) && "sabaneta".equals(buscado))
                || ("villalosalmacigos".equals(claveCanonica) && "losalmacigos".equals(buscado));
    }

    private static String clave(String valor) {
        return TextoNormalizer.normalizar(valor).replaceAll("[^a-z0-9]", "");
    }

    private static UbicacionService cargarPredeterminado() {
        return new UbicacionService(new UbicacionDAO());
    }
}