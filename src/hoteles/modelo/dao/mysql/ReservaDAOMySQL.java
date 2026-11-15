package hoteles.modelo.dao.mysql;

import hoteles.modelo.Cliente;
import hoteles.modelo.EstadoReserva;
import hoteles.modelo.Habitacion;
import hoteles.modelo.Hotel;
import hoteles.modelo.HotelesException;
import hoteles.modelo.LineaReserva;
import hoteles.modelo.Reserva;
import hoteles.modelo.dao.ReservaDAO;
import hoteles.util.ConexionBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO de reservas para MySQL. Las operaciones que modifican datos se ejecutan dentro de una
 * {@link Transaccion} y llaman a procedimientos almacenados con {@link CallableStatement}.
 */
class ReservaDAOMySQL implements ReservaDAO {

    private static final String SELECT = "SELECT id_reserva, id_cliente, id_hotel, fecha_entrada,"
            + " fecha_salida, num_personas, estado, fecha_creacion FROM reserva";

    /**
     * Transacción única: cabecera + una llamada por habitación. Si cualquier línea falla (por
     * ejemplo, habitación ocupada) se hace rollback y no queda ni la cabecera de la reserva.
     */
    @Override
    public int crear(Reserva r) throws HotelesException {
        return Transaccion.ejecutar("crear la reserva", con -> {
            int id;
            try (CallableStatement cs = con.prepareCall("{call sp_crear_reserva(?, ?, ?, ?, ?, ?)}")) {
                cs.setInt(1, r.getCliente().getId());
                cs.setInt(2, r.getHotel().getId());
                cs.setObject(3, r.getFechaEntrada());
                cs.setObject(4, r.getFechaSalida());
                cs.setInt(5, r.getNumPersonas());
                cs.registerOutParameter(6, Types.INTEGER);
                cs.execute();
                id = cs.getInt(6);
            }
            try (CallableStatement cs = con.prepareCall("{call sp_agregar_linea_reserva(?, ?)}")) {
                for (LineaReserva linea : r.getLineas()) {
                    cs.setInt(1, id);
                    cs.setInt(2, linea.getHabitacion().getId());
                    cs.execute();
                }
            }
            return id;
        });
    }

    @Override
    public void modificar(int id, LocalDate entrada, LocalDate salida, int numPersonas) throws HotelesException {
        Transaccion.ejecutar("modificar la reserva", con -> {
            try (CallableStatement cs = con.prepareCall("{call sp_modificar_reserva(?, ?, ?, ?)}")) {
                cs.setInt(1, id);
                cs.setObject(2, entrada);
                cs.setObject(3, salida);
                cs.setInt(4, numPersonas);
                cs.execute();
            }
            return null;
        });
    }

    @Override
    public void cancelar(int id) throws HotelesException {
        cambiarEstado(id, EstadoReserva.CANCELADA);
    }

    @Override
    public void finalizar(int id) throws HotelesException {
        cambiarEstado(id, EstadoReserva.FINALIZADA);
    }

    private void cambiarEstado(int id, EstadoReserva nuevo) throws HotelesException {
        Transaccion.ejecutar("cambiar el estado de la reserva", con -> {
            try (CallableStatement cs = con.prepareCall("{call sp_cambiar_estado_reserva(?, ?)}")) {
                cs.setInt(1, id);
                cs.setString(2, nuevo.name());
                cs.execute();
            }
            return null;
        });
    }

    @Override
    public Optional<Reserva> buscarPorId(int id) throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            List<Reserva> reservas = consultar(con, SELECT + " WHERE id_reserva = ?", List.of(id));
            return reservas.isEmpty() ? Optional.empty() : Optional.of(reservas.get(0));
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar la reserva");
        }
    }

    /**
     * El WHERE se compone solo con fragmentos fijos del programa; los valores del usuario viajan
     * siempre como parámetros ({@code ?}), nunca dentro del texto SQL.
     */
    @Override
    public List<Reserva> buscar(Integer clienteId, Integer hotelId, LocalDate entradaDesde,
                                LocalDate salidaHasta, EstadoReserva estado) throws HotelesException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (clienteId != null) {
            sql.append(" AND id_cliente = ?");
            parametros.add(clienteId);
        }
        if (hotelId != null) {
            sql.append(" AND id_hotel = ?");
            parametros.add(hotelId);
        }
        if (entradaDesde != null) {
            sql.append(" AND fecha_entrada >= ?");
            parametros.add(entradaDesde);
        }
        if (salidaHasta != null) {
            sql.append(" AND fecha_salida <= ?");
            parametros.add(salidaHasta);
        }
        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado.name());
        }
        sql.append(" ORDER BY fecha_entrada, id_reserva");
        try (Connection con = ConexionBD.getConexion()) {
            return consultar(con, sql.toString(), parametros);
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar reservas");
        }
    }

    /** Ejecuta la consulta de reservas y completa cliente, hotel y líneas de cada una. */
    private static List<Reserva> consultar(Connection con, String sql, List<Object> parametros)
            throws SQLException {
        Map<Integer, Reserva> reservas = new LinkedHashMap<>();
        Map<Integer, Cliente> clientes = new HashMap<>();
        Map<Integer, Hotel> hoteles = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idCliente = rs.getInt("id_cliente");
                    int idHotel = rs.getInt("id_hotel");
                    Cliente cliente = clientes.get(idCliente);
                    if (cliente == null) {
                        cliente = ClienteDAOMySQL.buscarPorId(con, idCliente).orElseThrow();
                        clientes.put(idCliente, cliente);
                    }
                    Hotel hotel = hoteles.get(idHotel);
                    if (hotel == null) {
                        hotel = HotelDAOMySQL.buscarPorId(con, idHotel).orElseThrow();
                        hoteles.put(idHotel, hotel);
                    }
                    Reserva r = new Reserva(rs.getInt("id_reserva"), cliente, hotel,
                            rs.getObject("fecha_entrada", LocalDate.class),
                            rs.getObject("fecha_salida", LocalDate.class), rs.getInt("num_personas"));
                    r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
                    r.setFechaCreacion(rs.getObject("fecha_creacion", LocalDateTime.class));
                    reservas.put(r.getId(), r);
                }
            }
        }
        cargarLineas(con, reservas);
        return new ArrayList<>(reservas.values());
    }

    private static void cargarLineas(Connection con, Map<Integer, Reserva> reservas) throws SQLException {
        if (reservas.isEmpty()) {
            return;
        }
        String marcas = String.join(", ", Collections.nCopies(reservas.size(), "?"));
        String sql = "SELECT id_reserva, id_habitacion, precio_noche FROM linea_reserva"
                + " WHERE id_reserva IN (" + marcas + ") ORDER BY id_reserva, id_habitacion";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (int idReserva : reservas.keySet()) {
                ps.setInt(i++, idReserva);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = reservas.get(rs.getInt("id_reserva"));
                    int idHabitacion = rs.getInt("id_habitacion");
                    Habitacion habitacion = r.getHotel().getHabitaciones().stream()
                            .filter(h -> h.getId() == idHabitacion).findFirst().orElseThrow();
                    LineaReserva linea = new LineaReserva(habitacion);
                    linea.setPrecioNoche(rs.getBigDecimal("precio_noche"));   // precio congelado al reservar
                    r.getLineas().add(linea);
                }
            }
        }
    }
}
