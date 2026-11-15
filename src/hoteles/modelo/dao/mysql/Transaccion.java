package hoteles.modelo.dao.mysql;

import hoteles.modelo.HotelesException;
import hoteles.util.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta un bloque de operaciones JDBC como una única transacción:
 * {@code setAutoCommit(false)} al empezar, {@code commit()} si todo va bien y
 * {@code rollback()} si algo falla, restaurando siempre el estado de la conexión antes de cerrarla.
 * <p>
 * Se usa el nivel READ COMMITTED para que las comprobaciones de disponibilidad vean siempre las
 * reservas ya confirmadas por otras transacciones (junto con el bloqueo de fila de las habitaciones
 * que hacen los procedimientos almacenados).
 */
final class Transaccion {

    /** Operación que usa la conexión transaccional y puede lanzar {@link SQLException}. */
    @FunctionalInterface
    interface Operacion<T> {
        T ejecutar(Connection con) throws SQLException;
    }

    private Transaccion() {
    }

    static <T> T ejecutar(String contexto, Operacion<T> operacion) throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            try {
                con.setAutoCommit(false);
                con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                T resultado = operacion.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException e) {
                deshacer(con);
                throw TraductorErrores.traducir(e, contexto);
            } catch (RuntimeException e) {
                deshacer(con);
                throw e;
            }
        } catch (SQLException e) {   // fallo al cerrar la conexión
            throw TraductorErrores.traducir(e, contexto);
        }
    }

    private static void deshacer(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e) {
            System.err.println("No se pudo deshacer la transacción: " + e.getMessage());
        }
    }
}
