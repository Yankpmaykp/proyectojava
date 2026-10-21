package hoteles.modelo;

import java.time.LocalDate;

/** Se lanza cuando una habitación ya está ocupada por otra reserva en las fechas pedidas. */
public class HabitacionNoDisponibleException extends HotelesException {

    public HabitacionNoDisponibleException(Habitacion habitacion, LocalDate entrada, LocalDate salida) {
        super("La habitación " + habitacion.getNumero() + " no está disponible del "
                + entrada + " al " + salida + ".");
    }
}
