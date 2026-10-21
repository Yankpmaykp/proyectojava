package hoteles.modelo;

/**
 * Excepción comprobada base de la aplicación. Todos los errores de negocio la extienden,
 * de modo que la vista puede capturarlos de forma unificada y mostrar su mensaje al usuario.
 */
public class HotelesException extends Exception {

    public HotelesException(String mensaje) {
        super(mensaje);
    }
}
