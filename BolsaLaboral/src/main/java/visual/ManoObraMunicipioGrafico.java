package visual;

import logico.ManoObraMunicipioDTO;

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

class ManoObraMunicipioGrafico extends JPanel implements Scrollable {

    private static final Color COLOR_CANDIDATOS = UIUtils.CANDIDATE_GREEN;
    private static final Color COLOR_VACANTES = UIUtils.TEAL;
    private static final int ALTO_FILA = 58;
    private static final int MARGEN_SUPERIOR = 74;
    private static final int MARGEN_INFERIOR = 36;

    private final List<ManoObraMunicipioDTO> resultados;

    ManoObraMunicipioGrafico(List<ManoObraMunicipioDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<ManoObraMunicipioDTO>emptyList()
                : new ArrayList<ManoObraMunicipioDTO>(resultados);
        setBackground(Color.WHITE);
        setOpaque(true);
        setToolTipText("Los nombres abreviados se muestran completos en la pestaña Tabla.");
        setPreferredSize(UIUtils.dimension(900,
                Math.max(420, MARGEN_SUPERIOR + MARGEN_INFERIOR
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
                g2.setColor(Color.DARK_GRAY);
                String mensaje = "No existen resultados para representar.";
                FontMetrics metrics = g2.getFontMetrics();
                g2.drawString(mensaje, Math.max(UIUtils.scale(16),
                                (getWidth() - metrics.stringWidth(mensaje)) / 2),
                        Math.max(UIUtils.scale(40), getHeight() / 2));
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
            int altoBarra = UIUtils.scale(15);
            int separacion = UIUtils.scale(4);

            g2.setColor(new Color(205, 205, 205));
            g2.drawLine(margenIzquierdo, UIUtils.scale(MARGEN_SUPERIOR - 8),
                    margenIzquierdo, getHeight() - UIUtils.scale(MARGEN_INFERIOR));

            for (int index = 0; index < resultados.size(); index++) {
                ManoObraMunicipioDTO resultado = resultados.get(index);
                int y = UIUtils.scale(MARGEN_SUPERIOR + index * ALTO_FILA);
                GraficoBarrasUtils.dibujarEtiqueta(g2, etiquetas.get(index),
                        margenIzquierdo, y + altoBarra);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo, y, finBarras,
                        altoBarra, resultado.getCandidatosDesempleados(), maximo,
                        COLOR_CANDIDATOS, finValores);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo,
                        y + altoBarra + separacion, finBarras, altoBarra,
                        resultado.getVacantesDisponibles(), maximo,
                        COLOR_VACANTES, finValores);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarLeyenda(Graphics2D g2) {
        int y = UIUtils.scale(24);
        int cuadro = UIUtils.scale(14);
        int x = UIUtils.scale(24);
        g2.setColor(COLOR_CANDIDATOS);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Candidatos desempleados",
                x + cuadro + UIUtils.scale(6), y + cuadro - 1);

        x += UIUtils.scale(190);
        g2.setColor(COLOR_VACANTES);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Vacantes disponibles", x + cuadro + UIUtils.scale(6), y + cuadro - 1);
    }

    private List<String> etiquetas() {
        ArrayList<String> etiquetas = new ArrayList<String>();
        for (ManoObraMunicipioDTO resultado : resultados) {
            etiquetas.add(etiqueta(resultado));
        }
        return etiquetas;
    }

    private String etiqueta(ManoObraMunicipioDTO resultado) {
        String municipio = resultado.getMunicipio() == null ? "Sin nombre" : resultado.getMunicipio();
        String provincia = resultado.getProvincia();
        return provincia == null ? municipio : municipio + " (" + provincia + ")";
    }

    private int valorMaximo() {
        int maximo = 1;
        for (ManoObraMunicipioDTO resultado : resultados) {
            maximo = Math.max(maximo, resultado.getCandidatosDesempleados());
            maximo = Math.max(maximo, resultado.getVacantesDisponibles());
        }
        return maximo;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return UIUtils.dimension(900, 500);
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
