package visual;

import org.junit.Test;
import org.junit.BeforeClass;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class UIUtilsValueIconTest {

    @BeforeClass
    public static void useHeadlessToolkit() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void estadosCanonicosUsanSusRecursosExplicitos() {
        assertSame(UIUtils.icon("enviada.png"), UIUtils.valueIcon("Enviada"));
        assertSame(UIUtils.icon("aprobada.png"), UIUtils.valueIcon(" Aprobada "));
        assertSame(UIUtils.icon("rechazada.png"), UIUtils.valueIcon("Rechazada"));
        assertNull(UIUtils.class.getResource("/recursos/aprovada.png"));
    }

    @Test
    public void valorDesconocidoUsaIconoNoDefinidoSinExcepcion() {
        assertSame(UIUtils.icon("nodefinido.png"), UIUtils.valueIcon("Estado legado desconocido"));
        assertSame(UIUtils.icon("nodefinido.png"), UIUtils.valueIcon(null));
    }
}
