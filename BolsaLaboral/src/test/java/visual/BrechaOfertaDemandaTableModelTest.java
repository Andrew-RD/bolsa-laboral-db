package visual;

import logico.BrechaOfertaDemandaDTO;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BrechaOfertaDemandaTableModelTest {

    @Test
    public void exponeColumnasTipadasYValoresCalculados() {
        BrechaOfertaDemandaTableModel model = new BrechaOfertaDemandaTableModel(
                Arrays.asList(new BrechaOfertaDemandaDTO("TI", 3, 1)));

        assertEquals(1, model.getRowCount());
        assertEquals(5, model.getColumnCount());
        assertEquals("Área laboral", model.getColumnName(0));
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(Integer.class, model.getColumnClass(1));
        assertEquals("TI", model.getValueAt(0, 0));
        assertEquals(Integer.valueOf(3), model.getValueAt(0, 1));
        assertEquals(Integer.valueOf(1), model.getValueAt(0, 2));
        assertEquals(Integer.valueOf(-2), model.getValueAt(0, 3));
        assertEquals(BrechaOfertaDemandaDTO.DIAGNOSTICO_ESCASEZ,
                model.getValueAt(0, 4));
    }

    @Test
    public void ningunaCeldaEsEditable() {
        BrechaOfertaDemandaTableModel model = new BrechaOfertaDemandaTableModel(
                Arrays.asList(new BrechaOfertaDemandaDTO("Salud", 0, 0)));

        for (int column = 0; column < model.getColumnCount(); column++) {
            assertFalse(model.isCellEditable(0, column));
        }
    }
}
