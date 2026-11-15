package hoteles.modelo.dao.mysql;

import hoteles.modelo.dao.ClienteDAO;
import hoteles.modelo.dao.DAOFactory;
import hoteles.modelo.dao.HabitacionDAO;
import hoteles.modelo.dao.HotelDAO;
import hoteles.modelo.dao.ReservaDAO;

/** Factoría concreta que crea los DAOs que trabajan contra MySQL mediante JDBC. */
public class MySQLDAOFactory extends DAOFactory {

    @Override
    public ClienteDAO getClienteDAO() {
        return new ClienteDAOMySQL();
    }

    @Override
    public HotelDAO getHotelDAO() {
        return new HotelDAOMySQL();
    }

    @Override
    public HabitacionDAO getHabitacionDAO() {
        return new HabitacionDAOMySQL();
    }

    @Override
    public ReservaDAO getReservaDAO() {
        return new ReservaDAOMySQL();
    }
}
