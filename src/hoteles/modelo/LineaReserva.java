package hoteles.modelo;

import java.math.BigDecimal;

/**
 * Cada una de las habitaciones incluidas en una reserva.
 * Guarda el precio por noche aplicado en el momento de reservar, de modo que
 * un cambio posterior de tarifa no altera el importe de reservas ya hechas.
 */
public class LineaReserva {

    private Habitacion habitacion;
    private BigDecimal precioNoche;

    public LineaReserva() {
    }

    public LineaReserva(Habitacion habitacion) {
        this.habitacion = habitacion;
        this.precioNoche = habitacion.getPrecioNoche();
    }

    public BigDecimal calcularImporte(long noches) {
        return precioNoche.multiply(BigDecimal.valueOf(noches));
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public BigDecimal getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(BigDecimal precioNoche) {
        this.precioNoche = precioNoche;
    }

    @Override
    public String toString() {
        return "LineaReserva{habitacion=" + habitacion.getNumero() + " (" + habitacion.getCategoria()
                + "), precioNoche=" + precioNoche + " €}";
    }
}
