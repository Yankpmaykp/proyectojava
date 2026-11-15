package hoteles.modelo.dao;

import hoteles.modelo.CategoriaHabitacion;
import hoteles.modelo.Habitacion;
import hoteles.modelo.HotelesException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de las habitaciones. */
public interface HabitacionDAO {

    int insertar(Habitacion habitacion) throws HotelesException;

    Optional<Habitacion> buscarPorId(int id) throws HotelesException;

    List<Habitacion> listarPorHotel(int hotelId) throws HotelesException;

    List<Habitacion> listarPorCategoria(int hotelId, CategoriaHabitacion categoria) throws HotelesException;

    /**
     * Habitaciones en servicio libres en todo el intervalo [entrada, salida).
     *
     * @param numPersonas si es mayor que 0, solo las que tienen capacidad suficiente
     */
    List<Habitacion> listarDisponibles(int hotelId, LocalDate entrada, LocalDate salida, int numPersonas)
            throws HotelesException;
}
