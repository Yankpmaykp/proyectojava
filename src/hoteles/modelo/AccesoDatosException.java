package hoteles.modelo;

/**
 * Fallo técnico al acceder al almacén de datos (conexión caída, SQL incorrecto, etc.).
 * Envuelve la excepción original ({@link java.sql.SQLException}) para que ni la vista ni el
 * controlador dependan de JDBC, y conserva su causa para poder diagnosticar el problema.
 */
public class AccesoDatosException extends HotelesException {

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje);
        initCause(causa);
    }
}
