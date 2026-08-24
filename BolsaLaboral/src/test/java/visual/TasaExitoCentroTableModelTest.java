package visual;

import logico.TasaExitoCentroDTO;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TasaExitoCentroTableModelTest {

    @Test
    public void utilizaSemanticaDeConversionYConservaElPorcentaje() {
        TasaExitoCentroTableModel model = new TasaExitoCentroTableModel(Arrays.asList(
                new TasaExitoCentroDTO("Centro", 3, 1, 33.33)));

        assertEquals("Oportunidades enviadas", model.getColumnName(1));
        assertEquals("Tasa de conversión (%)", model.getColumnName(3));
        assertEquals(Integer.class, model.getColumnClass(1));
        assertEquals(Double.class, model.getColumnClass(3));
        assertEquals(Integer.valueOf(3), model.getValueAt(0, 1));
        assertEquals(Double.valueOf(33.33), model.getValueAt(0, 3));
        for (int columna = 0; columna < model.getColumnCount(); columna++) {
            assertFalse(model.isCellEditable(0, columna));
        }
    }
}
