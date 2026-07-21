package visual;

import exception.FormatException;
import logico.BolsaLaboral;
import logico.CentroEmpleador;
import logico.OfertaLaboral;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.ArrayList;

public class RegistroOfertaLaboral extends JDialog {

    private static final String CARD_UNIVERSITARIO = "universitario";
    private static final String CARD_TECNICO = "tecnico";
    private static final String CARD_OBRERO = "obrero";

    private OfertaLaboral ofertaAct;
    private JTextField txtCodigo;
    private JTextField txtPuesto;
    private JRadioButton rdTecnico;
    private JRadioButton rdUniversitario;
    private JRadioButton rdObrero;
    private JLabel lblIcoModalidad;
    private JLabel lblIcoJornada;
    private JComboBox<String> cmbJornada;
    private JComboBox<String> cmbModalidad;
    private JComboBox<String> cmbArea;
    private JLabel lblIcoArea;
    private JPanel pnlTecnico;
    private JPanel pnlCarreras;
    private JPanel pnlObrero;
    private JPanel specializationCards;
    private CardLayout specializationLayout;
    private JComboBox<String> cmbOfertador;
    private JSpinner spnSalario;
    private JTextArea txtDescripcion;
    private JSpinner spnVacantes;
    private JComboBox<String> cmbHabilidad;
    private JCheckBox chckbxIngls;
    private JCheckBox chckbxPortugus;
    private JCheckBox chckbxItaliano;
    private JCheckBox chckbxAlemn;
    private JCheckBox chckbxMandarn;
    private JCheckBox chckbxCoreano;
    private JCheckBox chckbxEspaol;
    private JCheckBox chckbxFrancs;
    private JCheckBox chckbxJapons;
    private JCheckBox chkbxMayor;
    private JCheckBox chkReubicacion;
    private JCheckBox chkLicencia;
    private JCheckBox chkARQ;
    private JCheckBox chkICV;
    private JCheckBox chkIEL;
    private JCheckBox chkDER;
    private JCheckBox chkIST;
    private JCheckBox chkIT;
    private JCheckBox chkMKT;
    private JCheckBox chkCTB;
    private JCheckBox chkCOM;
    private JCheckBox chkMED;
    private JCheckBox chkEDU;
    private JCheckBox chkPSI;
    private JCheckBox chkII;
    private JCheckBox chkHOT;
    private JCheckBox chkNUT;
    private JCheckBox chkDE;
    private JCheckBox chkECO;
    private JCheckBox chkIAG;
    private JCheckBox[] checkIdiomas;
    private JCheckBox[] checkHabilidades;
    private JComboBox<String> cmbAreaTecnica;
    private JSpinner spnAniosExp;
    private JSpinner spnPorcentaje;

    public RegistroOfertaLaboral(JDialog parent, OfertaLaboral oferta) {
        super(parent, true);
        ofertaAct = oferta;
        setTitle(oferta == null ? "Registrar Oferta Laboral" : "Modificar Oferta Laboral");
        setIconImage(UIUtils.image("icono.png"));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIUtils.SURFACE);
        contentPanel.setBorder(UIUtils.emptyBorder(6, 6, 6, 6));
        setContentPane(contentPanel);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Generalidades", scrollTab(buildGeneralPanel()));
        tabs.addTab("Requerimientos", scrollTab(buildRequirementsPanel()));
        tabs.addTab("Laboral", scrollTab(buildWorkConditionsPanel()));
        contentPanel.add(tabs, BorderLayout.CENTER);
        contentPanel.add(buildButtonBar(), BorderLayout.SOUTH);

        cmbArea.setSelectedIndex(0);
        cambiarEspecializacion("Estudiante Universitario");
        cargarDatos();
        UIUtils.finishDialog(this, parent, 740, 680);
    }

    public RegistroOfertaLaboral(OfertaLaboral oferta) {
        this(null, oferta);
    }

    private JScrollPane scrollTab(JPanel panel) {
        panel.setBorder(UIUtils.emptyBorder(8, 8, 8, 8));
        return UIUtils.scrollable(panel);
    }

    private JPanel buildGeneralPanel() {
        JPanel panel = UIUtils.formPanel();
        txtCodigo = textField();
        txtCodigo.setText("OFR-" + BolsaLaboral.genCodigoOferta);
        txtCodigo.setEditable(false);
        UIUtils.addFormRow(panel, 0, "Código:", txtCodigo);
        UIUtils.addFullWidth(panel, new JSeparator(), 1);

        txtPuesto = textField();
        UIUtils.addFormRow(panel, 2, "Puesto:", txtPuesto);

        ArrayList<String> ofertadores = new ArrayList<String>();
        ofertadores.add("<Seleccione un ofertador>");
        for (CentroEmpleador centro : BolsaLaboral.getInstancia().getCentros()) {
            ofertadores.add(centro.getCodigo() + " : " + centro.getNombre());
        }
        cmbOfertador = new JComboBox<String>(ofertadores.toArray(new String[ofertadores.size()]));
        cmbOfertador.setMaximumRowCount(Math.max(1, ofertadores.size()));
        UIUtils.addFormRow(panel, 3, "Ofertador:", cmbOfertador);

        spnSalario = new JSpinner(new SpinnerNumberModel(
                Float.valueOf(12000), Float.valueOf(12000), null, Float.valueOf(1)));
        UIUtils.addFormRow(panel, 4, "Salario:", spnSalario);

        txtDescripcion = new JTextArea(5, 24);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(txtDescripcion);
        descriptionScroll.setPreferredSize(new Dimension(UIUtils.scale(320), UIUtils.scale(110)));
        UIUtils.addFormRow(panel, 5, "Descripción:", descriptionScroll);

        spnVacantes = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
        UIUtils.addFormRow(panel, 6, "Vacantes:", spnVacantes);

        cmbArea = combo(new String[]{"No definido", "Finanzas", "Recursos Humanos", "Marketing",
                "Limpieza", "Seguridad", "TI", "Salud", "Operaciones", "Administración",
                "Atención al Cliente", "Educación"});
        lblIcoArea = new JLabel();
        cmbArea.addActionListener(event -> cargarArea());
        UIUtils.addFormRow(panel, 7, "Área:", cmbArea, lblIcoArea);
        UIUtils.addVerticalFiller(panel, 8);
        return panel;
    }

    private JPanel buildRequirementsPanel() {
        JPanel panel = UIUtils.formPanel();
        JPanel typePanel = UIUtils.titledPanel("Nivel Académico Requerido");
        typePanel.setLayout(new GridLayout(1, 3, UIUtils.scale(8), 0));
        rdUniversitario = new JRadioButton("Estudiante Universitario", true);
        rdTecnico = new JRadioButton("Estudiante Técnico");
        rdObrero = new JRadioButton("Obrero");
        rdUniversitario.addActionListener(event -> cambiarEspecializacion("Estudiante Universitario"));
        rdTecnico.addActionListener(event -> cambiarEspecializacion("Estudiante Tecnico"));
        rdObrero.addActionListener(event -> cambiarEspecializacion("Obrero"));
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
        pnlCarreras = buildCareersPanel();
        pnlTecnico = buildTechnicalPanel();
        pnlObrero = buildWorkerPanel();
        specializationCards.add(pnlCarreras, CARD_UNIVERSITARIO);
        specializationCards.add(pnlTecnico, CARD_TECNICO);
        specializationCards.add(pnlObrero, CARD_OBRERO);
        GridBagConstraints cardConstraints = UIUtils.constraints(0, 1);
        cardConstraints.gridwidth = GridBagConstraints.REMAINDER;
        cardConstraints.weightx = 1;
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(specializationCards, cardConstraints);

        JPanel languages = UIUtils.titledPanel("Idiomas Requeridos");
        languages.setLayout(new GridLayout(3, 3, UIUtils.scale(8), UIUtils.scale(4)));
        chckbxIngls = check("Inglés");
        chckbxPortugus = check("Portugués");
        chckbxItaliano = check("Italiano");
        chckbxAlemn = check("Alemán");
        chckbxMandarn = check("Chino");
        chckbxCoreano = check("Coreano");
        chckbxEspaol = check("Español");
        chckbxFrancs = check("Francés");
        chckbxJapons = check("Japonés");
        JCheckBox[] orderedLanguages = {chckbxIngls, chckbxPortugus, chckbxMandarn,
                chckbxItaliano, chckbxAlemn, chckbxCoreano,
                chckbxEspaol, chckbxFrancs, chckbxJapons};
        for (JCheckBox language : orderedLanguages) {
            languages.add(language);
        }
        GridBagConstraints languageConstraints = UIUtils.constraints(0, 2);
        languageConstraints.gridwidth = GridBagConstraints.REMAINDER;
        languageConstraints.weightx = 1;
        languageConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(languages, languageConstraints);
        UIUtils.addVerticalFiller(panel, 3);
        return panel;
    }

    private JPanel buildCareersPanel() {
        JPanel panel = UIUtils.titledPanel("Carreras Permitidas");
        panel.setLayout(new GridLayout(0, 3, UIUtils.scale(8), UIUtils.scale(4)));
        chkARQ = check("Arquitectura");
        chkICV = check("Ingeniería Civil");
        chkIEL = check("Ingeniería Eléctrica");
        chkDER = check("Derecho");
        chkIST = check("Ingeniería de Sistemas");
        chkIT = check("Ingeniería Telemática");
        chkMKT = check("Marketing");
        chkCTB = check("Contabilidad");
        chkCOM = check("Comunicación");
        chkMED = check("Medicina");
        chkEDU = check("Educación");
        chkPSI = check("Psicología");
        chkII = check("Ingeniería Industrial");
        chkHOT = check("Hotelería");
        chkNUT = check("Nutrición");
        chkDE = check("Dirección Empresarial");
        chkECO = check("Economía");
        chkIAG = check("Ingeniería Agronómica");
        JCheckBox[] careers = {chkARQ, chkICV, chkIEL, chkDER, chkIST, chkIT, chkMKT, chkCTB,
                chkCOM, chkMED, chkEDU, chkPSI, chkII, chkHOT, chkNUT, chkDE, chkECO, chkIAG};
        for (JCheckBox career : careers) {
            panel.add(career);
        }
        return panel;
    }

    private JPanel buildTechnicalPanel() {
        JPanel panel = UIUtils.titledPanel("Requerimientos Técnicos");
        cmbAreaTecnica = combo(new String[]{
                "Gestión de Talento Humano", "Impuestos y Contabilidad", "Publicidad", "Gestión Comercial",
                "Higiene y Seguridad Industrial", "Mantenimiento de Instalaciones", "Protección Civil",
                "Protección Industrial", "Redes de Datos", "Desarrollo de Software", "Logística Industrial",
                "Gestión Empresarial", "Atención Comercial", "Automatización", "Diseño Gráfico",
                "Ciberseguridad", "Robótica", "Medios Digitales"
        });
        spnAniosExp = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        UIUtils.addFormRow(panel, 0, "Área requerida:", cmbAreaTecnica);
        UIUtils.addFormRow(panel, 1, "Años de experiencia:", spnAniosExp);
        UIUtils.addVerticalFiller(panel, 2);
        return panel;
    }

    private JPanel buildWorkerPanel() {
        JPanel panel = UIUtils.titledPanel("Requerimientos del Obrero");
        cmbHabilidad = combo(new String[]{"Plomería", "Carpintería", "Gestión Financiera",
                "Instalación Eléctrica", "Soldadura", "Mecánica", "Albañilería", "Redes Sociales",
                "Conducción", "Reparación de Electrónicos", "Ventas", "Fotografía", "Cocina",
                "Limpieza", "Pintura"});
        UIUtils.addFormRow(panel, 0, "Habilidad requerida:", cmbHabilidad);
        UIUtils.addVerticalFiller(panel, 1);
        return panel;
    }

    private JPanel buildWorkConditionsPanel() {
        JPanel panel = UIUtils.formPanel();
        cmbModalidad = combo(new String[]{"Presencial", "Remoto", "Híbrido"});
        cmbJornada = combo(new String[]{"Tiempo Completo", "Medio Tiempo", "Jornada Nocturna", "Jornada Rotativa"});
        lblIcoModalidad = new JLabel();
        lblIcoJornada = new JLabel();
        cmbModalidad.addActionListener(event -> cargarModalidad());
        cmbJornada.addActionListener(event -> cargarJornada());
        UIUtils.addFormRow(panel, 0, "Modalidad:", cmbModalidad, lblIcoModalidad);
        UIUtils.addFormRow(panel, 1, "Jornada:", cmbJornada, lblIcoJornada);
        spnPorcentaje = new JSpinner(new SpinnerNumberModel(0, 0, 100, 10));
        UIUtils.addFormRow(panel, 2, "Porcentaje Mínimo:", spnPorcentaje);
        UIUtils.addFullWidth(panel, new JSeparator(), 3);
        chkbxMayor = check("");
        chkReubicacion = check("");
        chkLicencia = check("");
        UIUtils.addFormRow(panel, 4, "¿Es obligatorio ser mayor de edad?", chkbxMayor);
        UIUtils.addFormRow(panel, 5, "¿Ofrece reubicación?", chkReubicacion);
        UIUtils.addFormRow(panel, 6, "¿Requiere licencia de conducir?", chkLicencia);
        UIUtils.addVerticalFiller(panel, 7);
        return panel;
    }

    private JPanel buildButtonBar() {
        JPanel bar = UIUtils.buttonBar(UIUtils.TEAL_DARK);
        JButton clearButton = UIUtils.button("Limpiar", "cerrar.png");
        clearButton.setIcon(null);
        clearButton.addActionListener(event -> limpiar());
        JButton confirmButton = UIUtils.button(
                ofertaAct == null ? "Registrar" : "Modificar",
                ofertaAct == null ? "agregarP.png" : "modificar.png");
        confirmButton.addActionListener(event -> confirmar());
        JButton cancelButton = UIUtils.button("Cancelar", "cerrar.png");
        cancelButton.addActionListener(event -> dispose());
        bar.add(clearButton);
        bar.add(confirmButton);
        bar.add(cancelButton);
        getRootPane().setDefaultButton(confirmButton);
        return bar;
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

    private void confirmar() {
        try {
            if (!verificar()) {
                JOptionPane.showMessageDialog(this, "Todos los registros son obligatorios.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ofertaAct != null) {
                modificarOferta();
            } else {
                registrarOferta();
            }
        } catch (FormatException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificarOferta() {
        ofertaAct.setPuesto(txtPuesto.getText());
        ofertaAct.setDescripcion(txtDescripcion.getText());
        ofertaAct.setSalario(((Number) spnSalario.getValue()).floatValue());
        ofertaAct.setVacantes(((Number) spnVacantes.getValue()).intValue());
        ofertaAct.setOfertador(BolsaLaboral.getInstancia().getCentros().get(cmbOfertador.getSelectedIndex() - 1));
        ofertaAct.setArea(cmbArea.getSelectedItem().toString());
        ofertaAct.setModalidad(cmbModalidad.getSelectedItem().toString());
        ofertaAct.setJornada(cmbJornada.getSelectedItem().toString());
        ofertaAct.setObligatorioMayorDeEdad(chkbxMayor.isSelected());
        ofertaAct.setOfreceReubicacion(chkReubicacion.isSelected());
        ofertaAct.setPorcentajeMinimo(((Number) spnPorcentaje.getValue()).intValue());

        for (JCheckBox idioma : checkIdiomas) {
            if (idioma.isSelected()) {
                ofertaAct.agregarIdioma(idioma.getText());
            }
        }
        if (rdUniversitario.isSelected()) {
            ofertaAct.setNivelAcademico(rdUniversitario.getText());
            for (JCheckBox carrera : checkHabilidades) {
                if (carrera.isSelected()) {
                    ofertaAct.agregarRequisito(carrera.getText());
                }
            }
        } else if (rdTecnico.isSelected()) {
            ofertaAct.setNivelAcademico(rdTecnico.getText());
            ofertaAct.setExperienciaMinima(((Number) spnAniosExp.getValue()).intValue());
            ofertaAct.clearRequisitos();
            ofertaAct.agregarRequisito(cmbAreaTecnica.getSelectedItem().toString());
        } else if (rdObrero.isSelected()) {
            ofertaAct.setNivelAcademico(rdObrero.getText());
            ofertaAct.clearRequisitos();
            ofertaAct.agregarRequisito(cmbHabilidad.getSelectedItem().toString());
        }

        if (BolsaLaboral.getInstancia().modificarOfertaLaboral(ofertaAct)) {
            JOptionPane.showMessageDialog(this,
                    "La oferta: " + txtPuesto.getText() + " ha sido modificada exitosamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            ConsultarOfertas.cargarOfertas();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "La oferta " + txtPuesto.getText() + " no logró ser modificada.");
        }
    }

    private void registrarOferta() {
        ArrayList<String> requisitos = new ArrayList<String>();
        ArrayList<String> idiomas = new ArrayList<String>();
        String nivelAcademico = "";
        for (JCheckBox idioma : checkIdiomas) {
            if (idioma.isSelected()) {
                idiomas.add(idioma.getText());
            }
        }
        if (rdUniversitario.isSelected()) {
            nivelAcademico = rdUniversitario.getText();
            for (JCheckBox carrera : checkHabilidades) {
                if (carrera.isSelected()) {
                    requisitos.add(carrera.getText());
                }
            }
        } else if (rdObrero.isSelected()) {
            nivelAcademico = rdObrero.getText();
            requisitos.add(cmbHabilidad.getSelectedItem().toString());
        } else if (rdTecnico.isSelected()) {
            nivelAcademico = rdTecnico.getText();
            requisitos.add(cmbAreaTecnica.getSelectedItem().toString());
        }

        OfertaLaboral nuevaOferta = new OfertaLaboral(
                txtCodigo.getText(), txtPuesto.getText(), txtDescripcion.getText(),
                cmbArea.getSelectedItem().toString(), cmbModalidad.getSelectedItem().toString(),
                cmbJornada.getSelectedItem().toString(), "Activa",
                ((Number) spnSalario.getValue()).floatValue(),
                ((Number) spnAniosExp.getValue()).intValue(),
                ((Number) spnVacantes.getValue()).intValue(),
                BolsaLaboral.getInstancia().getCentros().get(cmbOfertador.getSelectedIndex() - 1),
                chkReubicacion.isSelected(), chkbxMayor.isSelected(), chkLicencia.isSelected(),
                nivelAcademico, requisitos, idiomas, ((Number) spnPorcentaje.getValue()).intValue());
        BolsaLaboral.getInstancia().registrarOfertaLaboral(nuevaOferta);
        JOptionPane.showMessageDialog(this, "La oferta laboral ha sido agregado correctamente.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
        txtCodigo.setText("OFR-" + BolsaLaboral.genCodigoOferta);
        limpiar();
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

    private void cargarArea() {
        if (cmbArea.getSelectedItem() != null) {
            lblIcoArea.setIcon(UIUtils.valueIcon(cmbArea.getSelectedItem().toString()));
        }
    }

    private void cambiarEspecializacion(String especializacion) {
        if (especializacion.equalsIgnoreCase("Obrero")) {
            specializationLayout.show(specializationCards, CARD_OBRERO);
        } else if (especializacion.equalsIgnoreCase("Estudiante Tecnico")) {
            specializationLayout.show(specializationCards, CARD_TECNICO);
        } else {
            specializationLayout.show(specializationCards, CARD_UNIVERSITARIO);
        }
    }

    private boolean idiomaSeleccionado() {
        for (JCheckBox idioma : checkIdiomas) {
            if (idioma.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private boolean carreraSeleccionada() {
        for (JCheckBox carrera : checkHabilidades) {
            if (carrera.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private void cargarDatos() {
        checkIdiomas = new JCheckBox[]{chckbxIngls, chckbxPortugus, chckbxItaliano,
                chckbxAlemn, chckbxMandarn, chckbxCoreano, chckbxEspaol, chckbxFrancs, chckbxJapons};
        checkHabilidades = new JCheckBox[]{chkARQ, chkICV, chkIEL, chkDER, chkIST, chkIT,
                chkMKT, chkCTB, chkCOM, chkMED, chkEDU, chkPSI, chkII, chkHOT, chkNUT,
                chkDE, chkECO, chkIAG};
        if (ofertaAct == null) {
            return;
        }

        txtCodigo.setText(ofertaAct.getCodigo());
        txtPuesto.setText(ofertaAct.getPuesto());
        cmbOfertador.setSelectedIndex(
                BolsaLaboral.getInstancia().buscarIndiceCentroByCodigo(ofertaAct.getOfertador().getCodigo()) + 1);
        spnSalario.setValue(ofertaAct.getSalario());
        txtDescripcion.setText(ofertaAct.getDescripcion());
        spnVacantes.setValue(ofertaAct.getVacantes());
        spnPorcentaje.setValue(ofertaAct.getPorcentajeMinimo());
        cmbArea.setSelectedItem(ofertaAct.getArea());
        cmbModalidad.setSelectedItem(ofertaAct.getModalidad());
        cmbJornada.setSelectedItem(ofertaAct.getJornada());
        cmbAreaTecnica.setSelectedItem(ofertaAct.getArea());
        spnAniosExp.setValue(ofertaAct.getExperienciaMinima());
        chkbxMayor.setSelected(ofertaAct.isObligatorioMayorDeEdad());
        chkReubicacion.setSelected(ofertaAct.isOfreceReubicacion());

        for (JCheckBox idioma : checkIdiomas) {
            idioma.setSelected(ofertaAct.getIdiomasRequeridas().contains(idioma.getText()));
        }
        if (ofertaAct.getNivelAcademico().equals(rdUniversitario.getText())) {
            rdUniversitario.setSelected(true);
            cambiarEspecializacion("Estudiante Universitario");
            for (JCheckBox carrera : checkHabilidades) {
                carrera.setSelected(ofertaAct.getRequisitos().contains(carrera.getText()));
            }
        } else if (ofertaAct.getNivelAcademico().equals(rdObrero.getText())) {
            rdObrero.setSelected(true);
            cambiarEspecializacion("Obrero");
            if (!ofertaAct.getRequisitos().isEmpty()) {
                cmbHabilidad.setSelectedItem(ofertaAct.getRequisitos().get(0));
            }
        } else if (ofertaAct.getNivelAcademico().equals(rdTecnico.getText())) {
            rdTecnico.setSelected(true);
            cambiarEspecializacion("Estudiante Tecnico");
            if (!ofertaAct.getRequisitos().isEmpty()) {
                cmbAreaTecnica.setSelectedItem(ofertaAct.getRequisitos().get(0));
            }
        }
    }

    private void limpiar() {
        txtPuesto.setText("");
        cmbOfertador.setSelectedIndex(0);
        spnSalario.setValue(Integer.valueOf(12000));
        txtDescripcion.setText("");
        spnVacantes.setValue(Integer.valueOf(1));
        cmbArea.setSelectedIndex(0);
        cmbModalidad.setSelectedIndex(0);
        cmbJornada.setSelectedIndex(0);
        cmbHabilidad.setSelectedIndex(0);
        cmbAreaTecnica.setSelectedIndex(0);
        spnAniosExp.setValue(Integer.valueOf(0));
        spnPorcentaje.setValue(Integer.valueOf(0));
        for (JCheckBox idioma : checkIdiomas) {
            idioma.setSelected(false);
        }
        chkbxMayor.setSelected(false);
        chkReubicacion.setSelected(false);
        for (JCheckBox carrera : checkHabilidades) {
            carrera.setSelected(false);
        }
        rdUniversitario.setSelected(true);
        cambiarEspecializacion("Estudiante Universitario");
    }

    private boolean verificar() throws FormatException {
        if (cmbOfertador.getSelectedIndex() == 0) {
            throw new FormatException("Debe seleccionar un centro empleador válido.");
        }
        if (txtPuesto.getText().trim().isEmpty()) {
            throw new FormatException("Debe ingresar un título para el puesto.");
        }
        if (txtDescripcion.getText().trim().isEmpty()) {
            throw new FormatException("Debe ingresar una descripción para la oferta.");
        }
        if (!idiomaSeleccionado()) {
            throw new FormatException("Debe seleccionar por lo menos un idioma requerido.");
        }
        if (rdUniversitario.isSelected() && !carreraSeleccionada()) {
            throw new FormatException("Debe seleccionar por lo menos una carrera permitida.");
        }
        if (cmbArea.getSelectedIndex() == 0) {
            throw new FormatException("Debe seleccionar un area.");
        }
        return true;
    }
}
