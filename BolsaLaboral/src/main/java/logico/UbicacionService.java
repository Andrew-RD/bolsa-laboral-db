package logico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogo geográfico oficial, inmutable y cargado desde el classpath. */
public final class UbicacionService {

    public static final String SELECCIONE_PROVINCIA = "Seleccione una provincia";
    public static final String SELECCIONE_MUNICIPIO = "Seleccione un municipio";
    private static final String RECURSO = "/catalogos/provincias_municipios.tsv";
    private static final UbicacionService INSTANCIA = cargarPredeterminado();

    private final LinkedHashMap<String, List<String>> municipiosPorProvincia;

    private UbicacionService(InputStream input) {
        if (input == null) {
            throw new IllegalStateException("No se encontró el recurso geográfico " + RECURSO + ".");
        }
        municipiosPorProvincia = new LinkedHashMap<String, List<String>>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String linea;
            int numero = 0;
            while ((linea = reader.readLine()) != null) {
                numero++;
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }
                String[] partes = linea.split("\\t", -1);
                if (partes.length != 2 || partes[0].trim().isEmpty()) {
                    throw new IllegalStateException(
                            "Fila geográfica inválida en " + RECURSO + ":" + numero + ".");
                }
                ArrayList<String> municipios = new ArrayList<String>();
                for (String municipio : partes[1].split("\\|")) {
                    String limpio = municipio.trim();
                    if (!limpio.isEmpty()) {
                        municipios.add(limpio);
                    }
                }
                if (municipios.isEmpty()) {
                    throw new IllegalStateException(
                            "La provincia " + partes[0] + " no contiene municipios.");
                }
                municipiosPorProvincia.put(partes[0].trim(),
                        Collections.unmodifiableList(municipios));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible leer el catálogo geográfico.", exception);
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
        return new UbicacionService(UbicacionService.class.getResourceAsStream(RECURSO));
    }
}
