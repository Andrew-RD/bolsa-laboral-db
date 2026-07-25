package logico;

import java.text.Normalizer;
import java.util.Locale;

/** Normalización compartida para comparaciones tolerantes de texto. */
public final class TextoNormalizer {

    private TextoNormalizer() {
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
