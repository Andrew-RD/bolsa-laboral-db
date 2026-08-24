package visual;

import logico.TasaExitoCentroDTO;

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

class TasaExitoCentroGrafico extends JPanel implements Scrollable {

    private static final Color COLOR_OPORTUNIDADES = UIUtils.CANDIDATE_GREEN;
    private static final Color COLOR_CONTRATACIONES = UIUtils.TEAL;
    private static final int ALTO_FILA = 58;
    private static final int MARGEN_SUPERIOR = 74;
    private static final int MARGEN_INFERIOR = 36;

    private final List<TasaExitoCentroDTO> resultados;

    TasaExitoCentroGrafico(List<TasaExitoCentroDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<TasaExitoCentroDTO>emptyList()
                : new ArrayList<TasaExitoCentroDTO>(resultados);
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
            List<String> porcentajes = porcentajes();
            int margenIzquierdo = GraficoBarrasUtils.calcularMargenIzquierdo(
                    metrics, etiquetas);
            int maximo = valorMaximo();
            int finPorcentajes = getWidth() - UIUtils.scale(16);
            int anchoPorcentajes = GraficoBarrasUtils.anchoColumna(
                    metrics, porcentajes, 76);
            int inicioPorcentajes = finPorcentajes - anchoPorcentajes;
            int finValores = inicioPorcentajes - UIUtils.scale(12);
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
                TasaExitoCentroDTO resultado = resultados.get(index);
                int y = UIUtils.scale(MARGEN_SUPERIOR + index * ALTO_FILA);
                GraficoBarrasUtils.dibujarEtiqueta(g2, etiquetas.get(index),
                        margenIzquierdo, y + altoBarra);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo, y, finBarras,
                        altoBarra, resultado.getOportunidadesEnviadas(), maximo,
                        COLOR_OPORTUNIDADES, finValores);
                GraficoBarrasUtils.dibujarBarra(g2, margenIzquierdo,
                        y + altoBarra + separacion, finBarras, altoBarra,
                        resultado.getContrataciones(), maximo,
                        COLOR_CONTRATACIONES, finValores);
                dibujarTasa(g2, porcentajes.get(index), finPorcentajes, y + altoBarra);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarLeyenda(Graphics2D g2) {
        int y = UIUtils.scale(24);
        int cuadro = UIUtils.scale(14);
        int x = UIUtils.scale(24);
        g2.setColor(COLOR_OPORTUNIDADES);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Oportunidades enviadas",
                x + cuadro + UIUtils.scale(6), y + cuadro - 1);

        x += UIUtils.scale(190);
        g2.setColor(COLOR_CONTRATACIONES);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Contrataciones", x + cuadro + UIUtils.scale(6), y + cuadro - 1);
    }

    private List<String> etiquetas() {
        ArrayList<String> etiquetas = new ArrayList<String>();
        for (TasaExitoCentroDTO resultado : resultados) {
            etiquetas.add(etiqueta(resultado));
        }
        return etiquetas;
    }

    private String etiqueta(TasaExitoCentroDTO resultado) {
        return resultado.getCentroEmpleador() == null
                ? "Sin nombre" : resultado.getCentroEmpleador();
    }

    private List<String> porcentajes() {
        ArrayList<String> porcentajes = new ArrayList<String>();
        for (TasaExitoCentroDTO resultado : resultados) {
            porcentajes.add(String.format("%.2f%%", resultado.getTasaConversion()));
        }
        return porcentajes;
    }

    private void dibujarTasa(Graphics2D g2, String tasaConversion, int finColumna, int y) {
        g2.setFont(UIUtils.defaultFont(Font.BOLD));
        GraficoBarrasUtils.dibujarTextoDerecha(
                g2, tasaConversion, finColumna, y, UIUtils.TEAL_DARK);
        g2.setFont(UIUtils.defaultFont(Font.PLAIN));
    }

    private int valorMaximo() {
        int maximo = 1;
        for (TasaExitoCentroDTO resultado : resultados) {
            maximo = Math.max(maximo, resultado.getOportunidadesEnviadas());
            maximo = Math.max(maximo, resultado.getContrataciones());
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
