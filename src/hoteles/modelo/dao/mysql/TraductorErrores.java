package hoteles.modelo.dao.mysql;

import hoteles.modelo.AccesoDatosException;
import hoteles.modelo.ElementoDuplicadoException;
import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.HabitacionNoDisponibleException;
import hoteles.modelo.HotelesException;
import hoteles.modelo.OperacionNoPermitidaException;
import hoteles.modelo.ValidacionException;

import java.sql.SQLException;

/**
 * Convierte los errores de MySQL en las excepciones de negocio de la aplicación, de modo que
 * las capas superiores nunca ven una {@link SQLException}. Los códigos 5000x son los que lanzan
 * con {@code SIGNAL} los procedimientos almacenados (ver sql/01_schema.sql).
 */
final class TraductorErrores {

    private static final int HABITACION_NO_DISPONIBLE = 50001;
    private static final int NO_ENCONTRADO = 50002;
    private static final int OPERACION_NO_PERMITIDA = 50003;
    private static final int CLIENTE_CON_RESERVAS = 50004;
    private static final int DATOS_NO_VALIDOS = 50005;
    private static final int ENTRADA_DUPLICADA = 1062;
    private static final int FK_HIJOS_EXISTEN = 1451;
    private static final int FK_PADRE_NO_EXISTE = 1452;
    private static final int CHECK_INCUMPLIDO = 3819;

    private TraductorErrores() {
    }

    static HotelesException traducir(SQLException e, String contexto) {
        return switch (e.getErrorCode()) {
            case HABITACION_NO_DISPONIBLE -> new HabitacionNoDisponibleException(e.getMessage());
            case NO_ENCONTRADO -> new ElementoNoEncontradoException(e.getMessage());
            case OPERACION_NO_PERMITIDA, CLIENTE_CON_RESERVAS -> new OperacionNoPermitidaException(e.getMessage());
            case DATOS_NO_VALIDOS -> new ValidacionException(e.getMessage());
            case ENTRADA_DUPLICADA -> new ElementoDuplicadoException(
                    "Ya existe un registro con los mismos datos únicos (DNI o número de habitación).");
            case FK_HIJOS_EXISTEN -> new OperacionNoPermitidaException(
                    "No se puede completar la operación: hay datos que dependen de este registro.");
            case FK_PADRE_NO_EXISTE -> new ElementoNoEncontradoException(
                    "No existe un elemento relacionado (cliente, hotel o habitación).");
            case CHECK_INCUMPLIDO -> new ValidacionException(
                    "Algún dato no cumple las restricciones de la base de datos.");
            default -> new AccesoDatosException(
                    "Error de acceso a la base de datos al " + contexto + ": " + e.getMessage(), e);
        };
    }
}
