package hoteles.modelo.dao;

import hoteles.modelo.Hotel;
import hoteles.modelo.HotelesException;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de los hoteles. Los hoteles devueltos llevan cargadas sus habitaciones. */
public interface HotelDAO {

    int insertar(Hotel hotel) throws HotelesException;

    Optional<Hotel> buscarPorId(int id) throws HotelesException;

    List<Hotel> listar() throws HotelesException;

    List<Hotel> buscarPorCiudad(String ciudad) throws HotelesException;
}
