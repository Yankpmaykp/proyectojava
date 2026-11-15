package hoteles.modelo.dao.mysql;

import hoteles.modelo.CategoriaHabitacion;
import hoteles.modelo.Habitacion;
import hoteles.modelo.Hotel;
import hoteles.modelo.HotelesException;
import hoteles.modelo.dao.HabitacionDAO;
import hoteles.util.ConexionBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO de habitaciones para MySQL. La búsqueda de disponibles se delega en un procedimiento almacenado. */
class HabitacionDAOMySQL implements HabitacionDAO {

    private static final String COLUMNAS =
            "id_habitacion, id_hotel, numero, planta, categoria, precio_noche, en_servicio";

    @Override
    public int insertar(Habitacion h) throws HotelesException {
        String sql = "INSERT INTO habitacion (id_hotel, numero, planta, categoria, precio_noche, en_servicio)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getHotel().getId());
            ps.setString(2, h.getNumero());
            ps.setInt(3, h.getPlanta());
            ps.setString(4, h.getCategoria().name());
            ps.setBigDecimal(5, h.getPrecioNoche());
            ps.setBoolean(6, h.isEnServicio());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "guardar la habitación");
        }
    }

    @Override
    public Optional<Habitacion> buscarPorId(int id) throws HotelesException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT " + COLUMNAS + " FROM habitacion WHERE id_habitacion = ?")) {
            ps.setInt(1, id);
            int idHotel;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                idHotel = rs.getInt("id_hotel");
            }
            // Se devuelve la instancia que cuelga del hotel, de modo que ambos objetos queden enlazados.
            return HotelDAOMySQL.buscarPorId(con, idHotel).flatMap(hotel -> hotel.getHabitaciones().stream()
                    .filter(h -> h.getId() == id).findFirst());
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar la habitación");
        }
    }

    @Override
    public List<Habitacion> listarPorHotel(int hotelId) throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            return HotelDAOMySQL.buscarPorId(con, hotelId)
                    .map(h -> new ArrayList<>(h.getHabitaciones())).orElseGet(ArrayList::new);
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar habitaciones");
        }
    }

    @Override
    public List<Habitacion> listarPorCategoria(int hotelId, CategoriaHabitacion categoria)
            throws HotelesException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT " + COLUMNAS
                     + " FROM habitacion WHERE id_hotel = ? AND categoria = ? ORDER BY id_habitacion")) {
            ps.setInt(1, hotelId);
            ps.setString(2, categoria.name());
            return leer(con, ps, hotelId);
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar habitaciones");
        }
    }

    @Override
    public List<Habitacion> listarDisponibles(int hotelId, LocalDate entrada, LocalDate salida,
                                              int numPersonas) throws HotelesException {
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cs = con.prepareCall("{call sp_habitaciones_disponibles(?, ?, ?, ?)}")) {
            cs.setInt(1, hotelId);
            cs.setObject(2, entrada);
            cs.setObject(3, salida);
            cs.setInt(4, numPersonas);
            return leer(con, cs, hotelId);
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "buscar habitaciones disponibles");
        }
    }

    /** Ejecuta la consulta y construye las habitaciones, enlazadas a su hotel. */
    private static List<Habitacion> leer(Connection con, PreparedStatement ps, int hotelId) throws SQLException {
        Optional<Hotel> hotel = HotelDAOMySQL.buscarPorId(con, hotelId);
        List<Habitacion> resultado = new ArrayList<>();
        if (hotel.isEmpty()) {
            return resultado;
        }
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs, hotel.get()));
            }
        }
        return resultado;
    }

    static Habitacion mapear(ResultSet rs, Hotel hotel) throws SQLException {
        Habitacion h = new Habitacion(rs.getInt("id_habitacion"), rs.getString("numero"), rs.getInt("planta"),
                CategoriaHabitacion.valueOf(rs.getString("categoria")), rs.getBigDecimal("precio_noche"), hotel);
        h.setEnServicio(rs.getBoolean("en_servicio"));
        return h;
    }
}
