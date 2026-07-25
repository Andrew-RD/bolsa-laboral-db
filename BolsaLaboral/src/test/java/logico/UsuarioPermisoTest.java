package logico;

import exception.AutorizacionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UsuarioPermisoTest {

    private BolsaLaboral bolsa;
    private Usuario admin;
    private GestionUsuarioService servicio;

    @Before
    public void setUp() {
        bolsa = BolsaLaboral.getInstancia();
        bolsa.getUsuarios().clear();
        bolsa.getCentros().clear();
        admin = nuevoAdmin("admin", "admin@example.test");
        bolsa.regUsuario(admin);
        bolsa.setUsuarioActual(admin);
        servicio = new GestionUsuarioService(bolsa);
    }

    @After
    public void tearDown() {
        bolsa.getUsuarios().clear();
        bolsa.getCentros().clear();
        bolsa.setUsuarioActual(null);
    }

    @Test
    public void adminLegadoRecibeTodosLosPermisos() throws Exception {
        Usuario legado = new Usuario("Administrador", "clave", "Admin");
        simularCamposNuevosAusentes(legado);

        assertTrue(legado.migrarDatosDeserializados() > 0);
        assertEquals(RolUsuario.ADMINISTRADOR, legado.getRol());
        assertTrue(legado.isActivo());
        assertEquals(EnumSet.allOf(Permiso.class), legado.getPermisos());
    }

    @Test
    public void empleadoLegadoRecibePerfilPredeterminado() throws Exception {
        Usuario legado = new Usuario("Empleado", "clave", "Empleado");
        simularCamposNuevosAusentes(legado);

        legado.migrarDatosDeserializados();

        assertEquals(RolUsuario.EMPLEADO, legado.getRol());
        assertEquals(PermisosPorRol.predeterminados(RolUsuario.EMPLEADO), legado.getPermisos());
        assertFalse(legado.tienePermiso(Permiso.GESTIONAR_RESPALDOS));
        assertFalse(legado.tienePermiso(Permiso.GESTIONAR_USUARIOS));
    }

    @Test
    public void accionDirectaSinPermisoEsRechazadaSinModificarDatos() {
        Usuario empleado = new Usuario("Consulta", "consulta", "consulta@example.test",
                RolUsuario.EMPLEADO, true, "ClaveTemporal1".toCharArray());
        empleado.setPermisos(EnumSet.of(Permiso.CONSULTAR_CENTROS));
        bolsa.regUsuario(empleado);
        bolsa.setUsuarioActual(empleado);

        try {
            bolsa.registrarCentroTrabajo(new CentroEmpleador("CEN-X", "Centro", "Servicios",
                    "Distrito Nacional", "Santo Domingo de Guzmán", "8095550000",
                    "centro@example.test", "101010101"));
            fail("La acción debió ser rechazada.");
        } catch (AutorizacionException expected) {
            assertTrue(expected.getMessage().contains(Permiso.GESTIONAR_CENTROS.name()));
        }
        assertTrue(bolsa.getCentros().isEmpty());
    }

    @Test
    public void noPermiteDesactivarNiDegradarAlUltimoAdministrador() {
        try {
            servicio.modificar(admin, admin.getNombreCompleto(), admin.getNombreUsuario(),
                    admin.getCorreo(), RolUsuario.EMPLEADO, true,
                    EnumSet.allOf(Permiso.class));
            fail("No debe degradarse el último administrador.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("último administrador"));
        }
        assertEquals(RolUsuario.ADMINISTRADOR, admin.getRol());

        try {
            servicio.cambiarEstado(admin, false);
            fail("No debe desactivarse a sí mismo.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("sí mismo"));
        }
        assertTrue(admin.isActivo());
    }

    @Test
    public void usernameCorreoYConfirmacionSeValidanNormalizados() {
        servicio.registrar("Primera Persona", "Persona", "persona@example.test",
                "Clave1".toCharArray(), "Clave1".toCharArray(), RolUsuario.EMPLEADO,
                true, null);
        assertRegistroInvalido("Otra Persona", " persona ", "otro@example.test",
                "Clave1", "Clave1", "nombre");
        assertRegistroInvalido("Otra Persona", "otro", " PERSONA@example.test ",
                "Clave1", "Clave1", "correo");
        assertRegistroInvalido("Otra Persona", "otro", "otro@example.test",
                "Clave1", "Diferente", "coincidir");
    }

    @Test
    public void contrasenaNuevaNoQuedaEnTextoPlano() {
        Usuario usuario = servicio.registrar("Persona Segura", "segura", "segura@example.test",
                "Secreto temporal".toCharArray(), "Secreto temporal".toCharArray(),
                RolUsuario.EMPLEADO, true, null);

        assertNull(usuario.getContrasena());
        assertTrue(usuario.tieneContrasenaProtegida());
        assertNotNull(usuario.getPasswordSalt());
        assertNotNull(usuario.getPasswordHash());
        assertTrue(usuario.match("SEGURA", "Secreto temporal"));
        assertFalse(usuario.match("segura", "incorrecta"));
    }

    @Test
    public void passwordLegadoMigraSoloEnMemoriaTrasAutenticacionValida() throws Exception {
        Usuario legado = new Usuario("Legado", "inicial", "Empleado");
        set(legado, "passwordSalt", null);
        set(legado, "passwordHash", null);
        set(legado, "contrasena", "texto-legado");
        legado.migrarDatosDeserializados();

        assertFalse(legado.match("Legado", "incorrecta"));
        assertEquals("texto-legado", legado.getContrasena());
        assertTrue(legado.match("legado", "texto-legado"));
        assertNull(legado.getContrasena());
        assertTrue(legado.tieneContrasenaProtegida());
    }

    @Test
    public void migracionDeUsuarioLegadoVacioEsIdempotente() throws Exception {
        Usuario legado = new Usuario("temporal", "clave", "Empleado");
        set(legado, "nombreUsuario", "");
        set(legado, "nombreCompleto", null);
        set(legado, "identificador", null);
        set(legado, "fechaCreacion", null);
        set(legado, "permisos", null);

        assertTrue(legado.migrarDatosDeserializados() > 0);
        assertEquals(0, legado.migrarDatosDeserializados());
    }

    private void assertRegistroInvalido(String nombre, String username, String correo,
            String password, String confirmacion, String textoEsperado) {
        int antes = bolsa.getUsuarios().size();
        try {
            servicio.registrar(nombre, username, correo, password.toCharArray(),
                    confirmacion.toCharArray(), RolUsuario.EMPLEADO, true, null);
            fail("El registro debía rechazarse.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains(textoEsperado));
        }
        assertEquals(antes, bolsa.getUsuarios().size());
    }

    private Usuario nuevoAdmin(String username, String correo) {
        return new Usuario("Administrador", username, correo, RolUsuario.ADMINISTRADOR,
                true, "ClaveTemporal1".toCharArray());
    }

    private void simularCamposNuevosAusentes(Usuario usuario) throws Exception {
        set(usuario, "identificador", null);
        set(usuario, "nombreCompleto", null);
        set(usuario, "correo", null);
        set(usuario, "rol", null);
        set(usuario, "activo", null);
        set(usuario, "permisos", null);
        set(usuario, "fechaCreacion", null);
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
