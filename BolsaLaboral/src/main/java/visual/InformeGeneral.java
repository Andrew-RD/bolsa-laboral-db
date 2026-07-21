package visual;

import logico.BolsaLaboral;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class InformeGeneral extends JDialog {

    private JLabel lblCandidatos;
    private JLabel lblOfertas;
    private JLabel lblEmpresas;
    private JLabel lblSolicitudes;
    private JLabel lblContratados;
    private JLabel lblDeExto;
    private JLabel lblTasaCobertura;
    private JLabel lblOfVacias;

    public InformeGeneral() {
        setTitle("Informe General");
        setIconImage(UIUtils.image("icono.png"));

        JPanel content = new JPanel(new BorderLayout(UIUtils.scale(12), UIUtils.scale(12)));
        content.setBackground(UIUtils.DARK_BACKGROUND);
        content.setBorder(UIUtils.emptyBorder(0, 0, 10, 0));
        setContentPane(content);
        content.add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, UIUtils.scale(8)));
        center.setBackground(UIUtils.DARK_BACKGROUND);
        center.setBorder(UIUtils.emptyBorder(0, 18, 0, 18));
        JLabel title = new JLabel("Resumen Informativo", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(UIUtils.h3Font(Font.PLAIN));
        center.add(title, BorderLayout.NORTH);

        JPanel dashboard = new JPanel(new GridLayout(2, 4, UIUtils.scale(12), UIUtils.scale(12)));
        dashboard.setBackground(UIUtils.DARK_BACKGROUND);
        lblCandidatos = dashboardCard(dashboard, "Candidatos", "dashcand.png");
        lblOfertas = dashboardCard(dashboard, "Ofertas", "dashvac.png");
        lblEmpresas = dashboardCard(dashboard, "Empresas", "dashcentros.png");
        lblSolicitudes = dashboardCard(dashboard, "Solicitudes", "dashsolicitud.png");
        lblContratados = dashboardCard(dashboard, "Contratados", "dashcontratados.png");
        lblDeExto = dashboardCard(dashboard, "De Éxito", "dashexito.png");
        lblTasaCobertura = dashboardCard(dashboard, "de Cobertura", "dashcobertura.png");
        lblOfVacias = dashboardCard(dashboard, "Ofertas vacías", "dashofvacias.png");
        JScrollPane dashboardScroll = UIUtils.scrollable(dashboard);
        dashboardScroll.getViewport().setBackground(UIUtils.DARK_BACKGROUND);
        center.add(dashboardScroll, BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIUtils.scale(12), 0));
        footer.setBackground(UIUtils.DARK_BACKGROUND);
        footer.setBorder(UIUtils.emptyBorder(0, 18, 0, 18));
        JButton closeButton = UIUtils.button("Cerrar", "cerrar.png");
        closeButton.addActionListener(event -> dispose());
        footer.add(closeButton);
        content.add(footer, BorderLayout.SOUTH);

        cargarValores();
        UIUtils.finishDialog(this, getOwner(), 900, 650);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, UIUtils.scale(8), UIUtils.scale(16)));
        header.setBackground(UIUtils.TEAL_DARK);
        header.setBorder(UIUtils.emptyBorder(0, 12, 0, 12));
        JLabel welcome = new JLabel("Bienvenido");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(UIUtils.h2Font(Font.PLAIN));
        JLabel user = new JLabel(BolsaLaboral.getInstancia().getUsuarioActual().getNombreUsuario());
        user.setForeground(Color.WHITE);
        user.setFont(UIUtils.h2Font(Font.BOLD));
        header.add(welcome);
        header.add(user);
        return header;
    }

    private JLabel dashboardCard(JPanel dashboard, String text, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new LineBorder(Color.WHITE, UIUtils.scale(2)));
        card.setBackground(UIUtils.TEAL);
        JLabel image = new JLabel(UIUtils.icon(icon), SwingConstants.CENTER);
        image.setBorder(UIUtils.emptyBorder(8, 8, 2, 8));
        card.add(image, BorderLayout.CENTER);
        JLabel value = new JLabel(text, SwingConstants.CENTER);
        value.setForeground(Color.WHITE);
        value.setFont(UIUtils.largeFont(Font.PLAIN));
        value.setBorder(UIUtils.emptyBorder(2, 4, 8, 4));
        card.add(value, BorderLayout.SOUTH);
        dashboard.add(card);
        return value;
    }

    private void cargarValores() {
        int cantCand = BolsaLaboral.getInstancia().getCandidatos().size();
        lblCandidatos.setText(formatNumero(cantCand) + (cantCand != 1 ? " Candidatos" : " Candidato"));
        int cantOfertas = BolsaLaboral.getInstancia().getOfertas().size();
        lblOfertas.setText(formatNumero(cantOfertas) + (cantOfertas != 1 ? " Ofertas" : " Oferta"));
        int cantCentros = BolsaLaboral.getInstancia().getCentros().size();
        lblEmpresas.setText(formatNumero(cantCentros) + (cantCentros != 1 ? " Empresas" : " Empresa"));
        int cantSolicitudes = contarNoNulos(BolsaLaboral.getInstancia().getSolicitudes());
        lblSolicitudes.setText(formatNumero(cantSolicitudes)
                + (cantSolicitudes != 1 ? " Solicitudes" : " Solicitud"));
        int cantVacCompletadas = contarNoNulos(BolsaLaboral.getInstancia().getVacantes());
        lblContratados.setText(formatNumero(cantVacCompletadas)
                + (cantVacCompletadas != 1 ? " Contratados" : " Contratado"));
        lblTasaCobertura.setText(BolsaLaboral.getInstancia().calcularTasaCovertura() + "% De Cobertura");
        if (cantSolicitudes > 0) {
            lblDeExto.setText(calcularTasaExito(cantVacCompletadas, cantSolicitudes) + "% De Éxito");
        } else {
            lblDeExto.setText("0% de Éxito");
        }
        int ofVacias = BolsaLaboral.getInstancia().obtenerOfertasVacias();
        lblOfVacias.setText(formatNumero(ofVacias)
                + (ofVacias != 1 ? " Ofertas Vacias" : " Oferta Vacia"));
    }

    private String formatNumero(int valor) {
        if (valor < 1000) {
            return String.valueOf(valor);
        } else if (valor < 1_000_000) {
            return String.format("%.1fK", valor / 1000.0);
        }
        return String.format("%.1fM", valor / 1_000_000.0);
    }

    static int calcularTasaExito(int contrataciones, int solicitudes) {
        if (contrataciones <= 0 || solicitudes <= 0) {
            return 0;
        }
        int tasa = Math.round(contrataciones * 100.0f / solicitudes);
        return Math.max(0, Math.min(100, tasa));
    }

    static int contarNoNulos(Iterable<?> elementos) {
        if (elementos == null) {
            return 0;
        }
        int cantidad = 0;
        for (Object elemento : elementos) {
            if (elemento != null) {
                cantidad++;
            }
        }
        return cantidad;
    }
}
