package hoteles.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Almacén de todos los datos de la aplicación y lógica que los modifica.
 * Los datos viven en estructuras dinámicas (colecciones) en memoria:
 * <ul>
 *   <li>{@link Repositorio} (TreeMap por id) para clientes, hoteles, habitaciones y reservas.</li>
 *   <li>{@link Set} (HashSet) para calcular las habitaciones ocupadas sin duplicados.</li>
 *   <li>{@link EnumMap} para el recuento de habitaciones por categoría.</li>
 * </ul>
 */
public class Datos {

    private static final Comparator<Reserva> ORDEN_RESERVAS =
            Comparator.comparing(Reserva::getFechaEntrada).thenComparingInt(Reserva::getId);

    private final Repositorio<Cliente> clientes = new Repositorio<>("el cliente");
    private final Repositorio<Hotel> hoteles = new Repositorio<>("el hotel");
    private final Repositorio<Habitacion> habitaciones = new Repositorio<>("la habitación");
    private final Repositorio<Reserva> reservas = new Repositorio<>("la reserva");

    // ------------------------------------------------------------------ clientes

    public Cliente altaCliente(String dni, String nombre, String apellidos, String email,
                               String telefono, String direccion) throws HotelesException {
        Cliente cliente = new Cliente(0, dni, nombre, apellidos, email, telefono, direccion);
        cliente.validar();
        if (!clientes.buscar(c -> c.getDni().equalsIgnoreCase(dni.trim())).isEmpty()) {
            throw new ElementoDuplicadoException("Ya existe un cliente con el DNI " + dni + ".");
        }
        cliente.setId(clientes.siguienteId());
        clientes.agregar(cliente);
        return cliente;
    }

    /** Modifica los datos del cliente; los parámetros vacíos o nulos conservan el valor actual. */
    public Cliente modificarCliente(int id, String nombre, String apellidos, String email,
                                    String telefono, String direccion) throws HotelesException {
        Cliente actual = clientes.obtener(id);
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
        return actual;
    }

    private static String conservar(String nuevo, String actual) {
        return nuevo == null || nuevo.isBlank() ? actual : nuevo.trim();
    }

    public void eliminarCliente(int id) throws HotelesException {
        clientes.obtener(id);
        if (!reservas.buscar(r -> r.getCliente().getId() == id).isEmpty()) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el cliente porque tiene reservas asociadas.");
        }
        clientes.eliminar(id);
    }

    public Cliente obtenerCliente(int id) throws ElementoNoEncontradoException {
        return clientes.obtener(id);
    }

    public List<Cliente> listarClientes() {
        return clientes.todos();
    }

    /** Busca por nombre o apellidos, sin distinguir mayúsculas. */
    public List<Cliente> buscarClientes(String texto) {
        String t = texto.trim().toLowerCase();
        return clientes.buscar(c -> c.getNombre().toLowerCase().contains(t)
                || c.getApellidos().toLowerCase().contains(t));
    }

    public List<Reserva> reservasDeCliente(int clienteId) throws ElementoNoEncontradoException {
        clientes.obtener(clienteId);
        List<Reserva> resultado = reservas.buscar(r -> r.getCliente().getId() == clienteId);
        resultado.sort(ORDEN_RESERVAS);
        return resultado;
    }

    // ------------------------------------------------------------------ hoteles y habitaciones

    public Hotel altaHotel(String nombre, String direccion, String ciudad, String telefono,
                           String email, int estrellas) throws HotelesException {
        Hotel hotel = new Hotel(0, nombre, direccion, ciudad, telefono, email, estrellas);
        hotel.validar();
        hotel.setId(hoteles.siguienteId());
        hoteles.agregar(hotel);
        return hotel;
    }

    public Habitacion altaHabitacion(int hotelId, String numero, int planta,
                                     CategoriaHabitacion categoria, BigDecimal precioNoche)
            throws HotelesException {
        Hotel hotel = hoteles.obtener(hotelId);
        Habitacion habitacion = new Habitacion(0, numero, planta, categoria, precioNoche, hotel);
        habitacion.validar();
        for (Habitacion existente : hotel.getHabitaciones()) {
            if (existente.getNumero().equalsIgnoreCase(numero.trim())) {
                throw new ElementoDuplicadoException("El hotel ya tiene una habitación " + numero + ".");
            }
        }
        habitacion.setId(habitaciones.siguienteId());
        habitaciones.agregar(habitacion);
        hotel.addHabitacion(habitacion);
        return habitacion;
    }

    public Hotel obtenerHotel(int id) throws ElementoNoEncontradoException {
        return hoteles.obtener(id);
    }

    public List<Hotel> listarHoteles() {
        return hoteles.todos();
    }

    public List<Hotel> buscarHotelesPorCiudad(String ciudad) {
        String t = ciudad.trim().toLowerCase();
        return hoteles.buscar(h -> h.getCiudad().toLowerCase().contains(t));
    }

    public List<Habitacion> habitacionesDeHotel(int hotelId) throws ElementoNoEncontradoException {
        return new ArrayList<>(hoteles.obtener(hotelId).getHabitaciones());
    }

    public List<Habitacion> habitacionesPorCategoria(int hotelId, CategoriaHabitacion categoria)
            throws ElementoNoEncontradoException {
        return hoteles.obtener(hotelId).getHabitacionesPorCategoria(categoria);
    }

    /** Número de habitaciones del hotel de cada categoría (las categorías sin habitaciones salen a 0). */
    public Map<CategoriaHabitacion, Integer> contarHabitacionesPorCategoria(int hotelId)
            throws ElementoNoEncontradoException {
        Map<CategoriaHabitacion, Integer> recuento = new EnumMap<>(CategoriaHabitacion.class);
        for (CategoriaHabitacion categoria : CategoriaHabitacion.values()) {
            recuento.put(categoria, 0);
        }
        for (Habitacion h : hoteles.obtener(hotelId).getHabitaciones()) {
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
        Hotel hotel = hoteles.obtener(hotelId);
        Set<Integer> ocupadas = idsOcupadas(hotel, entrada, salida, 0);
        List<Habitacion> libres = new ArrayList<>();
        for (Habitacion h : hotel.getHabitaciones()) {
            if (h.isEnServicio() && !ocupadas.contains(h.getId())
                    && (numPersonas <= 0 || h.admitePersonas(numPersonas))) {
                libres.add(h);
            }
        }
        return libres;
    }

    public int contarHabitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida)
            throws HotelesException {
        return habitacionesDisponibles(hotelId, entrada, salida, 0).size();
    }

    /** Ids de las habitaciones del hotel ocupadas en el intervalo, ignorando una reserva (0 = ninguna). */
    private Set<Integer> idsOcupadas(Hotel hotel, LocalDate entrada, LocalDate salida, int ignorarReservaId) {
        Set<Integer> ocupadas = new HashSet<>();
        List<Reserva> solapadas = reservas.buscar(r -> r.getHotel().getId() == hotel.getId()
                && r.getId() != ignorarReservaId && r.seSolapaCon(entrada, salida));
        for (Reserva reserva : solapadas) {
            for (LineaReserva linea : reserva.getLineas()) {
                ocupadas.add(linea.getHabitacion().getId());
            }
        }
        return ocupadas;
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
        Cliente cliente = clientes.obtener(clienteId);
        Hotel hotel = hoteles.obtener(hotelId);
        validarFechas(entrada, salida);
        if (entrada.isBefore(LocalDate.now())) {
            throw new ValidacionException("La fecha de entrada no puede ser anterior a hoy.");
        }
        Reserva reserva = new Reserva(0, cliente, hotel, entrada, salida, numPersonas);
        for (int idHabitacion : idsHabitaciones) {
            Habitacion habitacion = habitaciones.obtener(idHabitacion);
            if (!habitacion.isEnServicio()) {
                throw new OperacionNoPermitidaException(
                        "La habitación " + habitacion.getNumero() + " está fuera de servicio.");
            }
            reserva.addHabitacion(habitacion);
        }
        reserva.validar();
        comprobarDisponibilidad(reserva, 0);
        reserva.setId(reservas.siguienteId());
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservas.agregar(reserva);
        return reserva;
    }

    /**
     * Cambia fechas y número de personas de una reserva activa. Si los nuevos datos no son
     * válidos o chocan con otra reserva, la reserva queda como estaba.
     */
    public Reserva modificarReserva(int id, LocalDate entrada, LocalDate salida, int numPersonas)
            throws HotelesException {
        Reserva reserva = reservas.obtener(id);
        comprobarModificable(reserva);
        LocalDate entradaAnterior = reserva.getFechaEntrada();
        LocalDate salidaAnterior = reserva.getFechaSalida();
        int personasAnteriores = reserva.getNumPersonas();
        reserva.setFechaEntrada(entrada);
        reserva.setFechaSalida(salida);
        reserva.setNumPersonas(numPersonas);
        try {
            validarFechas(entrada, salida);
            reserva.validar();
            comprobarDisponibilidad(reserva, id);
        } catch (HotelesException e) {
            reserva.setFechaEntrada(entradaAnterior);
            reserva.setFechaSalida(salidaAnterior);
            reserva.setNumPersonas(personasAnteriores);
            throw e;
        }
        return reserva;
    }

    public Reserva cancelarReserva(int id) throws HotelesException {
        Reserva reserva = reservas.obtener(id);
        comprobarModificable(reserva);
        reserva.cancelar();
        return reserva;
    }

    public Reserva finalizarReserva(int id) throws HotelesException {
        Reserva reserva = reservas.obtener(id);
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new OperacionNoPermitidaException("Solo se pueden finalizar reservas confirmadas.");
        }
        reserva.setEstado(EstadoReserva.FINALIZADA);
        return reserva;
    }

    private void comprobarModificable(Reserva reserva) throws OperacionNoPermitidaException {
        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new OperacionNoPermitidaException("La reserva " + reserva.getId() + " está "
                    + reserva.getEstado() + " y no se puede modificar.");
        }
    }

    private void comprobarDisponibilidad(Reserva reserva, int ignorarReservaId)
            throws HabitacionNoDisponibleException {
        Set<Integer> ocupadas = idsOcupadas(reserva.getHotel(), reserva.getFechaEntrada(),
                reserva.getFechaSalida(), ignorarReservaId);
        for (LineaReserva linea : reserva.getLineas()) {
            if (ocupadas.contains(linea.getHabitacion().getId())) {
                throw new HabitacionNoDisponibleException(linea.getHabitacion(),
                        reserva.getFechaEntrada(), reserva.getFechaSalida());
            }
        }
    }

    public Reserva obtenerReserva(int id) throws ElementoNoEncontradoException {
        return reservas.obtener(id);
    }

    /**
     * Búsqueda combinada; cada criterio nulo se ignora. {@code entradaDesde} filtra las reservas que
     * entran ese día o después y {@code salidaHasta} las que salen ese día o antes.
     * El resultado va ordenado por fecha de entrada y, a igualdad, por id.
     */
    public List<Reserva> buscarReservas(Integer clienteId, Integer hotelId, LocalDate entradaDesde,
                                        LocalDate salidaHasta, EstadoReserva estado) {
        List<Reserva> resultado = reservas.buscar(r ->
                (clienteId == null || r.getCliente().getId() == clienteId)
                        && (hotelId == null || r.getHotel().getId() == hotelId)
                        && (entradaDesde == null || !r.getFechaEntrada().isBefore(entradaDesde))
                        && (salidaHasta == null || !r.getFechaSalida().isAfter(salidaHasta))
                        && (estado == null || r.getEstado() == estado));
        resultado.sort(ORDEN_RESERVAS);
        return resultado;
    }

    // ------------------------------------------------------------------ datos de ejemplo

    /** Carga unos datos de ejemplo para poder probar la aplicación sin teclear todo a mano. */
    public void cargarDatosDemo() {
        try {
            Hotel mediterraneo = altaHotel("Hotel Mediterráneo", "Av. del Mar 10", "Valencia",
                    "961234567", "info@mediterraneo.com", 4);
            altaHabitacion(mediterraneo.getId(), "101", 1, CategoriaHabitacion.INDIVIDUAL, new BigDecimal("55.00"));
            altaHabitacion(mediterraneo.getId(), "102", 1, CategoriaHabitacion.DOBLE, new BigDecimal("80.00"));
            altaHabitacion(mediterraneo.getId(), "201", 2, CategoriaHabitacion.TRIPLE, new BigDecimal("105.00"));
            altaHabitacion(mediterraneo.getId(), "301", 3, CategoriaHabitacion.SUITE, new BigDecimal("190.00"));

            Hotel castilla = altaHotel("Gran Hotel Castilla", "Plaza Mayor 3", "Madrid",
                    "915551234", "reservas@castilla.com", 5);
            altaHabitacion(castilla.getId(), "10", 1, CategoriaHabitacion.DOBLE, new BigDecimal("120.00"));
            altaHabitacion(castilla.getId(), "11", 1, CategoriaHabitacion.DOBLE, new BigDecimal("120.00"));
            altaHabitacion(castilla.getId(), "20", 2, CategoriaHabitacion.SUITE, new BigDecimal("260.00"));

            Cliente ana = altaCliente("12345678Z", "Ana", "García López", "ana.garcia@email.com",
                    "600111222", "C/ Mayor 5, Madrid");
            Cliente luis = altaCliente("87654321X", "Luis", "Pérez Ruiz", "luis.perez@email.com",
                    "600333444", "C/ Sol 12, Valencia");
            altaCliente("11223344B", "Marta", "García Sanz", "marta.garcia@email.com",
                    "600555666", "Av. Libertad 8, Sevilla");

            LocalDate hoy = LocalDate.now();
            List<Habitacion> hMed = mediterraneo.getHabitaciones();
            crearReserva(ana.getId(), mediterraneo.getId(), hoy.plusDays(10), hoy.plusDays(13), 3,
                    List.of(hMed.get(1).getId(), hMed.get(0).getId()));
            crearReserva(luis.getId(), castilla.getId(), hoy.plusDays(20), hoy.plusDays(22), 2,
                    List.of(castilla.getHabitaciones().get(0).getId()));
        } catch (HotelesException e) {
            throw new IllegalStateException("Los datos de ejemplo no son válidos: " + e.getMessage(), e);
        }
    }
}
