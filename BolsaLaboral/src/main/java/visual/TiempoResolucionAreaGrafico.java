package visual;

import logico.TiempoResolucionAreaDTO;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TiempoResolucionAreaGrafico extends JPanel implements Scrollable {

    private static final Color COLOR_RESUELTAS = UIUtils.CANDIDATE_GREEN;
    private static final Color COLOR_PENDIENTES = UIUtils.TEAL;
    private static final Color COLOR_ATRASADAS = new Color(176, 91, 72);
    private static final int ALTO_FILA = 72;
    private static final int MARGEN_SUPERIOR = 76;
    private static final int MARGEN_INFERIOR = 36;

    private final List<TiempoResolucionAreaDTO> resultados;

    TiempoResolucionAreaGrafico(List<TiempoResolucionAreaDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<TiempoResolucionAreaDTO>emptyList()
                : new ArrayList<TiempoResolucionAreaDTO>(resultados);
        setBackground(Color.WHITE);
        setOpaque(true);
        setToolTipText("Los nombres abreviados se muestran completos en la pestaña Tabla.");
        setPreferredSize(UIUtils.dimension(950,
                Math.max(430, MARGEN_SUPERIOR + MARGEN_INFERIOR
                        + this.resultados.size() * ALTO_FILA)));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(UIUtils.defaultFont(Font.PLAIN));
            if (resultados.isEmpty()) {
                dibujarSinResultados(g2);
                return;
            }

            dibujarLeyenda(g2);
            FontMetrics metrics = g2.getFontMetrics();
            List<String> etiquetas = etiquetas();
            int margenIzquierdo = GraficoBarrasUtils.calcularMargenIzquierdo(
                    metrics, etiquetas);
            int maximo = valorMaximo();
            int finValores = getWidth() - UIUtils.scale(16);
            int anchoValores = GraficoBarrasUtils.anchoColumna(metrics,
                    Collections.singletonList(String.valueOf(maximo)), 44);
            int finBarras = Math.max(margenIzquierdo,
                    finValores - anchoValores - UIUtils.scale(10));
            int altoBarra = UIUtils.scale(13);
            int separacion = UIUtils.scale(3);

            g2.setColor(new Color(205, 205, 205));
            g2.drawLine(margenIzquierdo, UIUtils.scale(MARGEN_SUPERIOR - 8),
                    margenIzquierdo, getHeight() - UIUtils.scale(MARGEN_INFERIOR));

            for (int index = 0; index < resultados.size(); index++) {
                TiempoResolucionAreaDTO resultado = resultados.get(index);
                int y = UIUtils.scale(MARGEN_SUPERIOR + index * ALTO_FILA);
                GraficoBarrasUtils.dibujarEtiqueta(g2, etiquetas.get(index),
                        margenIzquierdo, y + altoBarra * 2);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo, y, finBarras,
                        altoBarra, resultado.getVinculacionesResueltas(), maximo,
                        COLOR_RESUELTAS, finValores);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo,
                        y + altoBarra + separacion, finBarras, altoBarra,
                        resultado.getVinculacionesPendientes(), maximo,
                        COLOR_PENDIENTES, finValores);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo,
                        y + (altoBarra + separacion) * 2, finBarras, altoBarra,
                        resultado.getPendientesMasSieteDias(), maximo,
                        COLOR_ATRASADAS, finValores);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarLeyenda(Graphics2D g2) {
        int y = UIUtils.scale(24);
        int cuadro = UIUtils.scale(14);
        int x = UIUtils.scale(24);
        x = dibujarLeyenda(g2, x, y, cuadro, COLOR_RESUELTAS, "Resueltas");
        x = dibujarLeyenda(g2, x, y, cuadro, COLOR_PENDIENTES, "Pendientes");
        dibujarLeyenda(g2, x, y, cuadro, COLOR_ATRASADAS, "Pendientes > 7 días");
    }

    private int dibujarLeyenda(Graphics2D g2, int x, int y, int cuadro,
                               Color color, String texto) {
        g2.setColor(color);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(texto, x + cuadro + UIUtils.scale(6), y + cuadro - 1);
        return x + cuadro + UIUtils.scale(18) + g2.getFontMetrics().stringWidth(texto);
    }

    private List<String> etiquetas() {
        ArrayList<String> etiquetas = new ArrayList<String>();
        for (TiempoResolucionAreaDTO resultado : resultados) {
            etiquetas.add(resultado.getAreaLaboral());
        }
        return etiquetas;
    }

    private int valorMaximo() {
        int maximo = 1;
        for (TiempoResolucionAreaDTO resultado : resultados) {
            maximo = Math.max(maximo, resultado.getVinculacionesResueltas());
            maximo = Math.max(maximo, resultado.getVinculacionesPendientes());
            maximo = Math.max(maximo, resultado.getPendientesMasSieteDias());
        }
        return maximo;
    }

    private void dibujarSinResultados(Graphics2D g2) {
        String mensaje = "No existen resultados para representar.";
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(mensaje, Math.max(UIUtils.scale(16),
                        (getWidth() - metrics.stringWidth(mensaje)) / 2),
                Math.max(UIUtils.scale(40), getHeight() / 2));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return UIUtils.dimension(950, 520);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return UIUtils.scale(18);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(UIUtils.scale(60), visibleRect.height - UIUtils.scale(60));
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
