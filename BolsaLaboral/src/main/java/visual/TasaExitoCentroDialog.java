package visual;

import logico.TasaExitoCentroDTO;

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

class TasaExitoCentroDialog extends JDialog {

    TasaExitoCentroDialog(Window owner, List<TasaExitoCentroDTO> resultados) {
        super(owner, "Tasa de éxito de contratación por centro empleador",
                Dialog.ModalityType.APPLICATION_MODAL);
        setIconImage(UIUtils.image("icono.png"));

        List<TasaExitoCentroDTO> datos = resultados == null
                ? Collections.<TasaExitoCentroDTO>emptyList()
                : new ArrayList<TasaExitoCentroDTO>(resultados);

        JPanel content = new JPanel(new BorderLayout(UIUtils.scale(10), UIUtils.scale(10)));
        content.setBackground(UIUtils.SURFACE);
        content.setBorder(UIUtils.emptyBorder(0, 0, 8, 0));
        setContentPane(content);
        content.add(crearEncabezado(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.largeFont(Font.BOLD));
        tabs.addTab("Tabla", crearPestanaTabla(datos));
        JScrollPane graficoScroll = UIUtils.scrollable(new TasaExitoCentroGrafico(datos));
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

        UIUtils.finishDialog(this, owner, 980, 680);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new FlowLayout(
                FlowLayout.LEFT, UIUtils.scale(16), UIUtils.scale(14)));
        encabezado.setBackground(UIUtils.TEAL_DARK);
        JLabel titulo = new JLabel("Tasa de éxito de contratación por centro empleador");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        encabezado.add(titulo);
        return encabezado;
    }

    private JPanel crearPestanaTabla(List<TasaExitoCentroDTO> resultados) {
        JPanel panel = new JPanel(new BorderLayout(UIUtils.scale(8), UIUtils.scale(8)));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(10, 10, 10, 10));
        panel.add(crearIndicadores(resultados), BorderLayout.NORTH);

        JTable tabla = new JTable(new TasaExitoCentroTableModel(resultados));
        UIUtils.configureTable(tabla);
        tabla.setAutoCreateRowSorter(true);
        alinearNumeros(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(UIUtils.scale(220));
        tabla.getColumnModel().getColumn(4).setPreferredWidth(UIUtils.scale(160));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearIndicadores(List<TasaExitoCentroDTO> resultados) {
        int solicitudes = 0;
        int contrataciones = 0;
        int centrosBajoDesempeno = 0;
        for (TasaExitoCentroDTO resultado : resultados) {
            solicitudes += resultado.getSolicitudesRecibidas();
            contrataciones += resultado.getContrataciones();
            if (TasaExitoCentroDTO.DIAGNOSTICO_BAJO.equals(resultado.getDiagnostico())) {
                centrosBajoDesempeno++;
            }
        }
        double tasaGlobal = solicitudes == 0 ? 0.0 : (contrataciones * 100.0) / solicitudes;

        JPanel indicadores = new JPanel(new GridLayout(1, 4,
                UIUtils.scale(10), UIUtils.scale(10)));
        indicadores.setOpaque(false);
        indicadores.add(crearIndicador("Solicitudes recibidas", String.valueOf(solicitudes)));
        indicadores.add(crearIndicador("Contrataciones", String.valueOf(contrataciones)));
        indicadores.add(crearIndicador("Tasa de éxito global",
                String.format("%.2f%%", tasaGlobal)));
        indicadores.add(crearIndicador("Centros de bajo desempeño",
                String.valueOf(centrosBajoDesempeno)));
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
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(1).setCellRenderer(renderer);
        tabla.getColumnModel().getColumn(2).setCellRenderer(renderer);

        DefaultTableCellRenderer rendererTasa = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : String.format("%.2f%%", (Double) value));
            }
        };
        rendererTasa.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(3).setCellRenderer(rendererTasa);
    }
}