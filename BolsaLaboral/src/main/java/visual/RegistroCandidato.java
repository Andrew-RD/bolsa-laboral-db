package visual;

import exception.FormatException;
import logico.BolsaLaboral;
import logico.Candidato;
import logico.ElementoCatalogo;
import logico.Obrero;
import logico.TecnicoSuperior;
import logico.Universitario;
import logico.AutorizacionService;
import logico.CedulaValidator;
import logico.Permiso;
import logico.ResultadoDocumento;
import logico.SituacionAcademica;
import logico.TipoCandidato;
import logico.TipoCatalogo;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RegistroCandidato extends JDialog {

    private static final String CARD_UNIVERSITARIO = "universitario";
    private static final String CARD_TECNICO = "tecnico";
    private static final String CARD_OBRERO = "obrero";

    private JTabbedPane contenedor;
    private Candidato candidatoAct;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtCedula;
    private JTextField txtCorreo;
    private JComboBox<String> cmbProvincia;
    private JComboBox<String> cmbMunicipio;
    private UbicacionComboSupport ubicacion;
    private JRadioButton rdTecnico;
    private JRadioButton rdUniversitario;
    private JRadioButton rdObrero;
    private JSpinner spnFechaNac;
    private JTextField txtTelefono;
    private JComboBox<UniversidadOpcion> cmbUniversidad;
    private JComboBox<SituacionAcademica> cmbSituacionAcademica;
    private JPanel pnlEstudiante;
    private JPanel pnlTecnico;
    private JPanel pnlObrero;
    private JPanel specializationCards;
    private CardLayout specializationLayout;
    private JLabel lblIcoModalidad;
    private JLabel lblIcoJornada;
    private JComboBox<String> cmbJornada;
    private JComboBox<String> cmbModalidad;
    private JLabel lblIcoArea;
    private JComboBox<String> cmbArea;
    private JPanel pnlIdiomas;
    private JSpinner spnSalarioEsperado;
    private JSpinner spnAniosExp;
    private JComboBox<String> cmbCarrera;
    private JComboBox<String> cmbNivel;
    private JCheckBox chkLicenciaConducir;
    private JCheckBox chkMudarse;
    private JCheckBox chckbxIngles;
    private JCheckBox chckbxItaliano;
    private JCheckBox chckbxEspanol;
    private JCheckBox chckbxFrances;
    private JCheckBox chckbxPortugues;
    private JCheckBox chckbxAleman;
    private JCheckBox chckbxCoreano;
    private JCheckBox chckbxJapones;
    private JCheckBox chckbxMandarin;
    private JCheckBox chkPlomeria;
    private JCheckBox chkCarpintero;
    private JCheckBox chkCajero;
    private JCheckBox chkSoldadura;
    private JCheckBox chkElectrica;
    private JCheckBox chkMecanica;
    private JCheckBox chkAlbanileria;
    private JCheckBox chkRedes;
    private JCheckBox chkConduccion;
    private JCheckBox chkReparacion;
    private JCheckBox chkVentas;
    private JCheckBox chkFotografia;
    private JCheckBox chkCocina;
    private JCheckBox chkLimpieza;
    private JCheckBox chkPintura;
    private JComboBox<String> cmbAreaTecnica;
    private JComboBox<String> cmbGenero;
    private JLabel lblEstadoLaboral;

    public RegistroCandidato(Candidato candidato) {
        candidatoAct = candidato;
        setTitle(candidato == null ? "Registrar Candidato" : "Modificar Candidato");
        setIconImage(UIUtils.image("icono.png"));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIUtils.SURFACE);
        contentPanel.setBorder(UIUtils.emptyBorder(6, 6, 6, 6));
        setContentPane(contentPanel);

        contenedor = new JTabbedPane(JTabbedPane.TOP);
        contenedor.addTab("Personal", scrollTab(buildPersonalPanel()));
        contenedor.addTab("Especializaciones", scrollTab(buildSpecializationPanel()));
        contenedor.addTab("Preferencias", scrollTab(buildPreferencesPanel()));
        contentPanel.add(contenedor, BorderLayout.CENTER);
        contentPanel.add(buildButtonBar(), BorderLayout.SOUTH);

        cmbJornada.setSelectedIndex(0);
        cmbArea.setSelectedIndex(0);
        cmbModalidad.setSelectedIndex(0);
        cambiarEspecializacion(TipoCandidato.UNIVERSITARIO);
        cargarDatos();
        UIUtils.finishDialog(this, getOwner(), 720, 680);
    }

    private JScrollPane scrollTab(JPanel panel) {
        panel.setBorder(UIUtils.emptyBorder(8, 8, 8, 8));
        return UIUtils.scrollable(panel);
    }

    private JPanel buildPersonalPanel() {
        JPanel panel = UIUtils.formPanel();
        txtCodigo = textField();
        txtCodigo.setText("CAN-" + BolsaLaboral.genCodigoCandidato);
        txtCodigo.setEditable(false);
        UIUtils.addFormRow(panel, 0, "Código:", txtCodigo);
        UIUtils.addFullWidth(panel, new JSeparator(), 1);

        txtNombre = textField();
        txtApellido = textField();
        txtCedula = textField();
        UIUtils.addFormRow(panel, 2, "Nombres:", txtNombre);
        UIUtils.addFormRow(panel, 3, "Apellidos:", txtApellido);
        UIUtils.addFormRow(panel, 4, "Cédula:", txtCedula);

        Calendar calendar = Calendar.getInstance();
        Date fechaMaxima = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date fechaMinima = calendar.getTime();
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -16);
        Date fechaPorDefecto = calendar.getTime();
        spnFechaNac = new JSpinner(new SpinnerDateModel(
                fechaPorDefecto, fechaMinima, fechaMaxima, Calendar.DAY_OF_MONTH));
        spnFechaNac.setEditor(new JSpinner.DateEditor(spnFechaNac, "dd/MM/yyyy"));
        UIUtils.addFormRow(panel, 5, "Fecha de Nacimiento:", spnFechaNac);

        cmbGenero = combo(new String[]{"Femenino", "Masculino"});
        UIUtils.addFormRow(panel, 6, "Género:", cmbGenero);

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
        GridBagConstraints contactsConstraints = UIUtils.constraints(0, 7);
        contactsConstraints.gridwidth = GridBagConstraints.REMAINDER;
        contactsConstraints.weightx = 1;
        contactsConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(contacts, contactsConstraints);
        UIUtils.addVerticalFiller(panel, 8);
        return panel;
    }

    private JPanel buildSpecializationPanel() {
        JPanel panel = UIUtils.formPanel();

        JPanel typePanel = UIUtils.titledPanel("Tipo de Candidato");
        typePanel.setLayout(new GridLayout(1, 3, UIUtils.scale(8), 0));
        rdUniversitario = new JRadioButton("Universitario / Profesional", true);
        rdTecnico = new JRadioButton("Técnico Superior");
        rdObrero = new JRadioButton("Obrero");
        rdUniversitario.addActionListener(event -> cambiarEspecializacion(TipoCandidato.UNIVERSITARIO));
        rdTecnico.addActionListener(event -> cambiarEspecializacion(TipoCandidato.TECNICO));
        rdObrero.addActionListener(event -> cambiarEspecializacion(TipoCandidato.OBRERO));
        ButtonGroup group = new ButtonGroup();
        group.add(rdUniversitario);
        group.add(rdTecnico);
        group.add(rdObrero);
        typePanel.add(rdUniversitario);
        typePanel.add(rdTecnico);
        typePanel.add(rdObrero);
        UIUtils.addFullWidth(panel, typePanel, 0);

        specializationLayout = new CardLayout();
        specializationCards = new JPanel(specializationLayout);
        specializationCards.setBackground(UIUtils.SURFACE);
        pnlEstudiante = buildUniversityPanel();
        pnlTecnico = buildTechnicalPanel();
        pnlObrero = buildWorkerPanel();
        specializationCards.add(pnlEstudiante, CARD_UNIVERSITARIO);
        specializationCards.add(pnlTecnico, CARD_TECNICO);
        specializationCards.add(pnlObrero, CARD_OBRERO);
        GridBagConstraints cardConstraints = UIUtils.constraints(0, 1);
        cardConstraints.gridwidth = GridBagConstraints.REMAINDER;
        cardConstraints.weightx = 1;
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(specializationCards, cardConstraints);

        pnlIdiomas = UIUtils.titledPanel("Idiomas");
        pnlIdiomas.setLayout(new GridLayout(3, 3, UIUtils.scale(8), UIUtils.scale(4)));
        for (String idioma : valoresCatalogoConHistoricos(TipoCatalogo.IDIOMAS,
                candidatoAct == null ? null : candidatoAct.getIdiomas())) {
            pnlIdiomas.add(check(idioma));
        }
        GridBagConstraints languageConstraints = UIUtils.constraints(0, 2);
        languageConstraints.gridwidth = GridBagConstraints.REMAINDER;
        languageConstraints.weightx = 1;
        languageConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pnlIdiomas, languageConstraints);

        chkLicenciaConducir = check("");
        UIUtils.addFormRow(panel, 3, "¿Tiene licencia de conducir?", chkLicenciaConducir);
        UIUtils.addVerticalFiller(panel, 4);
        return panel;
    }

    private JPanel buildUniversityPanel() {
        JPanel panel = UIUtils.titledPanel("Universitario / Profesional");
        String carreraHistorica = candidatoAct instanceof Universitario
                ? ((Universitario) candidatoAct).getCarrera() : null;
        String universidadHistorica = candidatoAct instanceof Universitario
                ? ((Universitario) candidatoAct).getUniversidad() : null;
        cmbCarrera = combo(valoresCatalogo(TipoCatalogo.CARRERAS, carreraHistorica));
        cmbUniversidad = new JComboBox<UniversidadOpcion>(
                opcionesUniversidad().toArray(new UniversidadOpcion[0]));
        cmbUniversidad.setMaximumRowCount(
                Math.min(12, cmbUniversidad.getItemCount()));
        cmbNivel = combo(new String[]{"Grado", "Postgrado", "Doctorado"});
        cmbSituacionAcademica = new JComboBox<SituacionAcademica>(situacionesDisponibles());
        UIUtils.addFormRow(panel, 0, "Carrera:", cmbCarrera);
        UIUtils.addFormRow(panel, 1, "Universidad:", cmbUniversidad);
        UIUtils.addFormRow(panel, 2, "Nivel Académico:", cmbNivel);
        UIUtils.addFormRow(panel, 3, "Situación académica:", cmbSituacionAcademica);
        return panel;
    }

    private JPanel buildTechnicalPanel() {
        JPanel panel = UIUtils.titledPanel("Técnico Superior");
        String historica = candidatoAct instanceof TecnicoSuperior
                ? ((TecnicoSuperior) candidatoAct).getAreaTecnica() : null;
        cmbAreaTecnica = combo(valoresCatalogo(TipoCatalogo.AREAS_TECNICAS, historica));
        spnAniosExp = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        UIUtils.addFormRow(panel, 0, "Área Técnica:", cmbAreaTecnica);
        UIUtils.addFormRow(panel, 1, "Años de Experiencia:", spnAniosExp);
        return panel;
    }

    private JPanel buildWorkerPanel() {
        JPanel panel = UIUtils.titledPanel("Habilidades del Obrero");
        panel.setLayout(new GridLayout(0, 3, UIUtils.scale(8), UIUtils.scale(4)));
        Iterable<String> historicas = candidatoAct instanceof Obrero
                ? ((Obrero) candidatoAct).getHabilidades() : null;
        for (String habilidad : valoresCatalogoConHistoricos(TipoCatalogo.HABILIDADES, historicas)) {
            panel.add(check(habilidad));
        }
        return panel;
    }

    private JPanel buildPreferencesPanel() {
        JPanel panel = UIUtils.formPanel();
        cmbModalidad = combo(new String[]{"Presencial", "Remoto", "Híbrido"});
        cmbJornada = combo(new String[]{"Tiempo Completo", "Medio Tiempo", "Jornada Nocturna", "Jornada Rotativa"});
        cmbArea = combo(valoresCatalogo(TipoCatalogo.AREAS_LABORALES,
                candidatoAct == null ? null : candidatoAct.getAreaDeInteres()));
        lblIcoModalidad = new JLabel();
        lblIcoJornada = new JLabel();
        lblIcoArea = new JLabel();
        cmbModalidad.addActionListener(event -> cargarModalidad());
        cmbJornada.addActionListener(event -> cargarJornada());
        cmbArea.addActionListener(event -> cargarArea());
        UIUtils.addFormRow(panel, 0, "Modalidad:", cmbModalidad, lblIcoModalidad);
        UIUtils.addFormRow(panel, 1, "Jornada:", cmbJornada, lblIcoJornada);
        UIUtils.addFormRow(panel, 2, "Área:", cmbArea, lblIcoArea);
        UIUtils.addFullWidth(panel, new JSeparator(), 3);

        spnSalarioEsperado = new JSpinner(new SpinnerNumberModel(
                Float.valueOf(12000), Float.valueOf(12000), null, Float.valueOf(1000)));
        UIUtils.addFormRow(panel, 4, "Salario Esperado:", spnSalarioEsperado);
        chkMudarse = check("");
        UIUtils.addFormRow(panel, 5, "¿Estaría dispuesto a mudarse si es requerido?", chkMudarse);
        lblEstadoLaboral = new JLabel(Candidato.descripcionEstadoLaboral(
                Candidato.ESTADO_DESEMPLEADO));
        lblEstadoLaboral.setToolTipText(
                "El estado laboral no se modifica manualmente.");
        UIUtils.addFormRow(panel, 6, "Estado laboral:", lblEstadoLaboral);
        UIUtils.addVerticalFiller(panel, 7);
        return panel;
    }

    private JPanel buildButtonBar() {
        JPanel buttonBar = UIUtils.buttonBar(UIUtils.CANDIDATE_GREEN);
        JButton clearButton = UIUtils.button("Limpiar", "cerrar.png");
        clearButton.setIcon(null);
        clearButton.addActionListener(event -> limpiar());
        JButton confirmButton = UIUtils.button(
                candidatoAct == null ? "Registrar" : "Modificar",
                candidatoAct == null ? "agregarP.png" : "modificar.png");
        confirmButton.addActionListener(event -> {
            try {
                if (verificar()) {
                    registrarCandidato();
                }
            } catch (FormatException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(),
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });
        JButton cancelButton = UIUtils.button("Cancelar", "cerrar.png");
        cancelButton.addActionListener(event -> dispose());
        buttonBar.add(clearButton);
        buttonBar.add(confirmButton);
        buttonBar.add(cancelButton);
        getRootPane().setDefaultButton(confirmButton);
        return buttonBar;
    }

    private JTextField textField() {
        return new JTextField(24);
    }

    private JComboBox<String> combo(String[] values) {
        JComboBox<String> comboBox = new JComboBox<String>(values);
        comboBox.setMaximumRowCount(Math.min(12, values.length));
        return comboBox;
    }

    private JCheckBox check(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBackground(UIUtils.SURFACE);
        return checkBox;
    }

    private void cambiarEspecializacion(TipoCandidato especializacion) {
        if (especializacion == TipoCandidato.OBRERO) {
            specializationLayout.show(specializationCards, CARD_OBRERO);
        } else if (especializacion == TipoCandidato.TECNICO) {
            specializationLayout.show(specializationCards, CARD_TECNICO);
        } else {
            specializationLayout.show(specializationCards, CARD_UNIVERSITARIO);
        }
    }

    private void cargarArea() {
        if (cmbArea.getSelectedItem() != null) {
            lblIcoArea.setIcon(UIUtils.valueIcon(cmbArea.getSelectedItem().toString()));
        }
    }

    private void cargarJornada() {
        if (cmbJornada.getSelectedItem() != null) {
            lblIcoJornada.setIcon(UIUtils.valueIcon(cmbJornada.getSelectedItem().toString()));
        }
    }

    private void cargarModalidad() {
        if (cmbModalidad.getSelectedItem() != null) {
            lblIcoModalidad.setIcon(UIUtils.valueIcon(cmbModalidad.getSelectedItem().toString()));
        }
    }

    private void limpiar() {
        txtCodigo.setText("CAN-" + BolsaLaboral.genCodigoCandidato);
        txtNombre.setText("");
        txtApellido.setText("");
        txtCedula.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
        ubicacion.limpiar();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -25);
        spnFechaNac.setValue(calendar.getTime());
        if (cmbUniversidad.getItemCount() > 0) {
            cmbUniversidad.setSelectedIndex(0);
        }
        cmbCarrera.setSelectedIndex(0);
        cmbNivel.setSelectedIndex(0);
        cmbSituacionAcademica.setSelectedItem(SituacionAcademica.ESTUDIANTE);
        cmbAreaTecnica.setSelectedIndex(0);
        spnAniosExp.setValue(Integer.valueOf(0));
        rdUniversitario.setSelected(true);
        cambiarEspecializacion(TipoCandidato.UNIVERSITARIO);

        limpiarChecks(pnlObrero);
        limpiarChecks(pnlIdiomas);
        chkLicenciaConducir.setSelected(false);
        chkMudarse.setSelected(false);
        cmbModalidad.setSelectedIndex(0);
        cmbJornada.setSelectedIndex(0);
        cmbArea.setSelectedIndex(0);
        cmbGenero.setSelectedIndex(0);
        lblEstadoLaboral.setText(Candidato.descripcionEstadoLaboral(
                Candidato.ESTADO_DESEMPLEADO));
        spnSalarioEsperado.setValue(Float.valueOf(12000));
    }

    private void registrarCandidato() {
        try {
            AutorizacionService.exigirPermiso(BolsaLaboral.getInstancia().getUsuarioActual(),
                    Permiso.GESTIONAR_CANDIDATOS);
            String codigo = txtCodigo.getText();
            String nombres = txtNombre.getText().trim();
            String apellidos = txtApellido.getText().trim();
            String cedula = BolsaLaboral.getInstancia().prepararCedula(
                    candidatoAct, txtCedula.getText().trim());
            String correo = txtCorreo.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String provincia = ubicacion.getProvincia();
            String municipio = ubicacion.getMunicipio();
            Date fechaNacSpinner = (Date) spnFechaNac.getValue();
            LocalDate fechaNacimiento = fechaNacSpinner.toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String modalidad = cmbModalidad.getSelectedItem().toString();
            String jornada = cmbJornada.getSelectedItem().toString();
            String areaInteres = cmbArea.getSelectedItem().toString();
            String genero = cmbGenero.getSelectedItem().toString();
            String estadoLaboral = candidatoAct == null
                    ? Candidato.ESTADO_DESEMPLEADO : candidatoAct.getEstado();
            float salarioEsperado = ((Number) spnSalarioEsperado.getValue()).floatValue();
            boolean licenciaConducir = chkLicenciaConducir.isSelected();
            boolean mudarse = chkMudarse.isSelected();

            ArrayList<String> idiomas = new ArrayList<String>();
            for (Component component : pnlIdiomas.getComponents()) {
                if (component instanceof JCheckBox && ((JCheckBox) component).isSelected()) {
                    idiomas.add(((JCheckBox) component).getText());
                }
            }

            Candidato nuevoCandidato = null;
            if (rdUniversitario.isSelected()) {
                UniversidadOpcion universidadSeleccionada =
                        (UniversidadOpcion) cmbUniversidad.getSelectedItem();
                Universitario nuevoUniversitario = new Universitario(
                        codigo, cedula, nombres, apellidos,
                        fechaNacimiento, genero, provincia, municipio, telefono, correo, jornada,
                        modalidad, areaInteres, salarioEsperado, licenciaConducir, mudarse,
                        idiomas, universidadSeleccionada.getValorPersistido(),
                        cmbCarrera.getSelectedItem().toString(), cmbNivel.getSelectedItem().toString(),
                        (SituacionAcademica) cmbSituacionAcademica.getSelectedItem(), estadoLaboral);
                if (universidadSeleccionada.getElemento() != null) {
                    nuevoUniversitario.setUniversidadCatalogo(
                            universidadSeleccionada.getElemento());
                }
                nuevoCandidato = nuevoUniversitario;
            } else if (rdTecnico.isSelected()) {
                nuevoCandidato = new TecnicoSuperior(codigo, cedula, nombres, apellidos,
                        fechaNacimiento, genero, provincia, municipio, telefono, correo, jornada,
                        modalidad, areaInteres, salarioEsperado, licenciaConducir, mudarse,
                        idiomas, cmbAreaTecnica.getSelectedItem().toString(),
                        ((Number) spnAniosExp.getValue()).intValue(), estadoLaboral);
            } else if (rdObrero.isSelected()) {
                ArrayList<String> habilidades = new ArrayList<String>();
                for (Component component : pnlObrero.getComponents()) {
                    if (component instanceof JCheckBox && ((JCheckBox) component).isSelected()) {
                        habilidades.add(((JCheckBox) component).getText());
                    }
                }
                nuevoCandidato = new Obrero(codigo, cedula, nombres, apellidos,
                        fechaNacimiento, genero, provincia, municipio, telefono, correo, jornada,
                        modalidad, areaInteres, salarioEsperado, licenciaConducir, mudarse,
                        idiomas, habilidades, estadoLaboral);
            }

            if (nuevoCandidato == null) {
                return;
            }
            if (candidatoAct == null) {
                BolsaLaboral.getInstancia().registrarCandidato(nuevoCandidato);
                JOptionPane.showMessageDialog(this, "Candidato registrado exitosamente",
                        "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                limpiar();
                contenedor.setSelectedIndex(0);
            } else {
                candidatoAct.setNombres(nuevoCandidato.getNombres());
                candidatoAct.setApellidos(nuevoCandidato.getApellidos());
                candidatoAct.setIdentificacion(nuevoCandidato.getIdentificacion());
                candidatoAct.setCorreo(nuevoCandidato.getCorreo());
                candidatoAct.setTelefono(nuevoCandidato.getTelefono());
                candidatoAct.setProvincia(nuevoCandidato.getProvincia());
                candidatoAct.setMunicipio(nuevoCandidato.getMunicipio());
                candidatoAct.setFechaNacimiento(nuevoCandidato.getFechaNacimiento());
                candidatoAct.setGenero(nuevoCandidato.getGenero());
                candidatoAct.setJornada(nuevoCandidato.getJornada());
                candidatoAct.setModalidad(nuevoCandidato.getModalidad());
                candidatoAct.setAreaDeInteres(nuevoCandidato.getAreaDeInteres());
                candidatoAct.setAspiracionSalarial(nuevoCandidato.getAspiracionSalarial());
                candidatoAct.setLicenciaConducir(nuevoCandidato.isLicenciaConducir());
                candidatoAct.setDisposicionMudarse(nuevoCandidato.isDisposicionMudarse());
                candidatoAct.setIdiomas(nuevoCandidato.getIdiomas());

                if (candidatoAct instanceof Universitario && nuevoCandidato instanceof Universitario) {
                    Universitario universitarioActual = (Universitario) candidatoAct;
                    Universitario universitarioNuevo = (Universitario) nuevoCandidato;
                    ElementoCatalogo universidadCatalogo = BolsaLaboral.getInstancia()
                            .getCatalogos().buscarPorIdentificador(
                                    TipoCatalogo.UNIVERSIDADES,
                                    universitarioNuevo.getUniversidadIdentificador());
                    if (universidadCatalogo == null) {
                        universitarioActual.setUniversidadLegada(
                                universitarioNuevo.getUniversidad());
                    } else {
                        universitarioActual.setUniversidadCatalogo(
                                universidadCatalogo);
                    }
                    ((Universitario) candidatoAct).setCarrera(((Universitario) nuevoCandidato).getCarrera());
                    ((Universitario) candidatoAct).setNivelAcademico(((Universitario) nuevoCandidato).getNivelAcademico());
                    ((Universitario) candidatoAct).setSituacionAcademica(
                            ((Universitario) nuevoCandidato).getSituacionAcademica());
                } else if (candidatoAct instanceof TecnicoSuperior && nuevoCandidato instanceof TecnicoSuperior) {
                    ((TecnicoSuperior) candidatoAct).setAreaTecnica(((TecnicoSuperior) nuevoCandidato).getAreaTecnica());
                    ((TecnicoSuperior) candidatoAct).setAniosExperiencia(((TecnicoSuperior) nuevoCandidato).getAniosExperiencia());
                } else if (candidatoAct instanceof Obrero && nuevoCandidato instanceof Obrero) {
                    ((Obrero) candidatoAct).setHabilidades(((Obrero) nuevoCandidato).getHabilidades());
                }
                BolsaLaboral.getInstancia().modificarCandidato(candidatoAct);
                advertirLegadosSiCorresponde(cedula);
                JOptionPane.showMessageDialog(this, "Candidato modificado exitosamente",
                        "Modificación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                ConsultarCandidatos.cargarCandidatos();
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Error al procesar los datos: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        if (candidatoAct == null) {
            rdUniversitario.setEnabled(true);
            rdTecnico.setEnabled(true);
            rdObrero.setEnabled(true);
            return;
        }

        txtCodigo.setText(candidatoAct.getCodigo());
        txtNombre.setText(candidatoAct.getNombres());
        txtApellido.setText(candidatoAct.getApellidos());
        txtCedula.setText(candidatoAct.getIdentificacion());
        txtCorreo.setText(candidatoAct.getCorreo());
        txtTelefono.setText(candidatoAct.getTelefono());
        ubicacion.seleccionar(candidatoAct.getProvincia(), candidatoAct.getMunicipio());
        spnFechaNac.setValue(Date.from(candidatoAct.getFechaNacimiento()
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        cmbModalidad.setSelectedItem(candidatoAct.getModalidad());
        cmbJornada.setSelectedItem(candidatoAct.getJornada());
        cmbArea.setSelectedItem(candidatoAct.getAreaDeInteres());
        cmbGenero.setSelectedItem(candidatoAct.getGenero());
        lblEstadoLaboral.setText(candidatoAct.getDescripcionEstadoLaboral());
        spnSalarioEsperado.setValue(candidatoAct.getAspiracionSalarial());
        chkLicenciaConducir.setSelected(candidatoAct.isLicenciaConducir());
        chkMudarse.setSelected(candidatoAct.isDisposicionMudarse());

        for (String idioma : candidatoAct.getIdiomas()) {
            setSelectedByText(pnlIdiomas, idioma);
        }

        if (candidatoAct instanceof Universitario) {
            rdUniversitario.setSelected(true);
            rdTecnico.setEnabled(false);
            rdObrero.setEnabled(false);
            cambiarEspecializacion(TipoCandidato.UNIVERSITARIO);
            Universitario universitario = (Universitario) candidatoAct;
            seleccionarUniversidad(universitario);
            cmbCarrera.setSelectedItem(universitario.getCarrera());
            cmbNivel.setSelectedItem(universitario.getNivelAcademico());
            cmbSituacionAcademica.setSelectedItem(universitario.getSituacionAcademica());
        } else if (candidatoAct instanceof TecnicoSuperior) {
            rdTecnico.setSelected(true);
            rdUniversitario.setEnabled(false);
            rdObrero.setEnabled(false);
            cambiarEspecializacion(TipoCandidato.TECNICO);
            TecnicoSuperior tecnico = (TecnicoSuperior) candidatoAct;
            cmbAreaTecnica.setSelectedItem(tecnico.getAreaTecnica());
            spnAniosExp.setValue(tecnico.getAniosExperiencia());
        } else if (candidatoAct instanceof Obrero) {
            rdObrero.setSelected(true);
            rdUniversitario.setEnabled(false);
            rdTecnico.setEnabled(false);
            cambiarEspecializacion(TipoCandidato.OBRERO);
            for (String habilidad : ((Obrero) candidatoAct).getHabilidades()) {
                setSelectedByText(pnlObrero, habilidad);
            }
        }
    }

    private void setSelectedByText(JPanel panel, String text) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JCheckBox && ((JCheckBox) component).getText().equals(text)) {
                ((JCheckBox) component).setSelected(true);
            }
        }
    }

    private boolean verificar() throws FormatException {
        if (txtNombre.getText().trim().isEmpty()) {
            throw new FormatException("El nombre es obligatorio");
        }
        if (txtApellido.getText().trim().isEmpty()) {
            throw new FormatException("El apellido es obligatoria");
        }
        ResultadoDocumento cedula = CedulaValidator.validar(txtCedula.getText());
        boolean legadoSinCambios = candidatoAct != null
                && java.util.Objects.equals(candidatoAct.getIdentificacion(), txtCedula.getText());
        if (!cedula.esValido() && !legadoSinCambios) {
            throw new FormatException(cedula.getMensaje());
        }
        try {
            BolsaLaboral.getInstancia().prepararCedula(
                    candidatoAct, txtCedula.getText().trim());
        } catch (IllegalArgumentException exception) {
            throw new FormatException(exception.getMessage());
        }

        Date fechaNacimiento = (Date) spnFechaNac.getValue();
        Calendar calendar = Calendar.getInstance();
        Date fechaActual = calendar.getTime();
        if (fechaNacimiento.after(fechaActual)) {
            throw new FormatException("La fecha de nacimiento no puede ser futura");
        }
        calendar.add(Calendar.YEAR, -16);
        if (fechaNacimiento.after(calendar.getTime())) {
            throw new FormatException("El candidato debe tener al menos 16 años");
        }
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -100);
        if (fechaNacimiento.before(calendar.getTime())) {
            throw new FormatException("La fecha de nacimiento no puede ser anterior a "
                    + (Calendar.getInstance().get(Calendar.YEAR) - 100));
        }
        if (cmbGenero.getSelectedItem() == null || cmbGenero.getSelectedItem().toString().trim().isEmpty()) {
            throw new FormatException("El género es obligatoria");
        }
        if (txtTelefono.getText().trim().isEmpty()) {
            throw new FormatException("El teléfono es obligatorio");
        }
        if (txtCorreo.getText().trim().isEmpty()) {
            throw new FormatException("El correo es obligatorio");
        }
        try {
            ubicacion.validar();
        } catch (IllegalArgumentException exception) {
            throw new FormatException(exception.getMessage());
        }
        if (!txtCorreo.getText().contains("@") || !txtCorreo.getText().contains(".")) {
            throw new FormatException("Formato del correo inválido. Ejemplo: usuario@dominio.com\"");
        }
        String telefono = txtTelefono.getText().trim().replaceAll("[^0-9]", "");
        if (telefono.length() != 10) {
            throw new FormatException("El teléfono debe tener 10 dígitos");
        }
        if (rdUniversitario.isSelected() && cmbUniversidad.getSelectedItem() == null) {
            throw new FormatException("La universidad es obligatoria para universitarios y profesionales");
        } else if (rdUniversitario.isSelected()
                && cmbSituacionAcademica.getSelectedItem() == SituacionAcademica.NO_ESPECIFICADO
                && candidatoAct == null) {
            throw new FormatException(
                    "Debe seleccionar una situación académica para el registro nuevo");
        } else if (rdTecnico.isSelected() && cmbAreaTecnica.getSelectedIndex() < 0) {
            throw new FormatException("El área técnica es obligatoria para técnicos superiores");
        } else if (rdObrero.isSelected()) {
            boolean tieneHabilidad = false;
            for (Component component : pnlObrero.getComponents()) {
                if (component instanceof JCheckBox && ((JCheckBox) component).isSelected()) {
                    tieneHabilidad = true;
                }
            }
            if (!tieneHabilidad) {
                throw new FormatException("Debe seleccionar al menos una habilidad para obreros");
            }
        }
        if (cmbModalidad.getSelectedItem() == null
                || cmbModalidad.getSelectedItem().toString().trim().isEmpty()) {
            throw new FormatException("La modalidad es obligatoria");
        }
        if (cmbArea.getSelectedItem() == null || cmbArea.getSelectedItem().toString().trim().isEmpty()) {
            throw new FormatException("Debe seleccionar un área");
        }
        if (cmbJornada.getSelectedItem() == null
                || cmbJornada.getSelectedItem().toString().trim().isEmpty()) {
            throw new FormatException("La jornada es obligatoria");
        }

        boolean tieneIdioma = false;
        for (Component component : pnlIdiomas.getComponents()) {
            if (component instanceof JCheckBox && ((JCheckBox) component).isSelected()) {
                tieneIdioma = true;
                break;
            }
        }
        if (!tieneIdioma) {
            throw new FormatException("Debe seleccionar al menos un idioma");
        }
        if (((Number) spnSalarioEsperado.getValue()).floatValue() < 12000) {
            throw new FormatException("El salario esperado debe ser al menos 12,000");
        }
        return true;
    }

    private String[] valoresCatalogo(TipoCatalogo tipo, String historico) {
        return BolsaLaboral.getInstancia().getCatalogos()
                .getValoresParaEdicion(tipo, historico).toArray(new String[0]);
    }

    private java.util.List<UniversidadOpcion> opcionesUniversidad() {
        ArrayList<UniversidadOpcion> opciones =
                new ArrayList<UniversidadOpcion>();
        Universitario universitario = candidatoAct instanceof Universitario
                ? (Universitario) candidatoAct : null;
        String identificador = universitario == null
                ? null : universitario.getUniversidadIdentificador();
        String historica = universitario == null
                ? null : universitario.getUniversidad();
        for (ElementoCatalogo elemento : BolsaLaboral.getInstancia()
                .getCatalogos().getUniversidadesParaEdicion(
                        identificador, historica)) {
            opciones.add(new UniversidadOpcion(elemento, null));
        }
        if (universitario != null
                && BolsaLaboral.getInstancia().getCatalogos()
                        .buscarPorIdentificador(TipoCatalogo.UNIVERSIDADES,
                                identificador) == null
                && BolsaLaboral.getInstancia().getCatalogos()
                        .buscarUniversidad(historica) == null
                && historica != null && !historica.trim().isEmpty()) {
            opciones.add(0, new UniversidadOpcion(null, historica));
        }
        return opciones;
    }

    private void seleccionarUniversidad(Universitario universitario) {
        String identificador = universitario.getUniversidadIdentificador();
        String texto = logico.TextoNormalizer.normalizar(
                universitario.getUniversidad());
        for (int index = 0; index < cmbUniversidad.getItemCount(); index++) {
            UniversidadOpcion opcion = cmbUniversidad.getItemAt(index);
            if ((identificador != null
                    && identificador.equals(opcion.getIdentificador()))
                    || logico.TextoNormalizer.normalizar(
                            opcion.getValorPersistido()).equals(texto)
                    || (opcion.getElemento() != null
                    && (logico.TextoNormalizer.normalizar(
                            opcion.getElemento().getSiglas()).equals(texto)
                    || logico.TextoNormalizer.normalizar(
                            opcion.getElemento().getNombreMostrado()).equals(texto)))) {
                cmbUniversidad.setSelectedIndex(index);
                return;
            }
        }
    }

    private java.util.List<String> valoresCatalogoConHistoricos(
            TipoCatalogo tipo, Iterable<String> historicos) {
        ArrayList<String> valores = new ArrayList<String>(
                BolsaLaboral.getInstancia().getCatalogos().getValoresActivos(tipo));
        if (historicos != null) {
            for (String historico : historicos) {
                boolean existe = false;
                for (String valor : valores) {
                    if (logico.TextoNormalizer.normalizar(valor).equals(
                            logico.TextoNormalizer.normalizar(historico))) {
                        existe = true;
                        break;
                    }
                }
                if (!existe && historico != null && !historico.trim().isEmpty()) {
                    valores.add(historico);
                }
            }
        }
        return valores;
    }

    private SituacionAcademica[] situacionesDisponibles() {
        if (candidatoAct instanceof Universitario
                && ((Universitario) candidatoAct).getSituacionAcademica()
                        == SituacionAcademica.NO_ESPECIFICADO) {
            return SituacionAcademica.values();
        }
        return new SituacionAcademica[]{SituacionAcademica.ESTUDIANTE,
                SituacionAcademica.EGRESADO, SituacionAcademica.GRADUADO};
    }

    private void limpiarChecks(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected(false);
            }
        }
    }

    private void advertirLegadosSiCorresponde(String cedulaPreparada) {
        if (!CedulaValidator.validar(cedulaPreparada).esValido() || ubicacion.esLegada()) {
            JOptionPane.showMessageDialog(this,
                    "Se conservó un dato legado inválido o fuera del catálogo porque no fue modificado.",
                    "Advertencia de compatibilidad", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static final class UniversidadOpcion {

        private final ElementoCatalogo elemento;
        private final String valorLegado;

        private UniversidadOpcion(
                ElementoCatalogo elemento, String valorLegado) {
            this.elemento = elemento;
            this.valorLegado = valorLegado;
        }

        private ElementoCatalogo getElemento() {
            return elemento;
        }

        private String getIdentificador() {
            return elemento == null ? null : elemento.getIdentificador();
        }

        private String getValorPersistido() {
            return elemento == null
                    ? valorLegado : elemento.getNombreCompleto();
        }

        @Override
        public String toString() {
            return elemento == null
                    ? valorLegado : elemento.getNombreMostrado();
        }
    }
}
