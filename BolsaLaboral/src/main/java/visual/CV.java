package visual;

import logico.Candidato;
import logico.Obrero;
import logico.TecnicoSuperior;
import logico.Universitario;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

public class CV extends JDialog {

    public static Object[] row;
    private JTextPane txpIdiomas;
    private JTextPane txpDatosFormacion;
    private JPanel pnlResumen;
    private JLabel lblNombre;
    private JLabel lblFechaNac;
    private JTextPane txpDescripcion;
    private JLabel lblModalidad;
    private JLabel lblJornada;
    private JLabel lblArea;
    private JLabel lblform;
    private JLabel lblUbic;
    private JLabel lblTelefono;

    public CV(Candidato solicitante) {
        setTitle(solicitante.getNombres() + " " + solicitante.getApellidos());
        setIconImage(UIUtils.image("icono.png"));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        setContentPane(container);

        pnlResumen = buildSummaryPanel();
        container.add(pnlResumen, BorderLayout.WEST);
        container.add(buildDetailPanel(), BorderLayout.CENTER);

        cargarCV(solicitante);
        setApariencia(solicitante.getAreaDeInteres());
        UIUtils.finishDialog(this, getOwner(), 820, 640);
    }

    private JPanel buildSummaryPanel() {
        JPanel summary = new JPanel(new GridBagLayout());
        summary.setBorder(UIUtils.emptyBorder(12, 12, 12, 12));
        summary.setPreferredSize(UIUtils.dimension(245, 600));

        int line = 0;
        addSummaryTitle(summary, line++, " Formación", "nivel.png");
        lblform = summaryValue();
        addSummaryValue(summary, line++, lblform, 0);
        addSummaryTitle(summary, line++, " Ubicación", "ubicacion.png");
        lblUbic = summaryValue();
        addSummaryValue(summary, line++, lblUbic, 0);
        addSummaryTitle(summary, line++, " Contactos", "contactos.png");
        lblTelefono = summaryValue();
        addSummaryValue(summary, line++, lblTelefono, 0);
        addSummaryTitle(summary, line++, " Idiomas", "idiomas.png");

        txpIdiomas = new JTextPane();
        txpIdiomas.setEditable(false);
        txpIdiomas.setFocusable(false);
        txpIdiomas.setForeground(Color.WHITE);
        txpIdiomas.setFont(UIUtils.largeFont(Font.PLAIN));
        JScrollPane languagesScroll = UIUtils.scrollable(txpIdiomas);
        GridBagConstraints languagesConstraints = UIUtils.constraints(0, line);
        languagesConstraints.weightx = 1;
        languagesConstraints.weighty = 1;
        languagesConstraints.fill = GridBagConstraints.BOTH;
        languagesConstraints.insets = UIUtils.insets(4, 0, 0, 0);
        summary.add(languagesScroll, languagesConstraints);
        return summary;
    }

    private void addSummaryTitle(JPanel panel, int rowIndex, String text, String icon) {
        JLabel label = new JLabel(text, UIUtils.icon(icon), SwingConstants.LEFT);
        label.setForeground(Color.WHITE);
        label.setFont(UIUtils.h4Font(Font.BOLD));
        GridBagConstraints constraints = UIUtils.constraints(0, rowIndex);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = UIUtils.insets(8, 0, 2, 0);
        panel.add(label, constraints);
    }

    private JLabel summaryValue() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(UIUtils.largeFont(Font.PLAIN));
        return label;
    }

    private void addSummaryValue(JPanel panel, int rowIndex, JLabel label, double weightY) {
        GridBagConstraints constraints = UIUtils.constraints(0, rowIndex);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = weightY;
        constraints.insets = UIUtils.insets(2, 0, 6, 0);
        panel.add(label, constraints);
    }

    private JPanel buildDetailPanel() {
        JPanel detail = new JPanel(new BorderLayout(UIUtils.scale(8), UIUtils.scale(8)));
        detail.setBackground(Color.WHITE);
        detail.setBorder(UIUtils.emptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new BorderLayout(0, UIUtils.scale(4)));
        header.setOpaque(false);
        lblNombre = new JLabel("NOMBRE");
        lblNombre.setFont(UIUtils.h1Font(Font.PLAIN));
        header.add(lblNombre, BorderLayout.NORTH);
        lblFechaNac = new JLabel("Fecha Nac.", UIUtils.icon("calendario.png"), SwingConstants.LEFT);
        lblFechaNac.setFont(UIUtils.defaultFont(Font.BOLD));
        header.add(lblFechaNac, BorderLayout.CENTER);
        header.add(new JSeparator(), BorderLayout.SOUTH);
        detail.add(header, BorderLayout.NORTH);

        JPanel sections = new JPanel(new GridBagLayout());
        sections.setOpaque(false);
        int rowIndex = 0;
        JLabel aboutTitle = sectionTitle("SOBRE MI");
        GridBagConstraints titleConstraints = UIUtils.constraints(0, rowIndex++);
        titleConstraints.weightx = 1;
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        sections.add(aboutTitle, titleConstraints);

        txpDescripcion = textPane();
        GridBagConstraints aboutConstraints = UIUtils.constraints(0, rowIndex++);
        aboutConstraints.weightx = 1;
        aboutConstraints.weighty = 0.6;
        aboutConstraints.fill = GridBagConstraints.BOTH;
        JScrollPane aboutScroll = UIUtils.scrollable(txpDescripcion);
        aboutScroll.setPreferredSize(UIUtils.dimension(520, 170));
        sections.add(aboutScroll, aboutConstraints);

        JLabel academicTitle = sectionTitle("DATOS ACADÉMICOS");
        GridBagConstraints academicTitleConstraints = UIUtils.constraints(0, rowIndex++);
        academicTitleConstraints.weightx = 1;
        academicTitleConstraints.fill = GridBagConstraints.HORIZONTAL;
        sections.add(academicTitle, academicTitleConstraints);

        txpDatosFormacion = textPane();
        GridBagConstraints academicConstraints = UIUtils.constraints(0, rowIndex);
        academicConstraints.weightx = 1;
        academicConstraints.weighty = 0.4;
        academicConstraints.fill = GridBagConstraints.BOTH;
        JScrollPane academicScroll = UIUtils.scrollable(txpDatosFormacion);
        academicScroll.setPreferredSize(UIUtils.dimension(520, 130));
        sections.add(academicScroll, academicConstraints);
        detail.add(sections, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JPanel preferences = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, UIUtils.scale(8), 0));
        preferences.setOpaque(false);
        JLabel title = new JLabel("Preferencias:");
        title.setFont(UIUtils.h4Font(Font.BOLD));
        preferences.add(title);
        lblArea = new JLabel();
        lblJornada = new JLabel();
        lblModalidad = new JLabel();
        preferences.add(lblArea);
        preferences.add(lblJornada);
        preferences.add(lblModalidad);
        footer.add(preferences, BorderLayout.CENTER);
        JButton closeButton = UIUtils.button("Cerrar", "cerrar.png");
        closeButton.addActionListener(event -> dispose());
        footer.add(closeButton, BorderLayout.EAST);
        detail.add(footer, BorderLayout.SOUTH);
        return detail;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.h4Font(Font.BOLD));
        return label;
    }

    private JTextPane textPane() {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setFont(UIUtils.defaultFont(Font.PLAIN));
        return pane;
    }

    public void cargarCV(Candidato solicitante) {
        if (solicitante instanceof Universitario) {
            lblform.setText(((Universitario) solicitante).getNivelAcademico());
        } else if (solicitante instanceof TecnicoSuperior) {
            lblform.setText("Técnico");
        } else if (solicitante instanceof Obrero) {
            lblform.setText("Trabajador");
        }
        lblNombre.setText(getFormatNombre(solicitante));
        lblFechaNac.setText(solicitante.getFechaNacimiento().toString());
        lblUbic.setText(getFormatUbicacion(solicitante));
        lblUbic.setToolTipText(solicitante.getMunicipio() + ", " + solicitante.getProvincia());
        lblTelefono.setText(solicitante.getTelefono());
        lblArea.setToolTipText(solicitante.getAreaDeInteres());
        lblJornada.setToolTipText(solicitante.getJornada());
        lblModalidad.setToolTipText(solicitante.getModalidad());
        cargarIdiomas(solicitante.getIdiomas());
        txpDescripcion.setText(solicitante.getSobreMi());
        txpDatosFormacion.setText(solicitante.getFormacion());
        lblArea.setIcon(UIUtils.valueIcon(solicitante.getAreaDeInteres()));
        lblJornada.setIcon(UIUtils.valueIcon(solicitante.getJornada()));
        lblModalidad.setIcon(UIUtils.valueIcon(solicitante.getModalidad()));
    }

    private String getFormatUbicacion(Candidato solicitante) {
        String municipio = solicitante.getMunicipio();
        String provincia = solicitante.getProvincia();
        String ubicacion = municipio + ", " + provincia;
        if (ubicacion.length() <= 15) {
            return ubicacion;
        }
        int maxPorParte = (15 - 2) / 2;
        String municipioAbrev = municipio.length() > maxPorParte
                ? municipio.substring(0, maxPorParte - 1) + "." : municipio;
        String provinciaAbrev = provincia.length() > maxPorParte
                ? provincia.substring(0, maxPorParte - 1) + "." : provincia;
        String resultado = municipioAbrev + ", " + provinciaAbrev;
        while (resultado.length() > 15 && provinciaAbrev.length() > 2) {
            provinciaAbrev = provinciaAbrev.substring(0, provinciaAbrev.length() - 2) + ".";
            resultado = municipioAbrev + ", " + provinciaAbrev;
        }
        return resultado;
    }

    private String getFormatNombre(Candidato solicitante) {
        String nombreCompleto = solicitante.getNombres() + " " + solicitante.getApellidos();
        if (nombreCompleto.length() <= 26) {
            return nombreCompleto;
        }
        String[] nombres = solicitante.getNombres().split(" ");
        String[] apellidos = solicitante.getApellidos().split(" ");
        StringBuilder builder = new StringBuilder();
        if (nombres.length > 0) {
            builder.append(nombres[0]);
        }
        if (nombres.length > 1) {
            builder.append(" ").append(nombres[1].charAt(0)).append('.');
        }
        if (apellidos.length > 0) {
            builder.append(" ").append(apellidos[0]);
        }
        if (apellidos.length > 1) {
            builder.append(" ").append(apellidos[1].charAt(0)).append('.');
        }
        return builder.toString().trim();
    }

    private void cargarIdiomas(ArrayList<String> idiomas) {
        StringBuilder text = new StringBuilder();
        for (String idioma : idiomas) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(idioma);
        }
        txpIdiomas.setText(text.toString());
    }

    private Color getColorPrincipal(String area) {
        switch (area) {
            case "Finanzas": return new Color(213, 69, 27);
            case "Recursos Humanos": return new Color(27, 60, 83);
            case "Marketing": return new Color(197, 23, 46);
            case "Limpieza": return new Color(78, 102, 136);
            case "Seguridad": return new Color(10, 64, 12);
            case "TI": return new Color(9, 107, 104);
            case "Salud": return new Color(162, 18, 50);
            case "Operaciones": return new Color(39, 63, 79);
            case "Administración": return new Color(190, 49, 68);
            case "Atención al Cliente": return new Color(130, 17, 49);
            default: return new Color(57, 62, 7);
        }
    }

    public void setApariencia(String area) {
        Color background = getColorPrincipal(area);
        pnlResumen.setBackground(background);
        txpIdiomas.setBackground(background);
        txpDescripcion.setBackground(Color.WHITE);
        txpDatosFormacion.setBackground(Color.WHITE);
    }
}
