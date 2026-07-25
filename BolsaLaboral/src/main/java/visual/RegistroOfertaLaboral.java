package visual;

import exception.FormatException;
import logico.BolsaLaboral;
import logico.CentroEmpleador;
import logico.OfertaLaboral;
import logico.AutorizacionService;
import logico.Permiso;
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
    private JTextField txtVacantesOcupadas;
    private JTextField txtVacantesDisponibles;
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
    private final ArrayList<CentroEmpleador> centrosSeleccionables =
            new ArrayList<CentroEmpleador>();

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
        cambiarEspecializacion(TipoCandidato.UNIVERSITARIO);
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
            if (centro != null) {
                centrosSeleccionables.add(centro);
                ofertadores.add(centro.getCodigo() + " : " + centro.getNombre());
            }
        }
        if (ofertaAct != null && ofertaAct.getOfertador() != null
                && indiceCentro(ofertaAct.getOfertador()) < 0) {
            centrosSeleccionables.add(ofertaAct.getOfertador());
            ofertadores.add(ofertaAct.getOfertador().getCodigo() + " : "
                    + ofertaAct.getOfertador().getNombre() + " (legado)");
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

        spnVacantes = new JSpinner(new SpinnerNumberModel(1, 0, null, 1));
        UIUtils.addFormRow(panel, 6, "Vacantes totales:", spnVacantes);
        txtVacantesOcupadas = textField();
        txtVacantesOcupadas.setEditable(false);
        txtVacantesDisponibles = textField();
        txtVacantesDisponibles.setEditable(false);
        UIUtils.addFormRow(panel, 7, "Vacantes ocupadas:", txtVacantesOcupadas);
        UIUtils.addFormRow(panel, 8, "Vacantes disponibles:", txtVacantesDisponibles);
        spnVacantes.addChangeListener(event -> actualizarResumenVacantes());

        cmbArea = combo(valoresCatalogo(TipoCatalogo.AREAS_LABORALES,
                ofertaAct == null ? null : ofertaAct.getArea()));
        lblIcoArea = new JLabel();
        cmbArea.addActionListener(event -> cargarArea());
        UIUtils.addFormRow(panel, 9, "Área:", cmbArea, lblIcoArea);
        UIUtils.addVerticalFiller(panel, 10);
        return panel;
    }

    private JPanel buildRequirementsPanel() {
        JPanel panel = UIUtils.formPanel();
        JPanel typePanel = UIUtils.titledPanel("Nivel Académico Requerido");
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
        for (String idioma : valoresCatalogoConHistoricos(TipoCatalogo.IDIOMAS,
                ofertaAct == null ? null : ofertaAct.getIdiomasRequeridas())) {
            languages.add(check(idioma));
        }
        checkIdiomas = checksDe(languages);
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
        Iterable<String> historicas = ofertaAct != null
                && ofertaAct.getTipoCandidatoRequerido() == TipoCandidato.UNIVERSITARIO
                ? ofertaAct.getRequisitos() : null;
        for (String carrera : valoresCatalogoConHistoricos(TipoCatalogo.CARRERAS, historicas)) {
            panel.add(check(carrera));
        }
        checkHabilidades = checksDe(panel);
        return panel;
    }

    private JPanel buildTechnicalPanel() {
        JPanel panel = UIUtils.titledPanel("Requerimientos Técnicos");
        cmbAreaTecnica = combo(valoresCatalogo(TipoCatalogo.AREAS_TECNICAS,
                requisitoHistorico(TipoCandidato.TECNICO)));
        spnAniosExp = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        UIUtils.addFormRow(panel, 0, "Área requerida:", cmbAreaTecnica);
        UIUtils.addFormRow(panel, 1, "Años de experiencia:", spnAniosExp);
        UIUtils.addVerticalFiller(panel, 2);
        return panel;
    }

    private JPanel buildWorkerPanel() {
        JPanel panel = UIUtils.titledPanel("Requerimientos del Obrero");
        cmbHabilidad = combo(valoresCatalogo(TipoCatalogo.HABILIDADES,
                requisitoHistorico(TipoCandidato.OBRERO)));
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
        } catch (FormatException | IllegalArgumentException | SecurityException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificarOferta() {
        AutorizacionService.exigirPermiso(BolsaLaboral.getInstancia().getUsuarioActual(),
                Permiso.GESTIONAR_OFERTAS);
        ofertaAct.setVacantesTotales(((Number) spnVacantes.getValue()).intValue());
        ofertaAct.setPuesto(txtPuesto.getText());
        ofertaAct.setDescripcion(txtDescripcion.getText());
        ofertaAct.setSalario(((Number) spnSalario.getValue()).floatValue());
        ofertaAct.setOfertador(getCentroSeleccionado());
        ofertaAct.setArea(cmbArea.getSelectedItem().toString());
        ofertaAct.setModalidad(cmbModalidad.getSelectedItem().toString());
        ofertaAct.setJornada(cmbJornada.getSelectedItem().toString());
        ofertaAct.setObligatorioMayorDeEdad(chkbxMayor.isSelected());
        ofertaAct.setOfreceReubicacion(chkReubicacion.isSelected());
        ofertaAct.setobligatorioLicencia(chkLicencia.isSelected());
        ofertaAct.setPorcentajeMinimo(((Number) spnPorcentaje.getValue()).intValue());

        ofertaAct.getIdiomasRequeridas().clear();
        ofertaAct.clearRequisitos();
        for (JCheckBox idioma : checkIdiomas) {
            if (idioma.isSelected()) {
                ofertaAct.agregarIdioma(idioma.getText());
            }
        }
        if (rdUniversitario.isSelected()) {
            ofertaAct.setTipoCandidatoRequerido(TipoCandidato.UNIVERSITARIO);
            for (JCheckBox carrera : checkHabilidades) {
                if (carrera.isSelected()) {
                    ofertaAct.agregarRequisito(carrera.getText());
                }
            }
        } else if (rdTecnico.isSelected()) {
            ofertaAct.setTipoCandidatoRequerido(TipoCandidato.TECNICO);
            ofertaAct.setExperienciaMinima(((Number) spnAniosExp.getValue()).intValue());
            ofertaAct.clearRequisitos();
            ofertaAct.agregarRequisito(cmbAreaTecnica.getSelectedItem().toString());
        } else if (rdObrero.isSelected()) {
            ofertaAct.setTipoCandidatoRequerido(TipoCandidato.OBRERO);
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
        AutorizacionService.exigirPermiso(BolsaLaboral.getInstancia().getUsuarioActual(),
                Permiso.GESTIONAR_OFERTAS);
        ArrayList<String> requisitos = new ArrayList<String>();
        ArrayList<String> idiomas = new ArrayList<String>();
        TipoCandidato tipoCandidato = TipoCandidato.UNIVERSITARIO;
        for (JCheckBox idioma : checkIdiomas) {
            if (idioma.isSelected()) {
                idiomas.add(idioma.getText());
            }
        }
        if (rdUniversitario.isSelected()) {
            tipoCandidato = TipoCandidato.UNIVERSITARIO;
            for (JCheckBox carrera : checkHabilidades) {
                if (carrera.isSelected()) {
                    requisitos.add(carrera.getText());
                }
            }
        } else if (rdObrero.isSelected()) {
            tipoCandidato = TipoCandidato.OBRERO;
            requisitos.add(cmbHabilidad.getSelectedItem().toString());
        } else if (rdTecnico.isSelected()) {
            tipoCandidato = TipoCandidato.TECNICO;
            requisitos.add(cmbAreaTecnica.getSelectedItem().toString());
        }

        OfertaLaboral nuevaOferta = new OfertaLaboral(
                txtCodigo.getText(), txtPuesto.getText(), txtDescripcion.getText(),
                cmbArea.getSelectedItem().toString(), cmbModalidad.getSelectedItem().toString(),
                cmbJornada.getSelectedItem().toString(), "Activa",
                ((Number) spnSalario.getValue()).floatValue(),
                ((Number) spnAniosExp.getValue()).intValue(),
                ((Number) spnVacantes.getValue()).intValue(),
                getCentroSeleccionado(),
                chkReubicacion.isSelected(), chkbxMayor.isSelected(), chkLicencia.isSelected(),
                tipoCandidato.getEtiqueta(), requisitos, idiomas,
                ((Number) spnPorcentaje.getValue()).intValue());
        nuevaOferta.setTipoCandidatoRequerido(tipoCandidato);
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

    private void cambiarEspecializacion(TipoCandidato especializacion) {
        if (especializacion == TipoCandidato.OBRERO) {
            specializationLayout.show(specializationCards, CARD_OBRERO);
        } else if (especializacion == TipoCandidato.TECNICO) {
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
        if (ofertaAct == null) {
            actualizarResumenVacantes();
            return;
        }

        ofertaAct.sincronizarVacantesOcupadas(
                BolsaLaboral.getInstancia().contarVacantesOcupadas(ofertaAct));
        txtCodigo.setText(ofertaAct.getCodigo());
        txtPuesto.setText(ofertaAct.getPuesto());
        cmbOfertador.setSelectedIndex(indiceCentro(ofertaAct.getOfertador()) + 1);
        spnSalario.setValue(ofertaAct.getSalario());
        txtDescripcion.setText(ofertaAct.getDescripcion());
        spnVacantes.setValue(ofertaAct.getVacantesTotales());
        actualizarResumenVacantes();
        spnPorcentaje.setValue(ofertaAct.getPorcentajeMinimo());
        cmbArea.setSelectedItem(ofertaAct.getArea());
        cmbModalidad.setSelectedItem(ofertaAct.getModalidad());
        cmbJornada.setSelectedItem(ofertaAct.getJornada());
        if (!ofertaAct.getRequisitos().isEmpty()) {
            cmbAreaTecnica.setSelectedItem(ofertaAct.getRequisitos().get(0));
        }
        spnAniosExp.setValue(ofertaAct.getExperienciaMinima());
        chkbxMayor.setSelected(ofertaAct.isObligatorioMayorDeEdad());
        chkReubicacion.setSelected(ofertaAct.isOfreceReubicacion());
        chkLicencia.setSelected(ofertaAct.isobligatorioLicencia());

        for (JCheckBox idioma : checkIdiomas) {
            idioma.setSelected(ofertaAct.getIdiomasRequeridas().contains(idioma.getText()));
        }
        if (ofertaAct.getTipoCandidatoRequerido() == TipoCandidato.UNIVERSITARIO) {
            rdUniversitario.setSelected(true);
            cambiarEspecializacion(TipoCandidato.UNIVERSITARIO);
            for (JCheckBox carrera : checkHabilidades) {
                carrera.setSelected(ofertaAct.getRequisitos().contains(carrera.getText()));
            }
        } else if (ofertaAct.getTipoCandidatoRequerido() == TipoCandidato.OBRERO) {
            rdObrero.setSelected(true);
            cambiarEspecializacion(TipoCandidato.OBRERO);
            if (!ofertaAct.getRequisitos().isEmpty()) {
                cmbHabilidad.setSelectedItem(ofertaAct.getRequisitos().get(0));
            }
        } else if (ofertaAct.getTipoCandidatoRequerido() == TipoCandidato.TECNICO) {
            rdTecnico.setSelected(true);
            cambiarEspecializacion(TipoCandidato.TECNICO);
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
        cambiarEspecializacion(TipoCandidato.UNIVERSITARIO);
        actualizarResumenVacantes();
    }

    private boolean verificar() throws FormatException {
        if (getCentroSeleccionado() == null) {
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
        if (cmbArea.getSelectedItem() == null) {
            throw new FormatException("Debe seleccionar un área.");
        }
        int total = ((Number) spnVacantes.getValue()).intValue();
        int ocupadas = ofertaAct == null ? 0
                : BolsaLaboral.getInstancia().contarVacantesOcupadas(ofertaAct);
        if (total < ocupadas) {
            throw new FormatException(
                    "Las vacantes totales no pueden ser menores que las ocupadas (" + ocupadas + ").");
        }
        return true;
    }

    private CentroEmpleador getCentroSeleccionado() {
        int indice = cmbOfertador.getSelectedIndex() - 1;
        return indice >= 0 && indice < centrosSeleccionables.size()
                ? centrosSeleccionables.get(indice) : null;
    }

    private int indiceCentro(CentroEmpleador buscado) {
        if (buscado == null) {
            return -1;
        }
        for (int indice = 0; indice < centrosSeleccionables.size(); indice++) {
            CentroEmpleador centro = centrosSeleccionables.get(indice);
            if (centro == buscado || java.util.Objects.equals(
                    centro.getCodigo(), buscado.getCodigo())) {
                return indice;
            }
        }
        return -1;
    }

    private void actualizarResumenVacantes() {
        if (txtVacantesOcupadas == null || txtVacantesDisponibles == null) {
            return;
        }
        int ocupadas = ofertaAct == null ? 0
                : BolsaLaboral.getInstancia().contarVacantesOcupadas(ofertaAct);
        int total = ((Number) spnVacantes.getValue()).intValue();
        txtVacantesOcupadas.setText(String.valueOf(ocupadas));
        txtVacantesDisponibles.setText(String.valueOf(Math.max(0, total - ocupadas)));
    }

    private String[] valoresCatalogo(TipoCatalogo tipo, String historico) {
        return BolsaLaboral.getInstancia().getCatalogos()
                .getValoresParaEdicion(tipo, historico).toArray(new String[0]);
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

    private String requisitoHistorico(TipoCandidato tipo) {
        if (ofertaAct != null && ofertaAct.getTipoCandidatoRequerido() == tipo
                && !ofertaAct.getRequisitos().isEmpty()) {
            return ofertaAct.getRequisitos().get(0);
        }
        return null;
    }

    private JCheckBox[] checksDe(JPanel panel) {
        ArrayList<JCheckBox> checks = new ArrayList<JCheckBox>();
        for (Component component : panel.getComponents()) {
            if (component instanceof JCheckBox) {
                checks.add((JCheckBox) component);
            }
        }
        return checks.toArray(new JCheckBox[0]);
    }
}
