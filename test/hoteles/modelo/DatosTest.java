package hoteles.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de {@link Datos#crearReserva}: no puede haber dos reservas sobre la misma habitación y fechas. */
class DatosTest {

    private static final LocalDate DIA_1 = LocalDate.of(2030, 6, 1);
    private static final LocalDate DIA_5 = LocalDate.of(2030, 6, 5);
    private static final LocalDate DIA_10 = LocalDate.of(2030, 6, 10);

    private Datos datos;
    private int hotelId;
    private int clienteId;
    private int habitacionId;

    @BeforeEach
    void preparar() throws HotelesException {
        datos = new Datos();
        hotelId = datos.altaHotel("Hotel Test", "Calle 1", "Valencia", "960000000", "test@hotel.com", 3).getId();
        habitacionId = datos.altaHabitacion(hotelId, "101", 1, CategoriaHabitacion.DOBLE,
                new BigDecimal("80.00")).getId();
        clienteId = datos.altaCliente("12345678Z", "Ana", "García", "ana@email.com", "600000000", "C/ Mayor").getId();
    }

    @Test
    void creaReservaConfirmadaConSuImporte() throws HotelesException {
        Reserva reserva = datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(0, new BigDecimal("320.00").compareTo(reserva.calcularImporte()));
    }

    @Test
    void rechazaReservaQueSolapaConOtraDeLaMismaHabitacion() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.crearReserva(clienteId, hotelId, LocalDate.of(2030, 6, 3), DIA_10, 2, List.of(habitacionId)));
    }

    @Test
    void permiteEntrarElMismoDiaQueSaleOtraReserva() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        Reserva siguiente = datos.crearReserva(clienteId, hotelId, DIA_5, DIA_10, 2, List.of(habitacionId));

        assertEquals(DIA_5, siguiente.getFechaEntrada());
    }

    @Test
    void unaReservaCanceladaLiberaLaHabitacion() throws HotelesException {
        Reserva primera = datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
        datos.cancelarReserva(primera.getId());

        assertTrue(datos.habitacionesDisponibles(hotelId, DIA_1, DIA_5, 0).stream()
                .anyMatch(h -> h.getId() == habitacionId));
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
    }

    @Test
    void rechazaFechaDeSalidaAnteriorALaEntrada() {
        assertThrows(ValidacionException.class, () ->
                datos.crearReserva(clienteId, hotelId, DIA_5, DIA_1, 2, List.of(habitacionId)));
    }

    @Test
    void rechazaMasPersonasQueCapacidad() {
        assertThrows(ValidacionException.class, () ->
                datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 3, List.of(habitacionId)));
    }

    @Test
    void rechazaClienteInexistente() {
        assertThrows(ElementoNoEncontradoException.class, () ->
                datos.crearReserva(99, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId)));
    }

    @Test
    void modificarConChoqueDejaLaReservaIntacta() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
        Reserva segunda = datos.crearReserva(clienteId, hotelId, DIA_5, DIA_10, 2, List.of(habitacionId));

        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.modificarReserva(segunda.getId(), LocalDate.of(2030, 6, 3), DIA_10, 2));

        assertEquals(DIA_5, segunda.getFechaEntrada());
        assertFalse(segunda.getFechaEntrada().isBefore(DIA_5));
    }
}
