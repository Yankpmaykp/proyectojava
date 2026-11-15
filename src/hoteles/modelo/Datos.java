package hoteles.modelo;

import hoteles.modelo.dao.ClienteDAO;
import hoteles.modelo.dao.DAOFactory;
import hoteles.modelo.dao.HabitacionDAO;
import hoteles.modelo.dao.HotelDAO;
import hoteles.modelo.dao.ReservaDAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fachada del modelo: contiene las reglas de negocio y delega la persistencia en los DAOs.
 * <p>
 * En el Producto 2 esta clase guardaba los datos en colecciones en memoria; ahora solo conoce las
 * interfaces DAO obtenidas de una {@link DAOFactory}, de modo que el controlador y la vista no
 * cambian y el motor de base de datos puede sustituirse sin tocar esta clase.
 * Las comprobaciones que dependen del estado compartido de la BD (solapamiento de reservas,
 * estado de la reserva) se repiten en los procedimientos almacenados, dentro de una transacción,
 * porque es el único sitio donde son seguras frente a accesos simultáneos.
 */
public class Datos {

    private final ClienteDAO clienteDAO;
    private final HotelDAO hotelDAO;
    private final HabitacionDAO habitacionDAO;
    private final ReservaDAO reservaDAO;

    public Datos(DAOFactory factoria) {
        this.clienteDAO = factoria.getClienteDAO();
        this.hotelDAO = factoria.getHotelDAO();
        this.habitacionDAO = factoria.getHabitacionDAO();
        this.reservaDAO = factoria.getReservaDAO();
    }

    // ------------------------------------------------------------------ clientes

    public Cliente altaCliente(String dni, String nombre, String apellidos, String email,
                               String telefono, String direccion) throws HotelesException {
        Cliente cliente = new Cliente(0, dni == null ? null : dni.trim(), nombre, apellidos, email,
                telefono, direccion);
        cliente.validar();
        if (clienteDAO.buscarPorDni(cliente.getDni()).isPresent()) {
            throw new ElementoDuplicadoException("Ya existe un cliente con el DNI " + dni + ".");
        }
        cliente.setId(clienteDAO.insertar(cliente));
        return cliente;
    }

    /** Modifica los datos del cliente; los parámetros vacíos o nulos conservan el valor actual. */
    public Cliente modificarCliente(int id, String nombre, String apellidos, String email,
                                    String telefono, String direccion) throws HotelesException {
        Cliente actual = obtenerCliente(id);
        Cliente candidato = new Cliente(id, actual.getDni(),
                conservar(nombre, actual.getNombre()), conservar(apellidos, actual.getApellidos()),
                conservar(email, actual.getEmail()), conservar(telefono, actual.getTelefono()),
                conservar(direccion, actual.getDireccion()));
        candidato.validar();
        actual.setNombre(candidato.getNombre());
        actual.setApellidos(candidato.getApellidos());
        actual.setEmail(candidato.getEmail());
        actual.setTelefono(candidato.getTelefono());
        actual.setDireccion(candidato.getDireccion());
        clienteDAO.actualizar(actual);
        return actual;
    }

    private static String conservar(String nuevo, String actual) {
        return nuevo == null || nuevo.isBlank() ? actual : nuevo.trim();
    }

    /** Elimina el cliente si no tiene reservas (la comprobación y el borrado van en una transacción). */
    public void eliminarCliente(int id) throws HotelesException {
        obtenerCliente(id);
        clienteDAO.eliminar(id);
    }

    public Cliente obtenerCliente(int id) throws HotelesException {
        return clienteDAO.buscarPorId(id)
                .orElseThrow(() -> new ElementoNoEncontradoException("el cliente", id));
    }

    public List<Cliente> listarClientes() throws HotelesException {
        return clienteDAO.listar();
    }

    /** Busca por nombre o apellidos, sin distinguir mayúsculas. */
    public List<Cliente> buscarClientes(String texto) throws HotelesException {
        return clienteDAO.buscarPorNombre(texto);
    }

    public List<Reserva> reservasDeCliente(int clienteId) throws HotelesException {
        obtenerCliente(clienteId);
        return reservaDAO.buscar(clienteId, null, null, null, null);
    }

    // ------------------------------------------------------------------ hoteles y habitaciones

    public Hotel altaHotel(String nombre, String direccion, String ciudad, String telefono,
                           String email, int estrellas) throws HotelesException {
        Hotel hotel = new Hotel(0, nombre, direccion, ciudad, telefono, email, estrellas);
        hotel.validar();
        hotel.setId(hotelDAO.insertar(hotel));
        return hotel;
    }

    public Habitacion altaHabitacion(int hotelId, String numero, int planta,
                                     CategoriaHabitacion categoria, BigDecimal precioNoche)
            throws HotelesException {
        Hotel hotel = obtenerHotel(hotelId);
        Habitacion habitacion = new Habitacion(0, numero == null ? null : numero.trim(), planta,
                categoria, precioNoche, hotel);
        habitacion.validar();
        for (Habitacion existente : hotel.getHabitaciones()) {
            if (existente.getNumero().equalsIgnoreCase(habitacion.getNumero())) {
                throw new ElementoDuplicadoException("El hotel ya tiene una habitación " + numero + ".");
            }
        }
        habitacion.setId(habitacionDAO.insertar(habitacion));
        hotel.addHabitacion(habitacion);
        return habitacion;
    }

    public Hotel obtenerHotel(int id) throws HotelesException {
        return hotelDAO.buscarPorId(id)
                .orElseThrow(() -> new ElementoNoEncontradoException("el hotel", id));
    }

    public List<Hotel> listarHoteles() throws HotelesException {
        return hotelDAO.listar();
    }

    public List<Hotel> buscarHotelesPorCiudad(String ciudad) throws HotelesException {
        return hotelDAO.buscarPorCiudad(ciudad);
    }

    public List<Habitacion> habitacionesDeHotel(int hotelId) throws HotelesException {
        return new ArrayList<>(obtenerHotel(hotelId).getHabitaciones());
    }

    public List<Habitacion> habitacionesPorCategoria(int hotelId, CategoriaHabitacion categoria)
            throws HotelesException {
        obtenerHotel(hotelId);
        return habitacionDAO.listarPorCategoria(hotelId, categoria);
    }

    /** Número de habitaciones del hotel de cada categoría (las categorías sin habitaciones salen a 0). */
    public Map<CategoriaHabitacion, Integer> contarHabitacionesPorCategoria(int hotelId)
            throws HotelesException {
        Map<CategoriaHabitacion, Integer> recuento = new EnumMap<>(CategoriaHabitacion.class);
        for (CategoriaHabitacion categoria : CategoriaHabitacion.values()) {
            recuento.put(categoria, 0);
        }
        for (Habitacion h : obtenerHotel(hotelId).getHabitaciones()) {
            recuento.merge(h.getCategoria(), 1, Integer::sum);
        }
        return recuento;
    }

    // ------------------------------------------------------------------ disponibilidad

    /**
     * Habitaciones en servicio del hotel libres en todo el intervalo [entrada, salida).
     *
     * @param numPersonas si es mayor que 0, solo se devuelven habitaciones con capacidad suficiente
     */
    public List<Habitacion> habitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida,
                                                    int numPersonas) throws HotelesException {
        validarFechas(entrada, salida);
        obtenerHotel(hotelId);
        return habitacionDAO.listarDisponibles(hotelId, entrada, salida, numPersonas);
    }

    public int contarHabitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida)
            throws HotelesException {
        return habitacionesDisponibles(hotelId, entrada, salida, 0).size();
    }

    private static void validarFechas(LocalDate entrada, LocalDate salida) throws ValidacionException {
        if (entrada == null || salida == null) {
            throw new ValidacionException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!salida.isAfter(entrada)) {
            throw new ValidacionException("La fecha de salida debe ser posterior a la de entrada.");
        }
    }

    // ------------------------------------------------------------------ reservas

    public Reserva crearReserva(int clienteId, int hotelId, LocalDate entrada, LocalDate salida,
                                int numPersonas, List<Integer> idsHabitaciones) throws HotelesException {
        Cliente cliente = obtenerCliente(clienteId);
        Hotel hotel = obtenerHotel(hotelId);
        validarFechas(entrada, salida);
        if (entrada.isBefore(LocalDate.now())) {
            throw new ValidacionException("La fecha de entrada no puede ser anterior a hoy.");
        }
        Reserva reserva = new Reserva(0, cliente, hotel, entrada, salida, numPersonas);
        for (int idHabitacion : idsHabitaciones) {
            Habitacion habitacion = habitacionDAO.buscarPorId(idHabitacion)
                    .orElseThrow(() -> new ElementoNoEncontradoException("la habitación", idHabitacion));
            if (!habitacion.isEnServicio()) {
                throw new OperacionNoPermitidaException(
                        "La habitación " + habitacion.getNumero() + " está fuera de servicio.");
            }
            reserva.addHabitacion(habitacion);
        }
        reserva.validar();
        int id = reservaDAO.crear(reserva);   // transacción: cabecera + líneas + control de solapamientos
        return obtenerReserva(id);
    }

    /**
     * Cambia fechas y número de personas de una reserva activa. Si los nuevos datos no son
     * válidos o chocan con otra reserva, la reserva queda como estaba.
     */
    public Reserva modificarReserva(int id, LocalDate entrada, LocalDate salida, int numPersonas)
            throws HotelesException {
        Reserva reserva = obtenerReserva(id);
        comprobarModificable(reserva);
        validarFechas(entrada, salida);
        reserva.setFechaEntrada(entrada);
        reserva.setFechaSalida(salida);
        reserva.setNumPersonas(numPersonas);
        reserva.validar();
        reservaDAO.modificar(id, entrada, salida, numPersonas);
        return obtenerReserva(id);
    }

    public Reserva cancelarReserva(int id) throws HotelesException {
        comprobarModificable(obtenerReserva(id));
        reservaDAO.cancelar(id);
        return obtenerReserva(id);
    }

    public Reserva finalizarReserva(int id) throws HotelesException {
        if (obtenerReserva(id).getEstado() != EstadoReserva.CONFIRMADA) {
            throw new OperacionNoPermitidaException("Solo se pueden finalizar reservas confirmadas.");
        }
        reservaDAO.finalizar(id);
        return obtenerReserva(id);
    }

    private void comprobarModificable(Reserva reserva) throws OperacionNoPermitidaException {
        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new OperacionNoPermitidaException("La reserva " + reserva.getId() + " está "
                    + reserva.getEstado() + " y no se puede modificar.");
        }
    }

    public Reserva obtenerReserva(int id) throws HotelesException {
        return reservaDAO.buscarPorId(id)
                .orElseThrow(() -> new ElementoNoEncontradoException("la reserva", id));
    }

    /**
     * Búsqueda combinada; cada criterio nulo se ignora. {@code entradaDesde} filtra las reservas que
     * entran ese día o después y {@code salidaHasta} las que salen ese día o antes.
     * El resultado va ordenado por fecha de entrada y, a igualdad, por id.
     */
    public List<Reserva> buscarReservas(Integer clienteId, Integer hotelId, LocalDate entradaDesde,
                                        LocalDate salidaHasta, EstadoReserva estado) throws HotelesException {
        return reservaDAO.buscar(clienteId, hotelId, entradaDesde, salidaHasta, estado);
    }
}
