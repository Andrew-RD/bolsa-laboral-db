package visual;

import org.junit.Test;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UbicacionComboSupportTest {

    @Test
    public void provinciaControlaMunicipiosYReiniciaSeleccion() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<Throwable>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                UbicacionComboSupport support = new UbicacionComboSupport();
                JComboBox<String> provincias = support.getProvinciaCombo();
                JComboBox<String> municipios = support.getMunicipioCombo();
                assertFalse(municipios.isEnabled());

                provincias.setSelectedItem("Santiago");
                assertTrue(municipios.isEnabled());
                assertTrue(contiene(municipios, "Baitoa"));
                assertFalse(contiene(municipios, "Boca Chica"));
                municipios.setSelectedItem("Baitoa");

                provincias.setSelectedItem("Santo Domingo");
                assertEquals(0, municipios.getSelectedIndex());
                assertTrue(contiene(municipios, "Boca Chica"));
                assertFalse(contiene(municipios, "Baitoa"));
            } catch (Throwable throwable) {
                error.set(throwable);
            }
        });
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void valorLegadoDesconocidoSeConservaYAdvierte() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<Throwable>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                UbicacionComboSupport support = new UbicacionComboSupport();
                support.seleccionar("Provincia histórica", "Municipio histórico");
                assertEquals("Provincia histórica", support.getProvincia());
                assertEquals("Municipio histórico", support.getMunicipio());
                assertTrue(support.esLegada());
                assertTrue(support.getProvinciaCombo().getToolTipText().contains("legado"));
                support.validar();
            } catch (Throwable throwable) {
                error.set(throwable);
            }
        });
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    private boolean contiene(JComboBox<String> combo, String valor) {
        for (int index = 0; index < combo.getItemCount(); index++) {
            if (valor.equals(combo.getItemAt(index))) {
                return true;
            }
        }
        return false;
    }
}
