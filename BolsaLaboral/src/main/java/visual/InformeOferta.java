package visual;

import logico.BolsaLaboral;
import logico.OfertaLaboral;
import logico.Solicitud;
import logico.AutorizacionService;
import logico.Permiso;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

public class InformeOferta extends JDialog {

    private JLabel lblArea;
    private JLabel lblPuesto;
    private JPanel pnlEnfasis;
    private JTable table;
    public static DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            Object value = getRowCount() == 0 ? null : getValueAt(0, column);
            return value == null ? Object.class : value.getClass();
        }
    };
    public static Object[] row;
    private JPanel pnlResumen;
    private JLabel lblCantidadVac;
    private JLabel lblJornada;
    private JLabel lblModalidad;
    private JLabel lblEstado;
    private JLabel lblDeAceptacin;
    private ArrayList<Solicitud> solicitudesVinculadas = new ArrayList<Solicitud>();

    public InformeOferta(OfertaLaboral oferta) {
        AutorizacionService.exigirPermiso(
                BolsaLaboral.getInstancia().getUsuarioActual(), Permiso.VER_INFORMES);
        setTitle("(" + oferta.getCodigo() + ") " + oferta.getPuesto());
        setIconImage(UIUtils.image("icono.png"));

        JPanel content = new JPanel(new BorderLayout(UIUtils.scale(10), UIUtils.scale(10)));
        content.setBackground(Color.WHITE);
        content.setBorder(UIUtils.emptyBorder(0, 0, 10, 0));
        setContentPane(content);

        pnlEnfasis = buildHeader();
        content.add(pnlEnfasis, BorderLayout.NORTH);

        JPanel tableSection = new JPanel(new BorderLayout(0, UIUtils.scale(8)));
        tableSection.setOpaque(false);
        tableSection.setBorder(UIUtils.emptyBorder(0, 12, 0, 12));
        JLabel title = new JLabel("Solicitudes Vinculadas", SwingConstants.CENTER);
        title.setFont(UIUtils.h4Font(Font.BOLD));
        tableSection.add(title, BorderLayout.NORTH);
        table = new JTable();
        modelo.setColumnIdentifiers(new String[]{"Código", "Solicitante", "Fecha Solicitud", "Estado", " "});
        table.setModel(modelo);
        UIUtils.configureTable(table);
        tableSection.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(tableSection, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(UIUtils.scale(8), 0));
        footer.setOpaque(false);
        footer.setBorder(UIUtils.emptyBorder(0, 0, 0, 12));
        pnlResumen = new JPanel(new BorderLayout());
        pnlResumen.setBorder(UIUtils.emptyBorder(8, 12, 8, 12));
        lblCantidadVac = new JLabel("Vacantes disponibles:");
        lblCantidadVac.setForeground(Color.WHITE);
        lblCantidadVac.setFont(UIUtils.h4Font(Font.PLAIN));
        pnlResumen.add(lblCantidadVac, BorderLayout.CENTER);
        footer.add(pnlResumen, BorderLayout.CENTER);
        JButton closeButton = UIUtils.button("Cerrar", "cerrar.png");
        closeButton.addActionListener(event -> dispose());
        footer.add(closeButton, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);

        cargarOferta(oferta);
        solicitudesVinculadas = BolsaLaboral.getInstancia().obtenerSolicitudesVinculadas(oferta);
        cargarDetalles();
        UIUtils.finishDialog(this, getOwner(), 800, 600);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, UIUtils.scale(6)));
        header.setBorder(UIUtils.emptyBorder(12, 16, 10, 16));
        lblPuesto = new JLabel("PUESTO LABORAL");
        lblPuesto.setForeground(Color.WHITE);
        lblPuesto.setFont(UIUtils.h1Font(Font.BOLD));
        header.add(lblPuesto, BorderLayout.NORTH);
        header.add(new JSeparator(), BorderLayout.CENTER);

        JPanel details = new JPanel(new FlowLayout(FlowLayout.LEFT, UIUtils.scale(12), 0));
        details.setOpaque(false);
        lblArea = new JLabel();
        lblJornada = new JLabel();
        lblModalidad = new JLabel();
        lblEstado = whiteLabel("Estado:");
        lblDeAceptacin = whiteLabel("% de Aceptación:");
        details.add(lblArea);
        details.add(lblJornada);
        details.add(lblModalidad);
        details.add(verticalSeparator());
        details.add(lblDeAceptacin);
        details.add(verticalSeparator());
        details.add(lblEstado);
        header.add(details, BorderLayout.SOUTH);
        return header;
    }

    private JLabel whiteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(UIUtils.h4Font(Font.PLAIN));
        return label;
    }

    private JSeparator verticalSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(UIUtils.dimension(1, 32));
        return separator;
    }

    public void cargarOferta(OfertaLaboral oferta) {
        lblPuesto.setText(oferta.getPuesto());
        lblArea.setToolTipText(oferta.getArea());
        lblEstado.setText("Estado: " + oferta.getEstado());
        lblCantidadVac.setText("Vacantes: " + oferta.getVacantesTotales()
                + " totales | " + oferta.getVacantesOcupadas()
                + " ocupadas | " + oferta.getVacantesDisponibles() + " disponibles");
        lblDeAceptacin.setText("% Mínimo: " + oferta.getPorcentajeMinimo() + "%");
        lblArea.setIcon(UIUtils.valueIcon(oferta.getArea()));
        lblJornada.setToolTipText(oferta.getJornada());
        lblJornada.setIcon(UIUtils.valueIcon(oferta.getJornada()));
        lblModalidad.setToolTipText(oferta.getModalidad());
        lblModalidad.setIcon(UIUtils.valueIcon(oferta.getModalidad()));
        Color fondo = getFondo(oferta.getArea());
        pnlEnfasis.setBackground(fondo);
        pnlResumen.setBackground(fondo);
    }

    public Color getFondo(String area) {
        switch (area) {
            case "Finanzas": return new Color(213, 69, 27);
            case "Recursos Humanos": return new Color(27, 60, 83);
            case "Marketing": return new Color(197, 23, 46);
            case "Limpieza": return new Color(78, 102, 136);
            case "Seguridad": return new Color(10, 64, 12);
            case "TI": return new Color(9, 107, 104);
            case "Salud": return new Color(162, 18, 50);
            case "Operaciones": return new Color(39, 63, 79);
            case "Administración": return new Color(190, 49, 68);
            case "Atención al Cliente": return new Color(130, 17, 49);
            default: return new Color(57, 62, 7);
        }
    }

    public void cargarDetalles() {
        modelo.setRowCount(0);
        row = new Object[table.getColumnCount()];
        for (Solicitud solicitud : solicitudesVinculadas) {
            row[0] = solicitud.getCodigo();
            row[1] = solicitud.getSolicitante().getNombres() + " " + solicitud.getSolicitante().getApellidos();
            row[2] = solicitud.getFechaSolicitud().toString();
            row[3] = solicitud.getEstado();
            row[4] = UIUtils.valueIcon(solicitud.getEstado());
            modelo.addRow(row);
        }
    }
}
