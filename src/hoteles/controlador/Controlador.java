package hoteles.controlador;

import hoteles.modelo.CategoriaHabitacion;
import hoteles.modelo.Cliente;
import hoteles.modelo.Datos;
import hoteles.modelo.EstadoReserva;
import hoteles.modelo.Habitacion;
import hoteles.modelo.HotelesException;
import hoteles.modelo.Hotel;
import hoteles.modelo.LineaReserva;
import hoteles.modelo.Reserva;
import hoteles.modelo.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Único punto de acceso de la vista al modelo. Recibe datos primitivos o cadenas de la vista,
 * los convierte a objetos del modelo, invoca a {@link Datos} y devuelve el resultado ya
 * formateado como texto, de modo que la vista nunca maneja clases del modelo.
 * Los errores de negocio se propagan como {@link HotelesException}.
 */
public class Controlador {

    private final Datos datos;

    public Controlador(Datos datos) {
        this.datos = datos;
    }

    // ------------------------------------------------------------------ clientes

    public String altaCliente(String dni, String nombre, String apellidos, String email,
                              String telefono, String direccion) throws HotelesException {
        return formatoCliente(datos.altaCliente(dni, nombre, apellidos, email, telefono, direccion));
    }

    public String modificarCliente(int id, String nombre, String apellidos, String email,
                                   String telefono, String direccion) throws HotelesException {
        return formatoCliente(datos.modificarCliente(id, nombre, apellidos, email, telefono, direccion));
    }

    public void eliminarCliente(int id) throws HotelesException {
        datos.eliminarCliente(id);
    }

    public String detalleCliente(int id) throws HotelesException {
        return formatoCliente(datos.obtenerCliente(id));
    }

    public List<String> listarClientes() {
        return formatoClientes(datos.listarClientes());
    }

    public List<String> buscarClientes(String texto) {
        return formatoClientes(datos.buscarClientes(texto));
    }

    public List<String> reservasDeCliente(int clienteId) throws HotelesException {
        return formatoReservas(datos.reservasDeCliente(clienteId));
    }

    // ------------------------------------------------------------------ hoteles y habitaciones

    public String altaHotel(String nombre, String direccion, String ciudad, String telefono,
                            String email, int estrellas) throws HotelesException {
        return formatoHotel(datos.altaHotel(nombre, direccion, ciudad, telefono, email, estrellas));
    }

    public String altaHabitacion(int hotelId, String numero, int planta, String categoria,
                                 BigDecimal precioNoche) throws HotelesException {
        return formatoHabitacion(datos.altaHabitacion(hotelId, numero, planta,
                parseCategoria(categoria), precioNoche));
    }

    public List<String> listarHoteles() {
        List<String> lineas = new ArrayList<>();
        for (Hotel h : datos.listarHoteles()) {
            lineas.add(formatoHotel(h));
        }
        return lineas;
    }

    public List<String> buscarHotelesPorCiudad(String ciudad) {
        List<String> lineas = new ArrayList<>();
        for (Hotel h : datos.buscarHotelesPorCiudad(ciudad)) {
            lineas.add(formatoHotel(h));
        }
        return lineas;
    }

    public List<String> habitacionesDeHotel(int hotelId) throws HotelesException {
        return formatoHabitaciones(datos.habitacionesDeHotel(hotelId));
    }

    public List<String> habitacionesPorCategoria(int hotelId, String categoria) throws HotelesException {
        return formatoHabitaciones(datos.habitacionesPorCategoria(hotelId, parseCategoria(categoria)));
    }

    public List<String> resumenCategorias(int hotelId) throws HotelesException {
        List<String> lineas = new ArrayList<>();
        for (Map.Entry<CategoriaHabitacion, Integer> e : datos.contarHabitacionesPorCategoria(hotelId).entrySet()) {
            lineas.add(e.getKey() + " (" + e.getKey().getCapacidadMaxima() + " pers.): " + e.getValue());
        }
        return lineas;
    }

    /** Habitaciones libres en las fechas indicadas; {@code numPersonas} 0 equivale a no filtrar. */
    public List<String> habitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida,
                                                int numPersonas) throws HotelesException {
        return formatoHabitaciones(datos.habitacionesDisponibles(hotelId, entrada, salida, numPersonas));
    }

    public int contarHabitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida)
            throws HotelesException {
        return datos.contarHabitacionesDisponibles(hotelId, entrada, salida);
    }

    public List<String> categorias() {
        List<String> nombres = new ArrayList<>();
        for (CategoriaHabitacion c : CategoriaHabitacion.values()) {
            nombres.add(c.name());
        }
        return nombres;
    }

    public List<String> estadosReserva() {
        List<String> nombres = new ArrayList<>();
        for (EstadoReserva e : EstadoReserva.values()) {
            nombres.add(e.name());
        }
        return nombres;
    }

    // ------------------------------------------------------------------ reservas

    public String crearReserva(int clienteId, int hotelId, LocalDate entrada, LocalDate salida,
                               int numPersonas, List<Integer> idsHabitaciones) throws HotelesException {
        return detalleReserva(datos.crearReserva(clienteId, hotelId, entrada, salida,
                numPersonas, idsHabitaciones));
    }

    public String modificarReserva(int id, LocalDate entrada, LocalDate salida, int numPersonas)
            throws HotelesException {
        return detalleReserva(datos.modificarReserva(id, entrada, salida, numPersonas));
    }

    public String cancelarReserva(int id) throws HotelesException {
        return detalleReserva(datos.cancelarReserva(id));
    }

    public String finalizarReserva(int id) throws HotelesException {
        return detalleReserva(datos.finalizarReserva(id));
    }

    public String detalleReserva(int id) throws HotelesException {
        return detalleReserva(datos.obtenerReserva(id));
    }

    /** Cualquier criterio nulo se ignora; {@code estado} es el nombre de un {@link EstadoReserva}. */
    public List<String> buscarReservas(Integer clienteId, Integer hotelId, LocalDate entradaDesde,
                                       LocalDate salidaHasta, String estado) throws HotelesException {
        return formatoReservas(datos.buscarReservas(clienteId, hotelId, entradaDesde, salidaHasta,
                parseEstado(estado)));
    }

    // ------------------------------------------------------------------ conversiones y formato

    private static CategoriaHabitacion parseCategoria(String texto) throws ValidacionException {
        try {
            return CategoriaHabitacion.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidacionException("La categoría '" + texto + "' no existe. Valores válidos: "
                    + List.of(CategoriaHabitacion.values()) + ".");
        }
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

    private static String dinero(BigDecimal importe) {
        return importe.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " €";
    }

    private static String formatoCliente(Cliente c) {
        return "[" + c.getId() + "] " + c.getNombreCompleto() + " | DNI " + c.getDni()
                + " | " + c.getEmail() + " | tel. " + c.getTelefono()
                + " | " + c.getDireccion();
    }

    private static List<String> formatoClientes(List<Cliente> clientes) {
        List<String> lineas = new ArrayList<>();
        for (Cliente c : clientes) {
            lineas.add(formatoCliente(c));
        }
        return lineas;
    }

    private static String formatoHotel(Hotel h) {
        return "[" + h.getId() + "] " + h.getNombre() + " (" + h.getEstrellas() + " estrellas) | "
                + h.getCiudad() + " | " + h.getDireccion() + " | " + h.getNumHabitaciones() + " habitaciones";
    }

    private static String formatoHabitacion(Habitacion h) {
        return "[" + h.getId() + "] Hab. " + h.getNumero() + " | planta " + h.getPlanta() + " | "
                + h.getCategoria() + " (" + h.getCapacidad() + " pers.) | " + dinero(h.getPrecioNoche())
                + "/noche" + (h.isEnServicio() ? "" : " | FUERA DE SERVICIO");
    }

    private static List<String> formatoHabitaciones(List<Habitacion> habitaciones) {
        List<String> lineas = new ArrayList<>();
        for (Habitacion h : habitaciones) {
            lineas.add(formatoHabitacion(h));
        }
        return lineas;
    }

    private static String resumenReserva(Reserva r) {
        return "[" + r.getId() + "] " + r.getCliente().getNombreCompleto() + " | " + r.getHotel().getNombre()
                + " | " + r.getFechaEntrada() + " -> " + r.getFechaSalida() + " (" + r.getNumNoches()
                + " noches) | " + r.getNumPersonas() + " pers. | " + r.getEstado()
                + " | " + dinero(r.calcularImporte());
    }

    private static List<String> formatoReservas(List<Reserva> reservas) {
        List<String> lineas = new ArrayList<>();
        for (Reserva r : reservas) {
            lineas.add(resumenReserva(r));
        }
        return lineas;
    }

    private static String detalleReserva(Reserva r) {
        StringBuilder sb = new StringBuilder(resumenReserva(r));
        for (LineaReserva linea : r.getLineas()) {
            Habitacion h = linea.getHabitacion();
            sb.append(System.lineSeparator()).append("      - Hab. ").append(h.getNumero()).append(" ")
                    .append(h.getCategoria()).append(": ").append(dinero(linea.getPrecioNoche()))
                    .append(" x ").append(r.getNumNoches()).append(" noches = ")
                    .append(dinero(linea.calcularImporte(r.getNumNoches())));
        }
        return sb.toString();
    }
}
