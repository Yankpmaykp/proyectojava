package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.EstadoReserva;
import hoteles.modelo.Habitacion;
import hoteles.modelo.HotelesException;
import hoteles.modelo.LineaReserva;
import hoteles.modelo.Reserva;
import hoteles.modelo.ValidacionException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Controlador de reservas. */
public class ReservaController {

    private final Datos datos;

    public ReservaController(Datos datos) {
        this.datos = datos;
    }

    public String crearReserva(int clienteId, int hotelId, LocalDate entrada, LocalDate salida,
                               int numPersonas, List<Integer> idsHabitaciones) throws HotelesException {
        return detalle(datos.crearReserva(clienteId, hotelId, entrada, salida,
                numPersonas, idsHabitaciones));
    }

    public String modificarReserva(int id, LocalDate entrada, LocalDate salida, int numPersonas)
            throws HotelesException {
        return detalle(datos.modificarReserva(id, entrada, salida, numPersonas));
    }

    public String cancelarReserva(int id) throws HotelesException {
        return detalle(datos.cancelarReserva(id));
    }

    public String finalizarReserva(int id) throws HotelesException {
        return detalle(datos.finalizarReserva(id));
    }

    public String detalleReserva(int id) throws HotelesException {
        return detalle(datos.obtenerReserva(id));
    }

    public List<String> reservasDeCliente(int clienteId) throws HotelesException {
        return formatoReservas(datos.reservasDeCliente(clienteId));
    }

    /** Cualquier criterio nulo se ignora; {@code estado} es el nombre de un {@link EstadoReserva}. */
    public List<String> buscarReservas(Integer clienteId, Integer hotelId, LocalDate entradaDesde,
                                       LocalDate salidaHasta, String estado) throws HotelesException {
        return formatoReservas(datos.buscarReservas(clienteId, hotelId, entradaDesde, salidaHasta,
                parseEstado(estado)));
    }

    public List<String> estadosReserva() {
        List<String> nombres = new ArrayList<>();
        for (EstadoReserva e : EstadoReserva.values()) {
            nombres.add(e.name());
        }
        return nombres;
    }

    private static EstadoReserva parseEstado(String texto) throws ValidacionException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return EstadoReserva.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("El estado '" + texto + "' no existe. Valores válidos: "
                    + List.of(EstadoReserva.values()) + ".");
        }
    }

    private static String resumen(Reserva r) {
        return "[" + r.getId() + "] " + r.getCliente().getNombreCompleto() + " | " + r.getHotel().getNombre()
                + " | " + r.getFechaEntrada() + " -> " + r.getFechaSalida() + " (" + r.getNumNoches()
                + " noches) | " + r.getNumPersonas() + " pers. | " + r.getEstado()
                + " | " + Formato.dinero(r.calcularImporte());
    }

    private static List<String> formatoReservas(List<Reserva> reservas) {
        List<String> lineas = new ArrayList<>();
        for (Reserva r : reservas) {
            lineas.add(resumen(r));
        }
        return lineas;
    }

    private static String detalle(Reserva r) {
        StringBuilder sb = new StringBuilder(resumen(r));
        for (LineaReserva linea : r.getLineas()) {
            Habitacion h = linea.getHabitacion();
            sb.append(System.lineSeparator()).append("      - Hab. ").append(h.getNumero()).append(" ")
                    .append(h.getCategoria()).append(": ").append(Formato.dinero(linea.getPrecioNoche()))
                    .append(" x ").append(r.getNumNoches()).append(" noches = ")
                    .append(Formato.dinero(linea.calcularImporte(r.getNumNoches())));
        }
        return sb.toString();
    }
}
