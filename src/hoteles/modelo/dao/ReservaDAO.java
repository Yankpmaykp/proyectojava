package hoteles.modelo.dao;

import hoteles.modelo.EstadoReserva;
import hoteles.modelo.HotelesException;
import hoteles.modelo.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de las reservas y sus líneas. */
public interface ReservaDAO {

    /**
     * Guarda la reserva y todas sus líneas como una única unidad (transacción): o se guarda
     * completa o no se guarda nada. Falla si alguna habitación está ocupada en esas fechas.
     * Devuelve el identificador asignado.
     */
    int crear(Reserva reserva) throws HotelesException;

    /** Cambia fechas y número de personas, revalidando la disponibilidad de las habitaciones. */
    void modificar(int id, LocalDate entrada, LocalDate salida, int numPersonas) throws HotelesException;

    void cancelar(int id) throws HotelesException;

    void finalizar(int id) throws HotelesException;

    Optional<Reserva> buscarPorId(int id) throws HotelesException;

    /** Búsqueda combinada (criterio nulo = ignorado), ordenada por fecha de entrada y id. */
    List<Reserva> buscar(Integer clienteId, Integer hotelId, LocalDate entradaDesde, LocalDate salidaHasta,
                         EstadoReserva estado) throws HotelesException;
}
