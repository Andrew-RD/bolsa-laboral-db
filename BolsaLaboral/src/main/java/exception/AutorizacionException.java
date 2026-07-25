package exception;

import logico.Permiso;

/** Rechazo explícito de una operación sensible invocada sin autorización. */
public class AutorizacionException extends SecurityException {

    private static final long serialVersionUID = 1L;

    public AutorizacionException(Permiso permiso) {
        super("Acción no autorizada. Se requiere el permiso " + permiso.name() + ".");
    }
}
