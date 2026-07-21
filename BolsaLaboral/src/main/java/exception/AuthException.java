package exception;

public class AuthException extends Exception {

    public AuthException() {
        super("Usuario o contraseña incorrectos.");
    }

    public AuthException(String message) {
        super(message);
    }
}
