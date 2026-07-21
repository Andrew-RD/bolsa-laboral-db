package visual;

import exception.FormatException;
import logico.BolsaLaboral;
import logico.CentroEmpleador;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;

public class RegistroCentro extends JDialog {

    private CentroEmpleador centroAct;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtProvincia;
    private JTextField txtMunicipio;
    private JComboBox<String> cmbSector;
    private JLabel lblIcono;
    private JTextField txtRNC;

    public RegistroCentro(CentroEmpleador centro) {
        setIconImage(UIUtils.image("icono.png"));
        centroAct = centro;
        setTitle(centro == null ? "Registrar Centro de Trabajo" : "Modificar Centro de Trabajo");

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIUtils.SURFACE);
        contentPanel.setBorder(UIUtils.emptyBorder(8, 8, 8, 8));
        setContentPane(contentPanel);

        JPanel form = UIUtils.titledPanel("Datos del Centro Empleador");
        form.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                form.getBorder(), UIUtils.emptyBorder(6, 6, 6, 6)));

        txtCodigo = textField();
        txtCodigo.setEditable(false);
        txtCodigo.setFocusable(false);
        txtCodigo.setText("CEN-" + BolsaLaboral.genCodigoCentro);
        UIUtils.addFormRow(form, 0, "Código:", txtCodigo);

        JSeparator separator = new JSeparator();
        UIUtils.addFullWidth(form, separator, 1);

        txtNombre = textField();
        UIUtils.addFormRow(form, 2, "Nombre:", txtNombre);

        cmbSector = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{
                "No definido", "Turismo", "Tecnología", "Salud", "Comercio", "Educación",
                "Agricultura", "Construcción", "Jurídico", "Arte", "Transporte"
        }));
        cmbSector.setMaximumRowCount(11);
        cmbSector.addActionListener(event -> cargarSector());
        lblIcono = new JLabel();
        UIUtils.addFormRow(form, 3, "Sector:", cmbSector, lblIcono);

        txtRNC = textField();
        UIUtils.addFormRow(form, 4, "RNC:", txtRNC);

        JPanel contacts = UIUtils.titledPanel("Contactos y Ubicación");
        txtTelefono = textField();
        txtCorreo = textField();
        txtProvincia = textField();
        txtMunicipio = textField();
        UIUtils.addFormRow(contacts, 0, "Teléfono:", txtTelefono);
        UIUtils.addFormRow(contacts, 1, "Correo:", txtCorreo);
        UIUtils.addFormRow(contacts, 2, "Provincia:", txtProvincia);
        UIUtils.addFormRow(contacts, 3, "Municipio:", txtMunicipio);
        GridBagConstraints contactsConstraints = UIUtils.constraints(0, 5);
        contactsConstraints.gridwidth = GridBagConstraints.REMAINDER;
        contactsConstraints.weightx = 1;
        contactsConstraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(contacts, contactsConstraints);
        UIUtils.addVerticalFiller(form, 6);

        contentPanel.add(UIUtils.scrollable(form), BorderLayout.CENTER);

        JPanel buttonBar = UIUtils.buttonBar(UIUtils.TEAL);
        JButton btnLimpiar = UIUtils.button("Limpiar", "cerrar.png");
        btnLimpiar.setIcon(null);
        btnLimpiar.addActionListener(event -> limpiar());

        JButton confirmButton = UIUtils.button(
                centroAct == null ? "Registrar" : "Modificar",
                centroAct == null ? "agregarP.png" : "modificar.png");
        confirmButton.addActionListener(event -> confirmar());

        JButton cancelButton = UIUtils.button("Cancelar", "cerrar.png");
        cancelButton.addActionListener(event -> dispose());
        buttonBar.add(btnLimpiar);
        buttonBar.add(confirmButton);
        buttonBar.add(cancelButton);
        contentPanel.add(buttonBar, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(confirmButton);

        cargarDatos();
        UIUtils.finishDialog(this, getOwner(), 620, 600);
    }

    private JTextField textField() {
        return new JTextField(24);
    }

    private void confirmar() {
        try {
            if (!verificar()) {
                JOptionPane.showMessageDialog(this, "Todos los registros son obligatorios.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (centroAct != null) {
                centroAct.setCorreo(txtCorreo.getText());
                centroAct.setMunicipio(txtMunicipio.getText());
                centroAct.setNombre(txtNombre.getText());
                centroAct.setProvincia(txtProvincia.getText());
                centroAct.setRnc(txtRNC.getText());
                centroAct.setSector(cmbSector.getSelectedItem().toString());
                centroAct.setTelefono(txtTelefono.getText());
                if (BolsaLaboral.getInstancia().modificarCentroTrabajo(centroAct)) {
                    ConsultarCentros.cargarCentros();
                    JOptionPane.showMessageDialog(this,
                            "El centro " + txtNombre.getText() + " ha sido modificado exitosamente.",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "El centro " + txtNombre.getText() + " no logró ser modificado.");
                }
            } else {
                CentroEmpleador nuevoCentro = new CentroEmpleador(
                        txtCodigo.getText(), txtNombre.getText(), cmbSector.getSelectedItem().toString(),
                        txtProvincia.getText(), txtMunicipio.getText(), txtTelefono.getText(),
                        txtCorreo.getText(), txtRNC.getText());
                BolsaLaboral.getInstancia().registrarCentroTrabajo(nuevoCentro);
                JOptionPane.showMessageDialog(this,
                        "El centro de trabajo ha sido agregado correctamente.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                txtCodigo.setText("CEN-" + BolsaLaboral.genCodigoCentro);
                limpiar();
            }
        } catch (FormatException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean verificar() throws FormatException {
        if (txtNombre.getText().isEmpty()) {
            throw new FormatException("El nombre no puede estar vacío.");
        } else if (txtRNC.getText().isEmpty()) {
            throw new FormatException("El RNC no puede estar vacío.");
        } else if (txtRNC.getText().length() != 9 || !txtRNC.getText().matches("\\d+")) {
            throw new FormatException("El RNC debe tener 9 dígitos.");
        } else if (txtTelefono.getText().isEmpty()) {
            throw new FormatException("El teléfono no puede estar vacío.");
        } else if (!txtTelefono.getText().matches("\\d{10}")) {
            throw new FormatException("El teléfono debe tener 10 dígitos.");
        } else if (txtCorreo.getText().isEmpty()) {
            throw new FormatException("El correo no puede estar vacío.");
        } else if (!txtCorreo.getText().contains("@") || !txtCorreo.getText().contains(".")) {
            throw new FormatException("Formato del correo inválido. Ejemplo: usuario@dominio.com");
        } else if (txtProvincia.getText().isEmpty()) {
            throw new FormatException("La provincia no puede estar vacía.");
        } else if (txtMunicipio.getText().isEmpty()) {
            throw new FormatException("El municipio no puede estar vacío.");
        }
        return true;
    }

    private void limpiar() {
        txtCorreo.setText("");
        txtMunicipio.setText("");
        txtNombre.setText("");
        txtProvincia.setText("");
        txtRNC.setText("");
        txtTelefono.setText("");
        cmbSector.setSelectedIndex(0);
    }

    private void cargarDatos() {
        cmbSector.setSelectedIndex(0);
        if (centroAct != null) {
            cmbSector.setSelectedItem(centroAct.getSector());
            txtCodigo.setText(centroAct.getCodigo());
            txtCorreo.setText(centroAct.getCorreo());
            txtMunicipio.setText(centroAct.getMunicipio());
            txtNombre.setText(centroAct.getNombre());
            txtProvincia.setText(centroAct.getProvincia());
            txtRNC.setText(centroAct.getRnc());
            txtTelefono.setText(centroAct.getTelefono());
        }
    }

    private void cargarSector() {
        if (cmbSector.getSelectedItem() != null) {
            lblIcono.setIcon(UIUtils.valueIcon(cmbSector.getSelectedItem().toString()));
        }
    }
}
