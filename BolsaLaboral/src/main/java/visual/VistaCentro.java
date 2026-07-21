package visual;

import logico.CentroEmpleador;
import logico.OfertaLaboral;

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

public class VistaCentro extends JDialog {

    private JLabel lblIconSec;
    private JLabel lblNombre;
    private JPanel pnlEnfasis;
    private JLabel lblTelefono;
    private JLabel lblCorreo;
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
    private JLabel lblDireccion;

    public VistaCentro(CentroEmpleador centroVista) {
        setTitle("(" + centroVista.getCodigo() + ") " + centroVista.getNombre());
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
        JLabel title = new JLabel("Ofertas Laborales Actuales", SwingConstants.CENTER);
        title.setFont(UIUtils.h4Font(Font.BOLD));
        tableSection.add(title, BorderLayout.NORTH);

        table = new JTable();
        modelo.setColumnIdentifiers(new String[]{"Código", "Puesto", "Área", "Estado"});
        table.setModel(modelo);
        UIUtils.configureTable(table);
        tableSection.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(tableSection, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(UIUtils.scale(8), 0));
        footer.setOpaque(false);
        footer.setBorder(UIUtils.emptyBorder(0, 0, 0, 12));
        pnlResumen = new JPanel(new BorderLayout());
        pnlResumen.setBorder(UIUtils.emptyBorder(8, 12, 8, 12));
        lblDireccion = new JLabel("", UIUtils.icon("ubicacion.png"), SwingConstants.LEFT);
        lblDireccion.setForeground(Color.WHITE);
        lblDireccion.setFont(UIUtils.h4Font(Font.PLAIN));
        pnlResumen.add(lblDireccion, BorderLayout.CENTER);
        footer.add(pnlResumen, BorderLayout.CENTER);
        JButton closeButton = UIUtils.button("Cerrar", "cerrar.png");
        closeButton.addActionListener(event -> dispose());
        footer.add(closeButton, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);

        cargarCentro(centroVista);
        cargarOfertas(centroVista);
        UIUtils.finishDialog(this, getOwner(), 760, 600);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(UIUtils.scale(8), UIUtils.scale(6)));
        header.setBorder(UIUtils.emptyBorder(12, 16, 10, 16));
        JPanel namePanel = new JPanel(new BorderLayout(UIUtils.scale(8), 0));
        namePanel.setOpaque(false);
        lblIconSec = new JLabel();
        namePanel.add(lblIconSec, BorderLayout.WEST);
        lblNombre = new JLabel("Nombre Empresa");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(UIUtils.h1Font(Font.BOLD));
        namePanel.add(lblNombre, BorderLayout.CENTER);
        header.add(namePanel, BorderLayout.NORTH);
        header.add(new JSeparator(), BorderLayout.CENTER);

        JPanel contacts = new JPanel(new FlowLayout(FlowLayout.LEFT, UIUtils.scale(18), 0));
        contacts.setOpaque(false);
        lblTelefono = new JLabel("", UIUtils.icon("contactos.png"), SwingConstants.LEFT);
        lblCorreo = new JLabel("", UIUtils.icon("correo.png"), SwingConstants.LEFT);
        lblTelefono.setForeground(Color.WHITE);
        lblCorreo.setForeground(Color.WHITE);
        lblTelefono.setFont(UIUtils.h4Font(Font.PLAIN));
        lblCorreo.setFont(UIUtils.h4Font(Font.PLAIN));
        contacts.add(lblTelefono);
        contacts.add(lblCorreo);
        header.add(contacts, BorderLayout.SOUTH);
        return header;
    }

    public void cargarCentro(CentroEmpleador centro) {
        lblNombre.setText(centro.getNombre());
        lblIconSec.setToolTipText(centro.getSector());
        lblTelefono.setText(" " + centro.getTelefono());
        lblTelefono.setToolTipText(centro.getTelefono());
        lblCorreo.setText(centro.getCorreo());
        lblDireccion.setText(" " + centro.getMunicipio() + ", " + centro.getProvincia());
        lblIconSec.setIcon(UIUtils.valueIcon(centro.getSector()));
        Color fondo = getFondo(centro.getSector());
        pnlEnfasis.setBackground(fondo);
        pnlResumen.setBackground(fondo);
    }

    public Color getFondo(String sector) {
        switch (sector) {
            case "No definido": return new Color(26, 26, 29);
            case "Turismo": return new Color(31, 125, 83);
            case "Tecnología": return new Color(17, 63, 103);
            case "Salud": return new Color(125, 10, 10);
            case "Comercio": return new Color(117, 14, 33);
            case "Educación": return new Color(51, 52, 70);
            case "Construcción": return new Color(84, 18, 18);
            case "Agricultura": return new Color(57, 153, 24);
            case "Jurídico": return new Color(68, 54, 39);
            case "Arte": return new Color(30, 81, 40);
            case "Transporte": return new Color(23, 49, 62);
            default: return Color.BLACK;
        }
    }

    public void cargarOfertas(CentroEmpleador centro) {
        modelo.setRowCount(0);
        row = new Object[table.getColumnCount()];
        for (OfertaLaboral oferta : centro.getOfertasLaborales()) {
            row[0] = oferta.getCodigo();
            row[1] = oferta.getPuesto();
            row[2] = UIUtils.valueIcon(oferta.getArea());
            row[3] = oferta.getEstado();
            modelo.addRow(row);
        }
    }
}
