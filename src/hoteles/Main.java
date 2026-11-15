package hoteles;

import hoteles.controlador.ClienteController;
import hoteles.controlador.HotelController;
import hoteles.controlador.ReservaController;
import hoteles.modelo.Datos;
import hoteles.modelo.HotelesException;
import hoteles.modelo.dao.DAOFactory;
import hoteles.util.ConexionBD;
import hoteles.vista.VistaPrincipal;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Punto de entrada: comprueba la conexión con la base de datos, obtiene la factoría de DAOs,
 * crea el modelo, los controladores y la vista y arranca el menú.
 */
public class Main {

    public static void main(String[] args) {
        try (Connection prueba = ConexionBD.getConexion()) {
            System.out.println("Conectado a " + prueba.getMetaData().getURL().split("[?]")[0]);
            DAOFactory factoria = DAOFactory.getDAOFactory(ConexionBD.getTipo());
            Datos datos = new Datos(factoria);
            new VistaPrincipal(new ClienteController(datos), new HotelController(datos),
                    new ReservaController(datos)).iniciar();
        } catch (HotelesException | SQLException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("Cree la base de datos con crear_bd.bat y revise db.properties.");
            System.exit(1);
        }
    }
}
