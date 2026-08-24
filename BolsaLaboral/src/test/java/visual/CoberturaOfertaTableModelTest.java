package visual;

import logico.CoberturaOfertaDTO;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CoberturaOfertaTableModelTest {

    @Test
    public void exponeColumnasTipadasYNoEditables() {
        CoberturaOfertaTableModel model = new CoberturaOfertaTableModel(Arrays.asList(
                new CoberturaOfertaDTO("Analista", "Centro", "TI", 5, 2, 3, 40.0)));

        assertEquals(9, model.getColumnCount());
        assertEquals("Oferta", model.getColumnName(0));
        assertEquals("Vacantes pendientes", model.getColumnName(5));
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(Integer.class, model.getColumnClass(3));
        assertEquals(Double.class, model.getColumnClass(7));
        assertEquals(Integer.valueOf(3), model.getValueAt(0, 5));
        assertEquals(Double.valueOf(40.0), model.getValueAt(0, 7));
        for (int columna = 0; columna < model.getColumnCount(); columna++) {
            assertFalse(model.isCellEditable(0, columna));
        }
    }
}
