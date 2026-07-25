package visual;

import exception.FormatException;
import logico.BolsaLaboral;
import logico.CentroEmpleador;
import logico.TipoCatalogo;
import logico.ResultadoDocumento;
import logico.RncValidator;

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
import java.util.Objects;

public class RegistroCentro extends JDialog {

    private CentroEmpleador centroAct;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JComboBox<String> cmbProvincia;
    private JComboBox<String> cmbMunicipio;
    private UbicacionComboSupport ubicacion;
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

        cmbSector = new JComboBox<String>(valoresCatalogo(TipoCatalogo.SECTORES_EMPRESARIALES,
                centro == null ? null : centro.getSector()));
        cmbSector.setMaximumRowCount(11);
        cmbSector.addActionListener(event -> cargarSector());
        lblIcono = new JLabel();
        UIUtils.addFormRow(form, 3, "Sector:", cmbSector, lblIcono);

        txtRNC = textField();
        UIUtils.addFormRow(form, 4, "RNC:", txtRNC);

        JPanel contacts = UIUtils.titledPanel("Contactos y Ubicación");
        txtTelefono = textField();
        txtCorreo = textField();
        ubicacion = new UbicacionComboSupport();
        cmbProvincia = ubicacion.getProvinciaCombo();
        cmbMunicipio = ubicacion.getMunicipioCombo();
        UIUtils.addFormRow(contacts, 0, "Teléfono:", txtTelefono);
        UIUtils.addFormRow(contacts, 1, "Correo:", txtCorreo);
        UIUtils.addFormRow(contacts, 2, "Provincia:", cmbProvincia);
        UIUtils.addFormRow(contacts, 3, "Municipio:", cmbMunicipio);
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
                String rnc = BolsaLaboral.getInstancia().prepararRnc(
                        centroAct, txtRNC.getText().trim());
                advertirLegadosSiCorresponde(rnc);
                centroAct.setCorreo(txtCorreo.getText());
                centroAct.setMunicipio(ubicacion.getMunicipio());
                centroAct.setNombre(txtNombre.getText());
                centroAct.setProvincia(ubicacion.getProvincia());
                centroAct.setRnc(rnc);
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
                String rnc = BolsaLaboral.getInstancia().prepararRnc(
                        null, txtRNC.getText().trim());
                CentroEmpleador nuevoCentro = new CentroEmpleador(
                        txtCodigo.getText(), txtNombre.getText(), cmbSector.getSelectedItem().toString(),
                        ubicacion.getProvincia(), ubicacion.getMunicipio(), txtTelefono.getText(),
                        txtCorreo.getText(), rnc);
                BolsaLaboral.getInstancia().registrarCentroTrabajo(nuevoCentro);
                JOptionPane.showMessageDialog(this,
                        "El centro de trabajo ha sido agregado correctamente.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                txtCodigo.setText("CEN-" + BolsaLaboral.genCodigoCentro);
                limpiar();
            }
        } catch (FormatException | IllegalArgumentException | SecurityException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean verificar() throws FormatException {
        if (txtNombre.getText().isEmpty()) {
            throw new FormatException("El nombre no puede estar vacío.");
        } else if (txtRNC.getText().isEmpty()) {
            throw new FormatException("El RNC no puede estar vacío.");
        } else if (!RncValidator.validar(txtRNC.getText()).esValido()
                && !(centroAct != null
                && Objects.equals(centroAct.getRnc(), txtRNC.getText()))) {
            throw new FormatException(RncValidator.validar(txtRNC.getText()).getMensaje());
        } else if (txtTelefono.getText().isEmpty()) {
            throw new FormatException("El teléfono no puede estar vacío.");
        } else if (!txtTelefono.getText().matches("\\d{10}")) {
            throw new FormatException("El teléfono debe tener 10 dígitos.");
        } else if (txtCorreo.getText().isEmpty()) {
            throw new FormatException("El correo no puede estar vacío.");
        } else if (!txtCorreo.getText().contains("@") || !txtCorreo.getText().contains(".")) {
            throw new FormatException("Formato del correo inválido. Ejemplo: usuario@dominio.com");
        }
        try {
            ubicacion.validar();
            BolsaLaboral.getInstancia().prepararRnc(centroAct, txtRNC.getText().trim());
        } catch (IllegalArgumentException exception) {
            throw new FormatException(exception.getMessage());
        }
        return true;
    }

    private void limpiar() {
        txtCorreo.setText("");
        ubicacion.limpiar();
        txtNombre.setText("");
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
            txtNombre.setText(centroAct.getNombre());
            ubicacion.seleccionar(centroAct.getProvincia(), centroAct.getMunicipio());
            txtRNC.setText(centroAct.getRnc());
            txtTelefono.setText(centroAct.getTelefono());
        }
    }

    private void cargarSector() {
        if (cmbSector.getSelectedItem() != null) {
            lblIcono.setIcon(UIUtils.valueIcon(cmbSector.getSelectedItem().toString()));
        }
    }

    private String[] valoresCatalogo(TipoCatalogo tipo, String historico) {
        return BolsaLaboral.getInstancia().getCatalogos()
                .getValoresParaEdicion(tipo, historico).toArray(new String[0]);
    }

    private void advertirLegadosSiCorresponde(String rncPreparado) {
        ResultadoDocumento validacion = RncValidator.validar(rncPreparado);
        if (!validacion.esValido() || ubicacion.esLegada()) {
            JOptionPane.showMessageDialog(this,
                    "Se conservará un valor legado fuera del catálogo o con RNC inválido "
                            + "porque no fue modificado.",
                    "Advertencia de compatibilidad", JOptionPane.WARNING_MESSAGE);
        }
    }
}
