package exception;

public class NotRemovableException extends Exception{

	public NotRemovableException() {
		super("No se puede eliminar");
	}

    public NotRemovableException(String message) {
        super(message);
    }
}
