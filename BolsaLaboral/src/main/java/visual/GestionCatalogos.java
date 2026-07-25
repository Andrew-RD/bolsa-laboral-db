package visual;

import logico.BolsaLaboral;
import logico.ElementoCatalogo;
import logico.GestionCatalogoService;
import logico.TipoCatalogo;
import logico.TextoNormalizer;
import logico.AutorizacionService;
import logico.Permiso;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

public class GestionCatalogos extends JDialog {

    private final BolsaLaboral bolsa = BolsaLaboral.getInstancia();
    private final GestionCatalogoService servicio = new GestionCatalogoService(bolsa);
    private final JComboBox<TipoCatalogo> tipo =
            new JComboBox<TipoCatalogo>(TipoCatalogo.values());
    private final JTextField filtro = new JTextField(20);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Siglas", "Nombre completo / elemento", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton btnEstado = UIUtils.button("Activar/Desactivar", "gestion.png");
    private final JButton btnModificar = UIUtils.button("Modificar", "modificar.png");
    private final ArrayList<ElementoCatalogo> filas =
            new ArrayList<ElementoCatalogo>();
    private ElementoCatalogo seleccionado;

    public GestionCatalogos() {
        AutorizacionService.exigirPermiso(bolsa.getUsuarioActual(), Permiso.GESTIONAR_CATALOGOS);
        setTitle("Catálogos");
        setIconImage(UIUtils.image("icono.png"));
        setLayout(new BorderLayout());
        JPanel norte = UIUtils.buttonBar(UIUtils.SURFACE);
        JLabel lblTipo = new JLabel("Catálogo:");
        lblTipo.setFont(UIUtils.largeFont(Font.BOLD));
        norte.add(lblTipo);
        norte.add(tipo);
        norte.add(new JLabel("Filtro:"));
        norte.add(filtro);
        add(norte, BorderLayout.NORTH);
        UIUtils.configureTable(tabla);
        tabla.getSelectionModel().addListSelectionListener(event -> seleccionar());
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton agregar = UIUtils.button("Agregar", "agregarP.png");
        agregar.addActionListener(event -> agregar());
        btnModificar.addActionListener(event -> modificar());
        btnEstado.addActionListener(event -> cambiarEstado());
        JButton cerrar = UIUtils.button("Cerrar", "cerrar.png");
        cerrar.addActionListener(event -> dispose());
        JPanel botones = UIUtils.buttonBar(UIUtils.TEAL);
        botones.add(agregar);
        botones.add(btnModificar);
        botones.add(btnEstado);
        botones.add(cerrar);
        add(botones, BorderLayout.SOUTH);

        tipo.addActionListener(event -> cargar());
        filtro.getDocument().addDocumentListener(new SimpleDocumentListener(this::cargar));
        cargar();
        UIUtils.finishDialog(this, getOwner(), 720, 520);
    }

    private void cargar() {
        modelo.setRowCount(0);
        filas.clear();
        seleccionado = null;
        boolean universidades =
                tipo.getSelectedItem() == TipoCatalogo.UNIVERSIDADES;
        String buscado = TextoNormalizer.normalizar(filtro.getText());
        for (ElementoCatalogo elemento : bolsa.getCatalogos().getElementos(
                (TipoCatalogo) tipo.getSelectedItem())) {
            if (elemento != null && TextoNormalizer.normalizar(
                    elemento.getNombreMostrado()).contains(buscado)) {
                filas.add(elemento);
                modelo.addRow(new Object[]{
                        universidades ? elemento.getSiglas() : "",
                        universidades
                                ? elemento.getNombreCompleto() : elemento.getNombre(),
                        elemento.isActivo() ? "Activo" : "Inactivo"});
            }
        }
        btnEstado.setEnabled(false);
        btnModificar.setVisible(universidades);
        btnModificar.setEnabled(false);
    }

    private void seleccionar() {
        seleccionado = null;
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            int filaModelo = tabla.convertRowIndexToModel(fila);
            if (filaModelo < filas.size()) {
                seleccionado = filas.get(filaModelo);
            }
        }
        btnEstado.setEnabled(seleccionado != null);
        btnModificar.setEnabled(seleccionado != null
                && tipo.getSelectedItem() == TipoCatalogo.UNIVERSIDADES);
    }

    private void agregar() {
        if (tipo.getSelectedItem() == TipoCatalogo.UNIVERSIDADES) {
            editarUniversidad(null);
            return;
        }
        String nombre = JOptionPane.showInputDialog(this, "Nombre del elemento:",
                "Agregar al catálogo", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null) {
            return;
        }
        try {
            servicio.agregar((TipoCatalogo) tipo.getSelectedItem(), nombre);
            cargar();
        } catch (IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void modificar() {
        if (seleccionado != null
                && tipo.getSelectedItem() == TipoCatalogo.UNIVERSIDADES) {
            editarUniversidad(seleccionado);
        }
    }

    private void editarUniversidad(ElementoCatalogo universidad) {
        JTextField siglas = new JTextField(
                universidad == null ? "" : universidad.getSiglas(), 20);
        JTextField nombreCompleto = new JTextField(
                universidad == null ? "" : universidad.getNombreCompleto(), 30);
        JPanel formulario = new JPanel(new GridLayout(
                2, 2, UIUtils.scale(8), UIUtils.scale(8)));
        formulario.add(new JLabel("Siglas o nombre corto:"));
        formulario.add(siglas);
        formulario.add(new JLabel("Nombre completo:"));
        formulario.add(nombreCompleto);
        int opcion = JOptionPane.showConfirmDialog(this, formulario,
                universidad == null
                        ? "Registrar universidad" : "Modificar universidad",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            if (universidad == null) {
                servicio.agregarUniversidad(
                        siglas.getText(), nombreCompleto.getText());
            } else {
                servicio.modificarUniversidad(universidad,
                        siglas.getText(), nombreCompleto.getText());
            }
            cargar();
        } catch (IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void cambiarEstado() {
        if (seleccionado == null) {
            return;
        }
        try {
            servicio.cambiarEstado((TipoCatalogo) tipo.getSelectedItem(),
                    seleccionado, !seleccionado.isActivo());
            cargar();
        } catch (IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void mostrarError(RuntimeException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
                "Operación rechazada", JOptionPane.WARNING_MESSAGE);
    }
}
