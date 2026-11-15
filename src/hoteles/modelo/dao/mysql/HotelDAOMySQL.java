package hoteles.modelo.dao.mysql;

import hoteles.modelo.Habitacion;
import hoteles.modelo.Hotel;
import hoteles.modelo.HotelesException;
import hoteles.modelo.dao.HotelDAO;
import hoteles.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DAO de hoteles para MySQL (consultas parametrizadas con {@link PreparedStatement}). */
class HotelDAOMySQL implements HotelDAO {

    private static final String SELECT =
            "SELECT id_hotel, nombre, direccion, ciudad, telefono, email, estrellas FROM hotel";

    @Override
    public int insertar(Hotel h) throws HotelesException {
        String sql = "INSERT INTO hotel (nombre, direccion, ciudad, telefono, email, estrellas)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getNombre());
            ps.setString(2, h.getDireccion());
            ps.setString(3, h.getCiudad());
            ps.setString(4, h.getTelefono());
            ps.setString(5, h.getEmail() == null || h.getEmail().isBlank() ? null : h.getEmail());
            ps.setInt(6, h.getEstrellas());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "guardar el hotel");
        }
    }

    @Override
    public Optional<Hotel> buscarPorId(int id) throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            return buscarPorId(con, id);
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar el hotel");
        }
    }

    /** Variante que reutiliza una conexión ya abierta (la usan los DAOs de habitaciones y reservas). */
    static Optional<Hotel> buscarPorId(Connection con, int id) throws SQLException {
        List<Hotel> hoteles = consultar(con, SELECT + " WHERE id_hotel = ?", id);
        return hoteles.isEmpty() ? Optional.empty() : Optional.of(hoteles.get(0));
    }

    @Override
    public List<Hotel> listar() throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            return consultar(con, SELECT + " ORDER BY id_hotel");
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar hoteles");
        }
    }

    @Override
    public List<Hotel> buscarPorCiudad(String ciudad) throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            return consultar(con, SELECT + " WHERE ciudad LIKE ? ORDER BY id_hotel",
                    "%" + ClienteDAOMySQL.escaparLike(ciudad.trim()) + "%");
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar hoteles");
        }
    }

    /** Ejecuta la consulta de hoteles y carga las habitaciones de todos ellos con una sola consulta más. */
    private static List<Hotel> consultar(Connection con, String sql, Object... parametros) throws SQLException {
        Map<Integer, Hotel> hoteles = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hotel h = new Hotel(rs.getInt("id_hotel"), rs.getString("nombre"),
                            rs.getString("direccion"), rs.getString("ciudad"), rs.getString("telefono"),
                            rs.getString("email"), rs.getInt("estrellas"));
                    hoteles.put(h.getId(), h);
                }
            }
        }
        cargarHabitaciones(con, hoteles);
        return new ArrayList<>(hoteles.values());
    }

    private static void cargarHabitaciones(Connection con, Map<Integer, Hotel> hoteles) throws SQLException {
        if (hoteles.isEmpty()) {
            return;
        }
        String marcas = String.join(", ", java.util.Collections.nCopies(hoteles.size(), "?"));
        String sql = "SELECT id_habitacion, id_hotel, numero, planta, categoria, precio_noche, en_servicio"
                + " FROM habitacion WHERE id_hotel IN (" + marcas + ") ORDER BY id_habitacion";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (int idHotel : hoteles.keySet()) {
                ps.setInt(i++, idHotel);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hotel hotel = hoteles.get(rs.getInt("id_hotel"));
                    Habitacion habitacion = HabitacionDAOMySQL.mapear(rs, hotel);
                    hotel.addHabitacion(habitacion);
                }
            }
        }
    }
}
