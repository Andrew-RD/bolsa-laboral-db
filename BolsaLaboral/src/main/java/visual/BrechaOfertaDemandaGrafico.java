package visual;

import logico.BrechaOfertaDemandaDTO;

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

class BrechaOfertaDemandaGrafico extends JPanel implements Scrollable {

    private static final Color COLOR_OFERTAS = UIUtils.TEAL;
    private static final Color COLOR_CANDIDATOS = UIUtils.CANDIDATE_GREEN;
    private static final int ALTO_FILA = 58;
    private static final int MARGEN_SUPERIOR = 74;
    private static final int MARGEN_INFERIOR = 36;
    private static final int MARGEN_DERECHO = 58;

    private final List<BrechaOfertaDemandaDTO> resultados;

    BrechaOfertaDemandaGrafico(List<BrechaOfertaDemandaDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<BrechaOfertaDemandaDTO>emptyList()
                : new ArrayList<BrechaOfertaDemandaDTO>(resultados);
        setBackground(Color.WHITE);
        setOpaque(true);
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
            int margenIzquierdo = calcularMargenIzquierdo(g2);
            int anchoDisponible = Math.max(UIUtils.scale(80),
                    getWidth() - margenIzquierdo - UIUtils.scale(MARGEN_DERECHO));
            int maximo = valorMaximo();
            int altoBarra = UIUtils.scale(15);
            int separacion = UIUtils.scale(4);

            g2.setColor(new Color(205, 205, 205));
            g2.drawLine(margenIzquierdo, UIUtils.scale(MARGEN_SUPERIOR - 8),
                    margenIzquierdo, getHeight() - UIUtils.scale(MARGEN_INFERIOR));

            for (int index = 0; index < resultados.size(); index++) {
                BrechaOfertaDemandaDTO resultado = resultados.get(index);
                int y = UIUtils.scale(MARGEN_SUPERIOR + index * ALTO_FILA);
                dibujarArea(g2, resultado.getAreaLaboral(), margenIzquierdo, y + altoBarra);
                dibujarBarra(g2, margenIzquierdo, y, anchoDisponible, altoBarra,
                        resultado.getOfertasActivas(), maximo, COLOR_OFERTAS);
                dibujarBarra(g2, margenIzquierdo, y + altoBarra + separacion,
                        anchoDisponible, altoBarra, resultado.getCandidatosDesempleados(),
                        maximo, COLOR_CANDIDATOS);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarLeyenda(Graphics2D g2) {
        int y = UIUtils.scale(24);
        int cuadro = UIUtils.scale(14);
        int x = UIUtils.scale(24);
        g2.setColor(COLOR_OFERTAS);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Ofertas activas", x + cuadro + UIUtils.scale(6), y + cuadro - 1);

        x += UIUtils.scale(150);
        g2.setColor(COLOR_CANDIDATOS);
        g2.fillRoundRect(x, y, cuadro, cuadro, UIUtils.scale(3), UIUtils.scale(3));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Candidatos desempleados",
                x + cuadro + UIUtils.scale(6), y + cuadro - 1);
    }

    private int calcularMargenIzquierdo(Graphics2D g2) {
        FontMetrics metrics = g2.getFontMetrics();
        int maximo = UIUtils.scale(150);
        for (BrechaOfertaDemandaDTO resultado : resultados) {
            String area = resultado.getAreaLaboral() == null ? "" : resultado.getAreaLaboral();
            maximo = Math.max(maximo, metrics.stringWidth(area) + UIUtils.scale(28));
        }
        return Math.min(maximo, UIUtils.scale(340));
    }

    private void dibujarArea(Graphics2D g2, String area, int margenIzquierdo, int y) {
        String texto = area == null ? "Sin nombre" : area;
        FontMetrics metrics = g2.getFontMetrics();
        int anchoMaximo = margenIzquierdo - UIUtils.scale(20);
        while (texto.length() > 1 && metrics.stringWidth(texto) > anchoMaximo) {
            texto = texto.substring(0, texto.length() - 1);
        }
        if (area != null && !texto.equals(area)) {
            texto = texto.length() > 3 ? texto.substring(0, texto.length() - 3) + "..." : texto;
        }
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(texto, UIUtils.scale(12), y);
    }

    private void dibujarBarra(Graphics2D g2, int x, int y, int anchoDisponible,
                              int alto, int valor, int maximo, Color color) {
        int ancho = valor == 0 ? 0
                : Math.max(UIUtils.scale(2), Math.round(anchoDisponible * valor / (float) maximo));
        g2.setColor(color);
        g2.fillRoundRect(x, y, ancho, alto, UIUtils.scale(4), UIUtils.scale(4));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(String.valueOf(valor), x + ancho + UIUtils.scale(6),
                y + alto - UIUtils.scale(2));
    }

    private int valorMaximo() {
        int maximo = 1;
        for (BrechaOfertaDemandaDTO resultado : resultados) {
            maximo = Math.max(maximo, resultado.getOfertasActivas());
            maximo = Math.max(maximo, resultado.getCandidatosDesempleados());
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
