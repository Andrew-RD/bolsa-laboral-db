package visual;

import logico.BolsaLaboral;
import logico.Candidato;
import logico.RolUsuario;
import logico.Usuario;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;

/** Verifica el archivo legado sin escribir ni reemplazar el archivo original. */
public final class LegacyBolsaSmoke {

    private LegacyBolsaSmoke() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Uso: LegacyBolsaSmoke <bolsa.dat>");
        }
        try {
            BolsaLaboral bolsa = readBolsa(args[0]);
            int estadosAusentes = countMissingStates(bolsa);
            int migrados = bolsa.migrarDatosDeserializados();
            int segundaMigracion = bolsa.migrarDatosDeserializados();
            if (segundaMigracion != 0) {
                throw new AssertionError(
                        "La migración no es idempotente; segundo ciclo=" + segundaMigracion + ".");
            }
            Usuario administrador = firstActiveAdmin(bolsa);
            if (administrador == null) {
                throw new AssertionError("El archivo legado no contiene un administrador activo.");
            }
            bolsa.setUsuarioActual(administrador);
            BolsaLaboral.setInstancia(bolsa);
            UIUtils.initializeLookAndFeel();

            final int[] resultados = new int[1];
            final boolean[] botonHabilitado = new boolean[1];
            final String[] razon = new String[1];
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    ProcesamientoAvanzado dialog = new ProcesamientoAvanzado();
                    dialog.setModal(false);
                    dialog.setVisible(true);
                    resultados[0] = ProcesamientoAvanzado.modeloMatcheo.getRowCount();
                    try {
                        JTable table = (JTable) field("tablaMatcheo").get(null);
                        JTable ofertasTable = (JTable) field("tablaOfertas").get(null);
                        JButton processButton = (JButton) field("btnProcesar").get(dialog);
                        JLabel reasonLabel = (JLabel) field(
                                "lblRazonProcesamiento").get(dialog);
                        if (ofertasTable.getRowCount() > 0) {
                            clickFirstRow(ofertasTable);
                        }
                        if (resultados[0] > 0) {
                            clickFirstRow(table);
                            botonHabilitado[0] = processButton.isEnabled();
                            if (!botonHabilitado[0]) {
                                throw new AssertionError(
                                        "Seleccionar un resultado elegible no habilitó Procesar.");
                            }
                        } else {
                            botonHabilitado[0] = processButton.isEnabled();
                            if (botonHabilitado[0]) {
                                throw new AssertionError(
                                    "Procesar quedó habilitado sin resultados elegibles.");
                            }
                        }
                        razon[0] = reasonLabel.getText();
                        if (razon[0] == null || razon[0].trim().isEmpty()
                                || processButton.getToolTipText() == null
                                || processButton.getToolTipText().trim().isEmpty()) {
                            throw new AssertionError(
                                    "El botón Procesar no expone una razón visible.");
                        }
                    } catch (ReflectiveOperationException exception) {
                        throw new IllegalStateException("No se pudo inspeccionar Procesamiento.", exception);
                    } finally {
                        dialog.dispose();
                    }
                }
            });

            if (migrados < estadosAusentes) {
                throw new AssertionError("La migración no normalizó todos los estados ausentes.");
            }
            System.out.println("LEGACY_BOLSA_OK estadosAusentes=" + estadosAusentes
                    + " migrados=" + migrados + " segundaMigracion=" + segundaMigracion
                    + " resultados=" + resultados[0]
                    + " procesarHabilitado=" + botonHabilitado[0]
                    + " razon=" + razon[0]);
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
    }

    private static Usuario firstActiveAdmin(BolsaLaboral bolsa) {
        for (Usuario usuario : bolsa.getUsuarios()) {
            if (usuario != null && usuario.isActivo()
                    && usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                return usuario;
            }
        }
        return null;
    }

    private static BolsaLaboral readBolsa(String path) throws Exception {
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(path));
        try {
            return (BolsaLaboral) input.readObject();
        } finally {
            input.close();
        }
    }

    private static int countMissingStates(BolsaLaboral bolsa) {
        int count = 0;
        for (Candidato candidato : bolsa.getCandidatos()) {
            if (candidato != null
                    && (candidato.getEstado() == null || candidato.getEstado().trim().isEmpty())) {
                count++;
            }
        }
        return count;
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = ProcesamientoAvanzado.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void clickFirstRow(JTable table) {
        table.setRowSelectionInterval(0, 0);
        for (MouseListener listener : table.getMouseListeners()) {
            listener.mouseClicked(null);
        }
    }
}
