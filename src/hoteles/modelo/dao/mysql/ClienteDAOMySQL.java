package hoteles.modelo.dao.mysql;

import hoteles.modelo.Cliente;
import hoteles.modelo.HotelesException;
import hoteles.modelo.dao.ClienteDAO;
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

/**
 * DAO de clientes para MySQL. Todas las consultas usan {@link PreparedStatement} con parámetros
 * ({@code ?}): los valores del usuario nunca se concatenan en el SQL, lo que impide la inyección SQL.
 */
class ClienteDAOMySQL implements ClienteDAO {

    private static final String COLUMNAS =
            "id_cliente, dni, nombre, apellidos, email, telefono, direccion, fecha_registro";

    @Override
    public int insertar(Cliente c) throws HotelesException {
        String sql = "INSERT INTO cliente (dni, nombre, apellidos, email, telefono, direccion, fecha_registro)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getApellidos());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getDireccion());
            ps.setObject(7, c.getFechaRegistro());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "guardar el cliente");
        }
    }

    @Override
    public void actualizar(Cliente c) throws HotelesException {
        String sql = "UPDATE cliente SET nombre = ?, apellidos = ?, email = ?, telefono = ?, direccion = ?"
                + " WHERE id_cliente = ?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "modificar el cliente");
        }
    }

    @Override
    public void eliminar(int id) throws HotelesException {
        Transaccion.ejecutar("eliminar el cliente", con -> {
            try (CallableStatement cs = con.prepareCall("{call sp_eliminar_cliente(?)}")) {
                cs.setInt(1, id);
                cs.execute();
            }
            return null;
        });
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) throws HotelesException {
        try (Connection con = ConexionBD.getConexion()) {
            return buscarPorId(con, id);
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar el cliente");
        }
    }

    /** Variante que reutiliza una conexión ya abierta (la usa el DAO de reservas). */
    static Optional<Cliente> buscarPorId(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT " + COLUMNAS + " FROM cliente WHERE id_cliente = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Cliente> buscarPorDni(String dni) throws HotelesException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT " + COLUMNAS + " FROM cliente WHERE dni = ?")) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar el cliente");
        }
    }

    @Override
    public List<Cliente> listar() throws HotelesException {
        return consultar("SELECT " + COLUMNAS + " FROM cliente ORDER BY id_cliente");
    }

    @Override
    public List<Cliente> buscarPorNombre(String texto) throws HotelesException {
        String patron = "%" + escaparLike(texto.trim()) + "%";
        return consultar("SELECT " + COLUMNAS + " FROM cliente WHERE nombre LIKE ? OR apellidos LIKE ?"
                + " ORDER BY id_cliente", patron, patron);
    }

    private List<Cliente> consultar(String sql, String... parametros) throws HotelesException {
        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setString(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
                return clientes;
            }
        } catch (SQLException e) {
            throw TraductorErrores.traducir(e, "consultar clientes");
        }
    }

    private static Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(rs.getInt("id_cliente"), rs.getString("dni"), rs.getString("nombre"),
                rs.getString("apellidos"), rs.getString("email"), rs.getString("telefono"),
                rs.getString("direccion"));
        c.setFechaRegistro(rs.getObject("fecha_registro", LocalDate.class));
        return c;
    }

    /** Evita que los comodines % y _ escritos por el usuario se interpreten como patrón. */
    static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
