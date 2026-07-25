package visual;

import logico.BolsaLaboral;
import logico.GestionUsuarioService;
import logico.Permiso;
import logico.PermisosPorRol;
import logico.RolUsuario;
import logico.Usuario;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/** Alta y modificación de usuarios construida exclusivamente con Swing. */
public class RegistroUsuario extends JDialog {

    private final Usuario usuario;
    private final Runnable alGuardar;
    private final GestionUsuarioService servicio;
    private final JTextField txtNombreCompleto = new JTextField(28);
    private final JTextField txtNombreUsuario = new JTextField(22);
    private final JTextField txtCorreo = new JTextField(28);
    private final JPasswordField txtPassword = new JPasswordField(22);
    private final JPasswordField txtConfirmacion = new JPasswordField(22);
    private final JComboBox<RolUsuario> cmbRol = new JComboBox<RolUsuario>(RolUsuario.values());
    private final JCheckBox chkActivo = new JCheckBox("Usuario activo", true);
    private final EnumMap<Permiso, JCheckBox> checks =
            new EnumMap<Permiso, JCheckBox>(Permiso.class);
    private final JButton btnPredeterminados =
            UIUtils.button("Aplicar permisos predeterminados", "gestion.png");

    public RegistroUsuario(Usuario usuario) {
        this(usuario, null);
    }

    public RegistroUsuario(Usuario usuario, Runnable alGuardar) {
        this.usuario = usuario;
        this.alGuardar = alGuardar;
        this.servicio = new GestionUsuarioService(BolsaLaboral.getInstancia());
        setTitle(usuario == null ? "Registrar Usuario" : "Modificar Usuario");
        setIconImage(UIUtils.image("icono.png"));
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.largeFont(Font.BOLD));
        tabs.addTab("Datos", crearDatos());
        tabs.addTab("Permisos", crearPermisos());
        add(tabs, BorderLayout.CENTER);

        JButton guardar = UIUtils.button("Guardar", "registro.png");
        guardar.addActionListener(event -> guardar());
        JButton cancelar = UIUtils.button("Cancelar", "cerrar.png");
        cancelar.addActionListener(event -> dispose());
        JPanel botones = UIUtils.buttonBar(UIUtils.TEAL);
        botones.add(guardar);
        botones.add(cancelar);
        add(botones, BorderLayout.SOUTH);

        cmbRol.addActionListener(event -> aplicarEstadoRol(false));
        btnPredeterminados.addActionListener(event -> aplicarEstadoRol(true));
        cargarUsuario();
        UIUtils.finishDialog(this, getOwner(), 680, 620);
    }

    private JPanel crearDatos() {
        JPanel panel = UIUtils.formPanel();
        panel.setBorder(UIUtils.emptyBorder(16, 20, 16, 20));
        UIUtils.addFormRow(panel, 0, "Nombre completo:", txtNombreCompleto);
        UIUtils.addFormRow(panel, 1, "Nombre de usuario:", txtNombreUsuario);
        UIUtils.addFormRow(panel, 2, "Correo:", txtCorreo);
        UIUtils.addFormRow(panel, 3,
                usuario == null ? "Contraseña:" : "Nueva contraseña (opcional):", txtPassword);
        UIUtils.addFormRow(panel, 4, "Confirmar contraseña:", txtConfirmacion);
        UIUtils.addFormRow(panel, 5, "Rol:", cmbRol);
        UIUtils.addFullWidth(panel, chkActivo, 6);
        UIUtils.addVerticalFiller(panel, 7);
        return panel;
    }

    private JPanel crearPermisos() {
        JPanel exterior = new JPanel(new BorderLayout(UIUtils.scale(8), UIUtils.scale(8)));
        exterior.setBackground(UIUtils.SURFACE);
        exterior.setBorder(UIUtils.emptyBorder(16, 20, 16, 20));

        JPanel grilla = new JPanel(new GridLayout(0, 2, UIUtils.scale(12), UIUtils.scale(8)));
        grilla.setOpaque(false);
        for (Permiso permiso : Permiso.values()) {
            JCheckBox check = new JCheckBox(permiso.getDescripcion());
            check.setOpaque(false);
            check.setFont(UIUtils.defaultFont(Font.PLAIN));
            checks.put(permiso, check);
            grilla.add(check);
        }
        exterior.add(grilla, BorderLayout.CENTER);
        exterior.add(btnPredeterminados, BorderLayout.SOUTH);
        return exterior;
    }

    private void cargarUsuario() {
        if (usuario == null) {
            cmbRol.setSelectedItem(RolUsuario.EMPLEADO);
            aplicarEstadoRol(true);
            return;
        }
        txtNombreCompleto.setText(usuario.getNombreCompleto());
        txtNombreUsuario.setText(usuario.getNombreUsuario());
        txtCorreo.setText(usuario.getCorreo());
        cmbRol.setSelectedItem(usuario.getRol());
        chkActivo.setSelected(usuario.isActivo());
        seleccionarPermisos(usuario.getPermisos());
        aplicarEstadoRol(false);
    }

    private void aplicarEstadoRol(boolean aplicarPredeterminados) {
        RolUsuario rol = (RolUsuario) cmbRol.getSelectedItem();
        EnumSet<Permiso> permisos = rol == null
                ? EnumSet.noneOf(Permiso.class) : PermisosPorRol.predeterminados(rol);
        boolean administrador = rol == RolUsuario.ADMINISTRADOR;
        for (Map.Entry<Permiso, JCheckBox> entry : checks.entrySet()) {
            JCheckBox check = entry.getValue();
            if (aplicarPredeterminados || administrador) {
                check.setSelected(permisos.contains(entry.getKey()));
            }
            check.setEnabled(!administrador);
        }
        btnPredeterminados.setEnabled(!administrador);
    }

    private void seleccionarPermisos(Iterable<Permiso> permisos) {
        EnumSet<Permiso> seleccionados = EnumSet.noneOf(Permiso.class);
        if (permisos != null) {
            for (Permiso permiso : permisos) {
                seleccionados.add(permiso);
            }
        }
        for (Map.Entry<Permiso, JCheckBox> entry : checks.entrySet()) {
            entry.getValue().setSelected(seleccionados.contains(entry.getKey()));
        }
    }

    private EnumSet<Permiso> permisosSeleccionados() {
        EnumSet<Permiso> permisos = EnumSet.noneOf(Permiso.class);
        for (Map.Entry<Permiso, JCheckBox> entry : checks.entrySet()) {
            if (entry.getValue().isSelected()) {
                permisos.add(entry.getKey());
            }
        }
        return permisos;
    }

    private void guardar() {
        char[] password = txtPassword.getPassword();
        char[] confirmacion = txtConfirmacion.getPassword();
        try {
            RolUsuario rol = (RolUsuario) cmbRol.getSelectedItem();
            if (usuario == null) {
                servicio.registrar(txtNombreCompleto.getText(), txtNombreUsuario.getText(),
                        txtCorreo.getText(), password, confirmacion, rol,
                        chkActivo.isSelected(), permisosSeleccionados());
            } else {
                if (password.length > 0 || confirmacion.length > 0) {
                    // Evita modificar parcialmente el perfil si la confirmación no coincide.
                    servicio.validarContrasenas(password, confirmacion, true);
                }
                servicio.modificar(usuario, txtNombreCompleto.getText(), txtNombreUsuario.getText(),
                        txtCorreo.getText(), rol, chkActivo.isSelected(), permisosSeleccionados());
                if (password.length > 0 || confirmacion.length > 0) {
                    servicio.restablecerContrasena(usuario, password, confirmacion);
                }
            }
            if (alGuardar != null) {
                alGuardar.run();
            }
            JOptionPane.showMessageDialog(this, "Usuario guardado correctamente.",
                    "Gestión de usuarios", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException | SecurityException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "No se pudo guardar", JOptionPane.WARNING_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmacion, '\0');
        }
    }
}
