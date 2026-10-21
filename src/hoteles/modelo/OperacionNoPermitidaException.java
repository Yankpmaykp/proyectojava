package hoteles.modelo;

/** Se lanza cuando una operación no se puede realizar por el estado actual de los datos. */
public class OperacionNoPermitidaException extends HotelesException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
