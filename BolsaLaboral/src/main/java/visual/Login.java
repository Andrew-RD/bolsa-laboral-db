package visual;

import Datos.CentroEmpleadorDAO;
import Datos.UsuarioDAO;
import exception.AuthException;
import logico.BolsaLaboral;
import logico.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class Login extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private boolean visible;

    public static void main(String[] args) {
        UIUtils.initializeLookAndFeel();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Login().setVisible(true);
                } catch (RuntimeException exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(null, exception.getMessage(),
                            "Error al iniciar", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public Login() {
        setTitle("Iniciar Sesión");
        setIconImage(UIUtils.image("icono.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UIUtils.DARK_BACKGROUND);
        setContentPane(contentPane);

        JPanel identityPanel = new JPanel(new GridBagLayout());
        identityPanel.setBackground(UIUtils.TEAL_DARK);
        identityPanel.setBorder(UIUtils.emptyBorder(24, 32, 24, 32));
        identityPanel.add(UIUtils.iconLabel("user.png"));
        contentPane.add(identityPanel, BorderLayout.WEST);

        JPanel form = UIUtils.formPanel();
        form.setBackground(UIUtils.DARK_BACKGROUND);
        form.setBorder(UIUtils.emptyBorder(24, 42, 32, 42));
        contentPane.add(form, BorderLayout.CENTER);

        JLabel title = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(UIUtils.h1Font(Font.BOLD));
        GridBagConstraints titleConstraints = UIUtils.constraints(0, 0);
        titleConstraints.gridwidth = GridBagConstraints.REMAINDER;
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleConstraints.weightx = 1;
        titleConstraints.insets = UIUtils.insets(0, 0, 8, 0);
        form.add(title, titleConstraints);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.WHITE);
        GridBagConstraints separatorConstraints = UIUtils.constraints(0, 1);
        separatorConstraints.gridwidth = GridBagConstraints.REMAINDER;
        separatorConstraints.fill = GridBagConstraints.HORIZONTAL;
        separatorConstraints.weightx = 1;
        separatorConstraints.insets = UIUtils.insets(0, 0, 16, 0);
        form.add(separator, separatorConstraints);

        JLabel userLabel = fieldLabel("Nombre de Usuario:");
        GridBagConstraints userLabelConstraints = UIUtils.constraints(0, 2);
        userLabelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        userLabelConstraints.insets = UIUtils.insets(8, 0, 4, 0);
        form.add(userLabel, userLabelConstraints);

        txtUsuario = new JTextField(24);
        txtUsuario.setFont(UIUtils.largeFont(Font.PLAIN));
        GridBagConstraints userFieldConstraints = UIUtils.constraints(0, 3);
        userFieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
        userFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        userFieldConstraints.weightx = 1;
        userFieldConstraints.insets = UIUtils.insets(0, 0, 8, 0);
        form.add(txtUsuario, userFieldConstraints);

        JLabel passwordLabel = fieldLabel("Contraseña:");
        GridBagConstraints passwordLabelConstraints = UIUtils.constraints(0, 4);
        passwordLabelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        passwordLabelConstraints.insets = UIUtils.insets(8, 0, 4, 0);
        form.add(passwordLabel, passwordLabelConstraints);

        txtContrasena = new JPasswordField(24);
        txtContrasena.setEchoChar('*');
        txtContrasena.setFont(UIUtils.largeFont(Font.PLAIN));

        final JLabel visibilityToggle = UIUtils.iconLabel("novisible.png");
        visibilityToggle.setToolTipText("Mostrar u ocultar contraseña");
        visibilityToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        visibilityToggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                visible = !visible;
                txtContrasena.setEchoChar(visible ? (char) 0 : '*');
                visibilityToggle.setIcon(UIUtils.icon(visible ? "visible.png" : "novisible.png"));
            }
        });

        JPanel passwordPanel = new JPanel(new BorderLayout(UIUtils.scale(8), 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(txtContrasena, BorderLayout.CENTER);
        passwordPanel.add(visibilityToggle, BorderLayout.EAST);
        GridBagConstraints passwordFieldConstraints = UIUtils.constraints(0, 5);
        passwordFieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
        passwordFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        passwordFieldConstraints.weightx = 1;
        passwordFieldConstraints.insets = UIUtils.insets(0, 0, 18, 0);
        form.add(passwordPanel, passwordFieldConstraints);

        JButton btnIniciarSesion = UIUtils.button("Iniciar Sesión", "iniciarsesion.png");
        btnIniciarSesion.addActionListener(event -> iniciarSesion());
        txtContrasena.addActionListener(event -> iniciarSesion());

        JButton btnCerrar = UIUtils.button("Cerrar", "cerrar.png");
        btnCerrar.addActionListener(event -> dispose());

        JPanel buttons = new JPanel(new java.awt.GridLayout(1, 2, UIUtils.scale(12), 0));
        buttons.setOpaque(false);
        buttons.add(btnIniciarSesion);
        buttons.add(btnCerrar);
        GridBagConstraints buttonConstraints = UIUtils.constraints(0, 6);
        buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.weightx = 1;
        buttonConstraints.insets = UIUtils.insets(0, 0, 0, 0);
        form.add(buttons, buttonConstraints);

        getRootPane().setDefaultButton(btnIniciarSesion);
        UIUtils.finishFrame(this, 700, 500);

        cargarDatosDesdeBaseDeDatos();
    }


    private void cargarDatosDesdeBaseDeDatos() {
        try {
            BolsaLaboral.getInstancia().setUsuarios(new UsuarioDAO().listarTodos());
            BolsaLaboral.getInstancia().setCentros(new CentroEmpleadorDAO().listarTodos());
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "No fue posible conectar con la base de datos:\n" + exception.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setForeground(Color.WHITE);
        label.setFont(UIUtils.h4Font(Font.PLAIN));
        return label;
    }

    private void iniciarSesion() {
        try {
            Usuario user = verificar();
            BolsaLaboral.getInstancia().setUsuarioActual(user);
            Principal menu = new Principal();
            menu.setVisible(true);
            dispose();
        } catch (AuthException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Usuario verificar() throws AuthException {
        Usuario encontrado = null;
        char[] clave = txtContrasena.getPassword();
        try {
            for (Usuario user : BolsaLaboral.getInstancia().getUsuarios()) {
                if (user != null && user.getNombreUsuario() != null
                        && user.getNombreUsuario().trim().equalsIgnoreCase(
                        txtUsuario.getText().trim())
                        && user.autenticar(clave)) {
                    encontrado = user;
                    break;
                }
            }
        } finally {
            Arrays.fill(clave, '\0');
        }
        if (encontrado == null) {
            throw new AuthException();
        }
        return encontrado;
    }
}