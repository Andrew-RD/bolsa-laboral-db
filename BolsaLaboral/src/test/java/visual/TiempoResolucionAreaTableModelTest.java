package visual;

import logico.TiempoResolucionAreaDTO;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TiempoResolucionAreaTableModelTest {

    @Test
    public void exponeColumnasTipadasYNoEditables() {
        TiempoResolucionAreaTableModel model = new TiempoResolucionAreaTableModel(Arrays.asList(
                new TiempoResolucionAreaDTO("TI", 4, 2, 2, 1, 3.5, 50.0)));

        assertEquals(8, model.getColumnCount());
        assertEquals("Área laboral", model.getColumnName(0));
        assertEquals("Promedio (días)", model.getColumnName(5));
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(Integer.class, model.getColumnClass(1));
        assertEquals(Double.class, model.getColumnClass(5));
        assertEquals(Double.valueOf(50.0), model.getValueAt(0, 6));
        for (int columna = 0; columna < model.getColumnCount(); columna++) {
            assertFalse(model.isCellEditable(0, columna));
        }
    }
}
