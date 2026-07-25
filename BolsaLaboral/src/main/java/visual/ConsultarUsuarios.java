package visual;

import logico.BolsaLaboral;
import logico.GestionUsuarioService;
import logico.RolUsuario;
import logico.Usuario;
import logico.AutorizacionService;
import logico.Permiso;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Arrays;

public class ConsultarUsuarios extends JDialog {

    private final BolsaLaboral bolsa = BolsaLaboral.getInstancia();
    private final GestionUsuarioService servicio = new GestionUsuarioService(bolsa);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Usuario", "Nombre", "Correo", "Rol", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField filtro = new JTextField(24);
    private final JButton btnModificar = UIUtils.button("Modificar", "modificar.png");
    private final JButton btnEstado = UIUtils.button("Activar/Desactivar", "gestion.png");
    private final JButton btnPassword = UIUtils.button("Restablecer contraseña", "visible.png");
    private Usuario seleccionado;

    public ConsultarUsuarios() {
        AutorizacionService.exigirPermiso(bolsa.getUsuarioActual(), Permiso.GESTIONAR_USUARIOS);
        setTitle("Consultar Usuarios");
        setIconImage(UIUtils.image("icono.png"));
        setLayout(new BorderLayout());

        JPanel norte = UIUtils.buttonBar(UIUtils.SURFACE);
        JLabel etiqueta = new JLabel("Filtro:");
        etiqueta.setFont(UIUtils.largeFont(Font.BOLD));
        norte.add(etiqueta);
        norte.add(filtro);
        add(norte, BorderLayout.NORTH);

        UIUtils.configureTable(tabla);
        tabla.getSelectionModel().addListSelectionListener(event -> seleccionar());
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnRegistrar = UIUtils.button("Registrar", "registro.png");
        btnRegistrar.addActionListener(event -> abrirRegistro(null));
        btnModificar.addActionListener(event -> abrirRegistro(seleccionado));
        btnEstado.addActionListener(event -> cambiarEstado());
        btnPassword.addActionListener(event -> restablecer());
        JButton cerrar = UIUtils.button("Cerrar", "cerrar.png");
        cerrar.addActionListener(event -> dispose());
        JPanel botones = UIUtils.buttonBar(UIUtils.TEAL);
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnEstado);
        botones.add(btnPassword);
        botones.add(cerrar);
        add(botones, BorderLayout.SOUTH);

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<DefaultTableModel>(modelo);
        tabla.setRowSorter(sorter);
        filtro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                filtrar(sorter);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                filtrar(sorter);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                filtrar(sorter);
            }
        });

        cargar();
        actualizarBotones();
        UIUtils.finishDialog(this, getOwner(), 960, 560);
    }

    private void cargar() {
        modelo.setRowCount(0);
        for (Usuario usuario : bolsa.getUsuarios()) {
            if (usuario != null) {
                modelo.addRow(new Object[]{usuario.getNombreUsuario(), usuario.getNombreCompleto(),
                        usuario.getCorreo(), usuario.getRol().getDescripcion(),
                        usuario.isActivo() ? "Activo" : "Inactivo"});
            }
        }
        seleccionado = null;
        tabla.clearSelection();
        actualizarBotones();
    }

    private void seleccionar() {
        int vista = tabla.getSelectedRow();
        seleccionado = null;
        if (vista >= 0) {
            String nombre = String.valueOf(
                    modelo.getValueAt(tabla.convertRowIndexToModel(vista), 0));
            for (Usuario usuario : bolsa.getUsuarios()) {
                if (usuario != null && usuario.getNombreUsuario().equalsIgnoreCase(nombre)) {
                    seleccionado = usuario;
                    break;
                }
            }
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean haySeleccion = seleccionado != null;
        btnModificar.setEnabled(haySeleccion);
        btnPassword.setEnabled(haySeleccion);
        boolean puedeCambiarEstado = haySeleccion && seleccionado != bolsa.getUsuarioActual();
        if (puedeCambiarEstado && seleccionado.isActivo()
                && seleccionado.getRol() == RolUsuario.ADMINISTRADOR
                && servicio.contarAdministradoresActivos() <= 1) {
            puedeCambiarEstado = false;
        }
        btnEstado.setEnabled(puedeCambiarEstado);
        btnEstado.setToolTipText(puedeCambiarEstado ? null
                : "No puede desactivar al usuario actual ni al último administrador activo.");
    }

    private void abrirRegistro(Usuario usuario) {
        RegistroUsuario registro = new RegistroUsuario(usuario, this::cargar);
        registro.setModal(true);
        registro.setLocationRelativeTo(this);
        registro.setVisible(true);
    }

    private void cambiarEstado() {
        if (seleccionado == null) {
            return;
        }
        try {
            servicio.cambiarEstado(seleccionado, !seleccionado.isActivo());
            cargar();
        } catch (IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void restablecer() {
        if (seleccionado == null) {
            return;
        }
        JPasswordField password = new JPasswordField(20);
        JPasswordField confirmacion = new JPasswordField(20);
        JPanel panel = new JPanel(new GridLayout(0, 1, UIUtils.scale(4), UIUtils.scale(4)));
        panel.add(new JLabel("Nueva contraseña:"));
        panel.add(password);
        panel.add(new JLabel("Confirmar contraseña:"));
        panel.add(confirmacion);
        if (JOptionPane.showConfirmDialog(this, panel, "Restablecer contraseña",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        char[] clave = password.getPassword();
        char[] confirmar = confirmacion.getPassword();
        try {
            servicio.restablecerContrasena(seleccionado, clave, confirmar);
            JOptionPane.showMessageDialog(this, "Contraseña restablecida correctamente.",
                    "Gestión de usuarios", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        } finally {
            Arrays.fill(clave, '\0');
            Arrays.fill(confirmar, '\0');
        }
    }

    private void filtrar(TableRowSorter<DefaultTableModel> sorter) {
        sorter.setRowFilter(filtro.getText().trim().isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(
                        filtro.getText().trim())));
    }

    private void mostrarError(RuntimeException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
                "Operación rechazada", JOptionPane.WARNING_MESSAGE);
    }
}
