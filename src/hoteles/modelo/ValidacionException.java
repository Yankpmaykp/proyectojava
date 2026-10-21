package hoteles.modelo;

/**
 * Se lanza cuando los datos de una entidad no son válidos.
 * El mensaje está pensado para mostrarse directamente al usuario.
 */
public class ValidacionException extends HotelesException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
