package visual;

import logico.ManoObraMunicipioDTO;

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

class ManoObraMunicipioDialog extends JDialog {

    ManoObraMunicipioDialog(Window owner, List<ManoObraMunicipioDTO> resultados) {
        super(owner, "Candidatos desempleados vs. vacantes disponibles por municipio",
                Dialog.ModalityType.APPLICATION_MODAL);
        setIconImage(UIUtils.image("icono.png"));

        List<ManoObraMunicipioDTO> datos = resultados == null
                ? Collections.<ManoObraMunicipioDTO>emptyList()
                : new ArrayList<ManoObraMunicipioDTO>(resultados);

        JPanel content = new JPanel(new BorderLayout(UIUtils.scale(10), UIUtils.scale(10)));
        content.setBackground(UIUtils.SURFACE);
        content.setBorder(UIUtils.emptyBorder(0, 0, 8, 0));
        setContentPane(content);
        content.add(crearEncabezado(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.largeFont(Font.BOLD));
        tabs.addTab("Tabla", crearPestanaTabla(datos));
        JScrollPane graficoScroll = UIUtils.scrollable(new ManoObraMunicipioGrafico(datos));
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
        JLabel titulo = new JLabel("Candidatos desempleados vs. vacantes disponibles por municipio");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(UIUtils.h2Font(Font.BOLD));
        encabezado.add(titulo);
        return encabezado;
    }

    private JPanel crearPestanaTabla(List<ManoObraMunicipioDTO> resultados) {
        JPanel panel = new JPanel(new BorderLayout(UIUtils.scale(8), UIUtils.scale(8)));
        panel.setBackground(UIUtils.SURFACE);
        panel.setBorder(UIUtils.emptyBorder(10, 10, 10, 10));
        panel.add(crearIndicadores(resultados), BorderLayout.NORTH);

        JTable tabla = new JTable(new ManoObraMunicipioTableModel(resultados));
        UIUtils.configureTable(tabla);
        tabla.setAutoCreateRowSorter(true);
        alinearNumeros(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(UIUtils.scale(150));
        tabla.getColumnModel().getColumn(1).setPreferredWidth(UIUtils.scale(150));
        tabla.getColumnModel().getColumn(2).setPreferredWidth(UIUtils.scale(180));
        tabla.getColumnModel().getColumn(3).setPreferredWidth(UIUtils.scale(160));
        tabla.getColumnModel().getColumn(4).setPreferredWidth(UIUtils.scale(100));
        tabla.getColumnModel().getColumn(5).setPreferredWidth(UIUtils.scale(190));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearIndicadores(List<ManoObraMunicipioDTO> resultados) {
        int candidatos = 0;
        int vacantes = 0;
        int municipiosSinOportunidades = 0;
        for (ManoObraMunicipioDTO resultado : resultados) {
            candidatos += resultado.getCandidatosDesempleados();
            vacantes += resultado.getVacantesDisponibles();
            if (resultado.getVacantesDisponibles() == 0 && resultado.getCandidatosDesempleados() > 0) {
                municipiosSinOportunidades++;
            }
        }

        JPanel indicadores = new JPanel(new GridLayout(1, 3,
                UIUtils.scale(10), UIUtils.scale(10)));
        indicadores.setOpaque(false);
        indicadores.add(crearIndicador("Candidatos desempleados", candidatos));
        indicadores.add(crearIndicador("Vacantes disponibles", vacantes));
        indicadores.add(crearIndicador("Municipios sin oportunidades", municipiosSinOportunidades));
        return indicadores;
    }

    private JPanel crearIndicador(String titulo, int valor) {
        JPanel panel = new JPanel(new BorderLayout(UIUtils.scale(8), 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 198, 198)),
                UIUtils.emptyBorder(7, 10, 7, 10)));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(UIUtils.defaultFont(Font.PLAIN));
        JLabel numero = new JLabel(String.valueOf(valor), SwingConstants.RIGHT);
        numero.setFont(UIUtils.h4Font(Font.BOLD));
        numero.setForeground(UIUtils.TEAL_DARK);
        panel.add(etiqueta, BorderLayout.CENTER);
        panel.add(numero, BorderLayout.EAST);
        return panel;
    }

    private void alinearNumeros(JTable tabla) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(2).setCellRenderer(renderer);
        tabla.getColumnModel().getColumn(3).setCellRenderer(renderer);
        tabla.getColumnModel().getColumn(4).setCellRenderer(renderer);
    }
}
