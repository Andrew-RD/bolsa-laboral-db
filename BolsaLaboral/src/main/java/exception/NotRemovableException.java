package exception;

public class NotRemovableException extends Exception{

    public NotRemovableException() {
        super("No se puede eliminar");
    }

    public NotRemovableException(String message) {
        super(message);
    }

    public NotRemovableException(String message, Throwable cause) {
        super(message, cause);
    }
}