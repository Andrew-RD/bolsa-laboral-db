package visual;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

/** Cálculos de distribución compartidos por los gráficos gerenciales. */
final class GraficoBarrasUtils {

    private GraficoBarrasUtils() {
    }

    static int calcularMargenIzquierdo(FontMetrics metrics, List<String> etiquetas) {
        int margen = UIUtils.scale(150);
        for (String etiqueta : etiquetas) {
            margen = Math.max(margen,
                    metrics.stringWidth(normalizar(etiqueta)) + UIUtils.scale(28));
        }
        return Math.min(margen, UIUtils.scale(320));
    }

    static int anchoColumna(FontMetrics metrics, Iterable<String> valores, int minimo) {
        int ancho = UIUtils.scale(minimo);
        for (String valor : valores) {
            ancho = Math.max(ancho,
                    metrics.stringWidth(normalizar(valor)) + UIUtils.scale(12));
        }
        return ancho;
    }

    static void dibujarEtiqueta(Graphics2D g2, String etiqueta,
                                int margenIzquierdo, int y) {
        FontMetrics metrics = g2.getFontMetrics();
        String texto = recortar(normalizar(etiqueta), metrics,
                margenIzquierdo - UIUtils.scale(20));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(texto, UIUtils.scale(12), y);
    }

    static void dibujarBarra(Graphics2D g2, int x, int y, int finBarras,
                             int alto, int valor, int maximo, Color color,
                             int finColumnaValor) {
        int anchoDisponible = Math.max(0, finBarras - x);
        int ancho = valor == 0 ? 0 : Math.max(UIUtils.scale(2),
                Math.round(anchoDisponible * valor / (float) Math.max(1, maximo)));
        ancho = Math.min(ancho, anchoDisponible);
        g2.setColor(color);
        g2.fillRoundRect(x, y, ancho, alto, UIUtils.scale(4), UIUtils.scale(4));

        String texto = String.valueOf(valor);
        FontMetrics metrics = g2.getFontMetrics();
        int textoX = Math.max(finBarras + UIUtils.scale(6),
                finColumnaValor - metrics.stringWidth(texto));
        textoX = Math.min(textoX,
                finColumnaValor - metrics.stringWidth(texto));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(texto, Math.max(0, textoX), y + alto - UIUtils.scale(2));
    }

    static void dibujarTextoDerecha(Graphics2D g2, String texto,
                                    int finColumna, int y, Color color) {
        FontMetrics metrics = g2.getFontMetrics();
        int x = Math.max(0, finColumna - metrics.stringWidth(normalizar(texto)));
        g2.setColor(color);
        g2.drawString(normalizar(texto), x, y);
    }

    private static String recortar(String texto, FontMetrics metrics, int anchoMaximo) {
        if (metrics.stringWidth(texto) <= anchoMaximo) {
            return texto;
        }
        String puntos = "...";
        int anchoDisponible = Math.max(0, anchoMaximo - metrics.stringWidth(puntos));
        int longitud = texto.length();
        while (longitud > 0
                && metrics.stringWidth(texto.substring(0, longitud)) > anchoDisponible) {
            longitud--;
        }
        return longitud == 0 ? puntos : texto.substring(0, longitud) + puntos;
    }

    private static String normalizar(String texto) {
        return texto == null || texto.trim().isEmpty() ? "Sin nombre" : texto;
    }
}
