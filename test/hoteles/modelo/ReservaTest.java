package hoteles.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pruebas de {@link Reserva#calcularImporte()}: importe = suma de precio por noche x noches. */
class ReservaTest {

    private Hotel hotel;
    private Habitacion doble;
    private Habitacion individual;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        hotel = new Hotel(1, "Hotel Test", "Calle 1", "Valencia", "960000000", "test@hotel.com", 3);
        doble = new Habitacion(1, "102", 1, CategoriaHabitacion.DOBLE, new BigDecimal("80.00"), null);
        individual = new Habitacion(2, "101", 1, CategoriaHabitacion.INDIVIDUAL, new BigDecimal("55.00"), null);
        hotel.addHabitacion(doble);
        hotel.addHabitacion(individual);
        cliente = new Cliente(1, "12345678Z", "Ana", "García", "ana@email.com", "600000000", "C/ Mayor");
    }

    @Test
    void importeDeUnaHabitacionEsPrecioPorNoches() {
        Reserva reserva = new Reserva(1, cliente, hotel, LocalDate.of(2030, 5, 1), LocalDate.of(2030, 5, 4), 2);
        reserva.addHabitacion(doble);

        assertEquals(0, new BigDecimal("240.00").compareTo(reserva.calcularImporte()));
    }

    @Test
    void importeSumaTodasLasHabitaciones() {
        Reserva reserva = new Reserva(1, cliente, hotel, LocalDate.of(2030, 5, 1), LocalDate.of(2030, 5, 4), 3);
        reserva.addHabitacion(doble);
        reserva.addHabitacion(individual);

        // (80 + 55) x 3 noches
        assertEquals(0, new BigDecimal("405.00").compareTo(reserva.calcularImporte()));
    }

    @Test
    void cambiarLaTarifaNoAlteraElImporteYaCongelado() {
        Reserva reserva = new Reserva(1, cliente, hotel, LocalDate.of(2030, 5, 1), LocalDate.of(2030, 5, 3), 2);
        reserva.addHabitacion(doble);
        doble.setPrecioNoche(new BigDecimal("999.00"));

        assertEquals(0, new BigDecimal("160.00").compareTo(reserva.calcularImporte()));
    }

    @Test
    void sinHabitacionesElImporteEsCero() {
        Reserva reserva = new Reserva(1, cliente, hotel, LocalDate.of(2030, 5, 1), LocalDate.of(2030, 5, 3), 1);

        assertEquals(0, BigDecimal.ZERO.compareTo(reserva.calcularImporte()));
    }
}
