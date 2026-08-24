package visual;

import logico.CoberturaOfertaDTO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CoberturaOfertaDialog extends JDialog {

    CoberturaOfertaDialog(Window owner, List<CoberturaOfertaDTO> resultados) {
        super(owner, "Ofertas activas con mayor dificultad de cobertura",
                Dialog.ModalityType.APPLICATION_MODAL);
        setIconImage(UIUtils.image("icono.png"));

        List<CoberturaOfertaDTO> datos = resultados == null
                ? Collections.<CoberturaOfertaDTO>emptyList()
                : new ArrayList<CoberturaOfertaDTO>(resultados);

        JPanel content = new JPanel(new BorderLayout(UIUtils.scale(10), UIUtils.scale(10)));
        content.setBackground(UIUtils.SURFACE);
        content.setBorder(UIUtils.emptyBorder(0, 0, 8, 0));
        setContentPane(content);
        content.add(crearEncabezado(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.largeFont(Font.BOLD));
        tabs.addTab("Tabla", crearPestanaTabla(datos));
        JScrollPane graficoScroll = UIUtils.scrollable(new CoberturaOfertaGrafico(datos));
        graficoScroll.getViewport().setBackground(Color.WHITE);
        tabs.addTab("Gráfico", graficoScroll);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(UIUtils.emptyBorder(0, 12, 0, 12));
        centro.add(tabs, BorderLayout.CENTER);
        content.add(centro, BorderLayout.CENTER);

        JButton cerrar = UIUtils.button("Cerrar", "cerrar.png");
        cerrar.addActionListener(event -> dispose());
        JPanel pie = UIUtils.buttonBar(UIUtils.TEAL);
        pie.add(cerrar);
        content.add(pie, BorderLayout.SOUTH);

        UIUtils.finishDialog(this, owner, 1120, 700);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new FlowLayout(
                FlowLayout.LEFT, UIUtils.scale(16), UIUtils.scale(14)));
        encabezado.setBackground(UIUtils.TEAL_DARK);
        JLabel titulo = new JLabel("Ofertas activas con mayor dificultad de cobertura");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        encabezado.add(titulo);
        return encabezado;
    }

    private JPanel crearPestanaTabla(List<CoberturaOfertaDTO> resultados) {
        JPanel panel = new JPanel(new BorderLayout(UIUtils.scale(8), UIUtils.scale(8)));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(10, 10, 10, 10));
        panel.add(crearIndicadores(resultados), BorderLayout.NORTH);

        JTable tabla = new JTable(new CoberturaOfertaTableModel(resultados));
        UIUtils.configureTable(tabla);
        tabla.setAutoCreateRowSorter(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        alinearNumeros(tabla);
        int[] anchos = {180, 190, 150, 120, 130, 140, 155, 120, 180};
        for (int columna = 0; columna < anchos.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(
                    UIUtils.scale(anchos[columna]));
        }
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearIndicadores(List<CoberturaOfertaDTO> resultados) {
        int pendientes = 0;
        int sinCandidatos = 0;
        int vacantesTotales = 0;
        int vacantesOcupadas = 0;
        for (CoberturaOfertaDTO resultado : resultados) {
            pendientes += resultado.getVacantesPendientes();
            vacantesTotales += resultado.getVacantesTotales();
            vacantesOcupadas += resultado.getVacantesOcupadas();
            if (resultado.getOportunidadesEnviadas() == 0) {
                sinCandidatos++;
            }
        }
        double coberturaGlobal = vacantesTotales == 0 ? 0.0
                : vacantesOcupadas * 100.0 / vacantesTotales;

        JPanel indicadores = new JPanel(new GridLayout(1, 4,
                UIUtils.scale(10), UIUtils.scale(10)));
        indicadores.setOpaque(false);
        indicadores.add(crearIndicador("Ofertas analizadas", String.valueOf(resultados.size())));
        indicadores.add(crearIndicador("Vacantes pendientes", String.valueOf(pendientes)));
        indicadores.add(crearIndicador("Sin candidatos vinculados", String.valueOf(sinCandidatos)));
        indicadores.add(crearIndicador("Cobertura global",
                String.format("%.2f%%", coberturaGlobal)));
        return indicadores;
    }

    private JPanel crearIndicador(String titulo, String valor) {
        JPanel panel = new JPanel(new BorderLayout(UIUtils.scale(8), 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 198, 198)),
                UIUtils.emptyBorder(7, 10, 7, 10)));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(UIUtils.defaultFont(Font.PLAIN));
        JLabel numero = new JLabel(valor, SwingConstants.RIGHT);
        numero.setFont(UIUtils.h4Font(Font.BOLD));
        numero.setForeground(UIUtils.TEAL_DARK);
        panel.add(etiqueta, BorderLayout.CENTER);
        panel.add(numero, BorderLayout.EAST);
        return panel;
    }

    private void alinearNumeros(JTable tabla) {
        DefaultTableCellRenderer numero = new DefaultTableCellRenderer();
        numero.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int columna = 3; columna <= 6; columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(numero);
        }

        DefaultTableCellRenderer porcentaje = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : String.format("%.2f%%", (Double) value));
            }
        };
        porcentaje.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(7).setCellRenderer(porcentaje);
    }
}
