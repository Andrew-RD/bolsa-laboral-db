package logico;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Locale;

/** Tipo interno estable usado por registro y matching. */
public enum TipoCandidato implements Serializable {
    UNIVERSITARIO("Universitario / Profesional"),
    TECNICO("Técnico Superior"),
    OBRERO("Obrero");

    private final String etiqueta;

    TipoCandidato(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoCandidato desdeTextoLegado(String texto) {
        String normalizado = normalizar(texto);
        if (normalizado.contains("universit")) {
            return UNIVERSITARIO;
        }
        if (normalizado.contains("tecnic")) {
            return TECNICO;
        }
        if (normalizado.contains("obrero")) {
            return OBRERO;
        }
        return null;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
