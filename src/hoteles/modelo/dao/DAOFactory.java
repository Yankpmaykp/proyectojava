package hoteles.modelo.dao;

import hoteles.modelo.HotelesException;
import hoteles.modelo.dao.mysql.MySQLDAOFactory;

/**
 * Patrón Factory (Abstract Factory) para obtener los DAOs sin conocer el motor de base de datos.
 * Las capas superiores solo dependen de esta clase y de las interfaces DAO; para soportar otro
 * motor basta con crear una subclase nueva y añadir un caso a {@link #getDAOFactory(String)}.
 */
public abstract class DAOFactory {

    public abstract ClienteDAO getClienteDAO();

    public abstract HotelDAO getHotelDAO();

    public abstract HabitacionDAO getHabitacionDAO();

    public abstract ReservaDAO getReservaDAO();

    /** Devuelve la factoría del tipo de almacén indicado (p. ej. "MYSQL"). */
    public static DAOFactory getDAOFactory(String tipo) throws HotelesException {
        if (tipo != null && tipo.trim().equalsIgnoreCase("MYSQL")) {
            return new MySQLDAOFactory();
        }
        throw new HotelesException("Tipo de almacén de datos no soportado: " + tipo);
    }
}
