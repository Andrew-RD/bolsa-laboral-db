package visual;

import logico.BolsaLaboral;
import logico.Candidato;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
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
            BolsaLaboral.setInstancia(bolsa);
            UIUtils.initializeLookAndFeel();

            final int[] resultados = new int[1];
            final boolean[] botonHabilitado = new boolean[1];
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    ProcesamientoAvanzado dialog = new ProcesamientoAvanzado();
                    dialog.setModal(false);
                    dialog.setVisible(true);
                    resultados[0] = ProcesamientoAvanzado.modeloMatcheo.getRowCount();
                    if (resultados[0] == 0) {
                        dialog.dispose();
                        throw new AssertionError("Procesamiento no cargó resultados para el archivo legado.");
                    }
                    try {
                        JTable table = (JTable) field("tablaMatcheo").get(null);
                        JButton processButton = (JButton) field("btnProcesar").get(dialog);
                        table.setRowSelectionInterval(0, 0);
                        MouseEvent click = new MouseEvent(table, MouseEvent.MOUSE_CLICKED,
                                System.currentTimeMillis(), 0, 1, 1, 1, false);
                        for (MouseListener listener : table.getMouseListeners()) {
                            listener.mouseClicked(click);
                        }
                        botonHabilitado[0] = processButton.isEnabled();
                        if (!botonHabilitado[0]) {
                            throw new AssertionError("Seleccionar un resultado no habilitó el botón Procesar.");
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
                    + " migrados=" + migrados + " resultados=" + resultados[0]
                    + " procesarHabilitado=" + botonHabilitado[0]);
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
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
}
