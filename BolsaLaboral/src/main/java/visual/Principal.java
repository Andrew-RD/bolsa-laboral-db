package visual;

import logico.BolsaLaboral;
import logico.AutorizacionService;
import logico.Permiso;
import server.Servidor;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Font;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Supplier;

public class Principal extends JFrame {

    private JMenu mnGestion;
    private JMenu mnUsuarios;
    private static boolean serverStarted;
    private static Socket sfd;
    private static DataInputStream entradaSocket;
    private static DataOutputStream salidaSocket;

    public Principal() {
        startBackupServerOnce();

        setTitle("Bolsa Laboral");
        setIconImage(UIUtils.image("icono.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
        setContentPane(new ScaledImagePanel("fondo.png"));

        UIUtils.finishFrame(this, 800, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static synchronized void startBackupServerOnce() {
        if (!serverStarted) {
            Servidor servidor = new Servidor(7000);
            servidor.setDaemon(true);
            servidor.start();
            serverStarted = true;
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);

        JMenu centros = menu("Centros de Trabajo", "empresa.png");
        addAuthorized(centros, Permiso.CONSULTAR_CENTROS,
                item("  Consultar", "consulta.png",
                        event -> openAuthorized(Permiso.CONSULTAR_CENTROS,
                                () -> new ConsultarCentros())));
        addAuthorized(centros, Permiso.GESTIONAR_CENTROS,
                item("  Registrar", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_CENTROS,
                                () -> new RegistroCentro(null))));
        addIfNotEmpty(menuBar, centros);

        JMenu candidatos = menu("Candidatos", "trabajador.png");
        addAuthorized(candidatos, Permiso.CONSULTAR_CANDIDATOS,
                item("  Consultar", "consulta.png",
                        event -> openAuthorized(Permiso.CONSULTAR_CANDIDATOS,
                                () -> new ConsultarCandidatos())));
        addAuthorized(candidatos, Permiso.GESTIONAR_CANDIDATOS,
                item("  Registrar", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_CANDIDATOS,
                                () -> new RegistroCandidato(null))));
        addIfNotEmpty(menuBar, candidatos);

        JMenu ofertas = menu("Catálogo de Ofertas", "conexion.png");
        addAuthorized(ofertas, Permiso.CONSULTAR_OFERTAS,
                item("  Consultar", "consulta.png",
                        event -> openAuthorized(Permiso.CONSULTAR_OFERTAS,
                                () -> new ConsultarOfertas())));
        addAuthorized(ofertas, Permiso.GESTIONAR_OFERTAS,
                item("  Registrar", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_OFERTAS,
                                () -> new RegistroOfertaLaboral((logico.OfertaLaboral) null))));
        addAuthorized(ofertas, Permiso.CONSULTAR_SOLICITUDES,
                item("  Solicitudes", "solicitud.png",
                        event -> openAuthorized(Permiso.CONSULTAR_SOLICITUDES,
                                () -> new ConsultarSolicitudes())));
        addIfNotEmpty(menuBar, ofertas);

        mnGestion = menu("Gestión de Datos", "gestion.png");
        addAuthorized(mnGestion, Permiso.USAR_PROCESAMIENTO_AVANZADO,
                item("  Procesamiento", "avanzado.png",
                        event -> openAuthorized(Permiso.USAR_PROCESAMIENTO_AVANZADO,
                                () -> new ProcesamientoAvanzado())));
        addAuthorized(mnGestion, Permiso.GESTIONAR_RESPALDOS,
                item("  Crear Respaldo", "respaldo.png",
                        event -> runAuthorized(Permiso.GESTIONAR_RESPALDOS,
                                () -> crearRespaldo())));
        addAuthorized(mnGestion, Permiso.GESTIONAR_RESPALDOS,
                item("  Cargar Respaldo", "descargar.png",
                        event -> runAuthorized(Permiso.GESTIONAR_RESPALDOS,
                                () -> cargarRespaldo())));
        addAuthorized(mnGestion, Permiso.VER_INFORMES,
                item("  Informe", "informes.png",
                        event -> openAuthorized(Permiso.VER_INFORMES,
                                () -> new InformeGeneral())));
        addAuthorized(mnGestion, Permiso.GESTIONAR_CATALOGOS,
                item("  Catálogos", "gestion.png",
                        event -> openAuthorized(Permiso.GESTIONAR_CATALOGOS,
                                () -> new GestionCatalogos())));
        addIfNotEmpty(menuBar, mnGestion);

        mnUsuarios = menu("Gestión de Usuarios", "user.png");
        addAuthorized(mnUsuarios, Permiso.GESTIONAR_USUARIOS,
                item("  Consultar usuarios", "consulta.png",
                        event -> openAuthorized(Permiso.GESTIONAR_USUARIOS,
                                () -> new ConsultarUsuarios())));
        addAuthorized(mnUsuarios, Permiso.GESTIONAR_USUARIOS,
                item("  Registrar usuario", "registro.png",
                        event -> openAuthorized(Permiso.GESTIONAR_USUARIOS,
                                () -> new RegistroUsuario(null))));
        addIfNotEmpty(menuBar, mnUsuarios);

        return menuBar;
    }

    private JMenu menu(String text, String icon) {
        JMenu menu = new JMenu(text);
        menu.setIcon(UIUtils.icon(icon));
        menu.setForeground(Color.BLACK);
        menu.setFont(UIUtils.h4Font(Font.BOLD));
        return menu;
    }

    private JMenuItem item(String text, String icon, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text, UIUtils.icon(icon));
        item.setFont(UIUtils.largeFont(Font.PLAIN));
        item.addActionListener(listener);
        return item;
    }

    private void openModal(JDialog dialog) {
        dialog.setModal(true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openAuthorized(Permiso permiso, Supplier<JDialog> dialogFactory) {
        try {
            AutorizacionService.exigirPermiso(
                    BolsaLaboral.getInstancia().getUsuarioActual(), permiso);
            JDialog dialog = dialogFactory.get();
            openModal(dialog);
        } catch (SecurityException exception) {
            showUnauthorized(exception);
        }
    }

    private void runAuthorized(Permiso permiso, Runnable action) {
        try {
            AutorizacionService.exigirPermiso(
                    BolsaLaboral.getInstancia().getUsuarioActual(), permiso);
            action.run();
        } catch (SecurityException exception) {
            showUnauthorized(exception);
        }
    }

    private void addAuthorized(JMenu menu, Permiso permiso, JMenuItem item) {
        if (AutorizacionService.tienePermiso(
                BolsaLaboral.getInstancia().getUsuarioActual(), permiso)) {
            menu.add(item);
        }
    }

    private void addIfNotEmpty(JMenuBar menuBar, JMenu menu) {
        if (menu.getItemCount() > 0) {
            menuBar.add(menu);
        }
    }

    private void showUnauthorized(SecurityException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
                "Acción no autorizada", JOptionPane.WARNING_MESSAGE);
    }

    private void crearRespaldo() {
        AutorizacionService.exigirPermiso(
                BolsaLaboral.getInstancia().getUsuarioActual(), Permiso.GESTIONAR_RESPALDOS);
        saveBolsa();
        saveCodigos();
        enviarArchivo("bolsa", "bolsa.dat");
        enviarArchivo("codigos", "codigos.dat");
        JOptionPane.showMessageDialog(this,
                "Respaldo enviado exitosamente al servidor",
                "Respaldo Completado", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void saveBolsa() {
        FileOutputStream bolsaOut;
        ObjectOutputStream bolsaWrite;
        try {
            bolsaOut = new FileOutputStream("bolsa.dat");
            bolsaWrite = new ObjectOutputStream(bolsaOut);
            bolsaWrite.writeObject(BolsaLaboral.getInstancia());
            bolsaOut.close();
            bolsaWrite.close();
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void saveCodigos() {
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream("codigos.dat"))) {
            output.writeInt(BolsaLaboral.genCodigoCandidato);
            output.writeInt(BolsaLaboral.genCodigoSolicitud);
            output.writeInt(BolsaLaboral.genCodigoOferta);
            output.writeInt(BolsaLaboral.genCodigoCentro);
            output.writeInt(BolsaLaboral.genCodigoVacanteCompletada);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void enviarArchivo(String tipo, String nombreArchivo) {
        try {
            sfd = new Socket("127.0.0.1", 7000);
            File archivo = new File(nombreArchivo);
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this, "Archivo " + nombreArchivo + " no encontrado",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            entradaSocket = new DataInputStream(new FileInputStream(archivo));
            salidaSocket = new DataOutputStream(sfd.getOutputStream());
            salidaSocket.writeUTF(tipo);

            int unByte;
            while ((unByte = entradaSocket.read()) != -1) {
                salidaSocket.write(unByte);
            }
            salidaSocket.flush();
        } catch (UnknownHostException exception) {
            JOptionPane.showMessageDialog(this,
                    "No se puede acceder al servidor: " + exception.getMessage(),
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this,
                    "Error durante la transferencia: " + exception.getMessage(),
                    "Error de Comunicación", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (entradaSocket != null) {
                    entradaSocket.close();
                }
                if (salidaSocket != null) {
                    salidaSocket.close();
                }
                if (sfd != null) {
                    sfd.close();
                }
            } catch (IOException exception) {
                System.out.println("Error al cerrar recursos: " + exception.getMessage());
            } finally {
                entradaSocket = null;
                salidaSocket = null;
                sfd = null;
            }
        }
    }

    private void cargarRespaldo() {
        AutorizacionService.exigirPermiso(
                BolsaLaboral.getInstancia().getUsuarioActual(), Permiso.GESTIONAR_RESPALDOS);
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setDialogTitle("Seleccionar archivo de respaldo de la bolsa");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de respaldo (.dat)", "dat"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivoBolsa = fileChooser.getSelectedFile();
        String nombre = archivoBolsa.getName();
        if (!nombre.startsWith("bolsa_respaldo_") || !nombre.endsWith(".dat")) {
            JOptionPane.showMessageDialog(this,
                    "Archivo inválido. Debe ser un respaldo tipo 'bolsa_respaldo_#.dat'",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String numero = nombre.replace("bolsa_respaldo_", "").replace(".dat", "");
        File archivoCodigos = new File(
                archivoBolsa.getParentFile(), "codigos_respaldo_" + numero + ".dat");
        if (!archivoCodigos.exists()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró el archivo de códigos correspondiente: " + archivoCodigos.getName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Deseas restaurar la bolsa y los códigos desde el respaldo #" + numero + "?",
                "Confirmar Restauración", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        if (!archivoBolsa.getName().endsWith(".dat")) {
            JOptionPane.showMessageDialog(this, "El archivo debe tener extensión .dat",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (ObjectInputStream bolsaIn = new ObjectInputStream(new FileInputStream(archivoBolsa))) {
            BolsaLaboral instancia = (BolsaLaboral) bolsaIn.readObject();
            instancia.migrarDatosDeserializados();
            BolsaLaboral.setInstancia(instancia);
        } catch (IOException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar archivo de bolsa: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar archivo de bolsa: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (DataInputStream codigosIn = new DataInputStream(new FileInputStream(archivoCodigos))) {
            BolsaLaboral.genCodigoCandidato = codigosIn.readInt();
            BolsaLaboral.genCodigoSolicitud = codigosIn.readInt();
            BolsaLaboral.genCodigoOferta = codigosIn.readInt();
            BolsaLaboral.genCodigoCentro = codigosIn.readInt();
            BolsaLaboral.genCodigoVacanteCompletada = codigosIn.readInt();
        } catch (IOException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar archivo de códigos: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Respaldo #" + numero + " restaurado exitosamente.",
                "Restauración completada", JOptionPane.INFORMATION_MESSAGE);
    }
}
