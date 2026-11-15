package hoteles.controlador;

import hoteles.modelo.Cliente;
import hoteles.modelo.Datos;
import hoteles.modelo.HotelesException;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de clientes: recibe datos de la vista, invoca a {@link Datos} y devuelve texto
 * formateado, de modo que la vista nunca maneja clases del modelo.
 */
public class ClienteController {

    private final Datos datos;

    public ClienteController(Datos datos) {
        this.datos = datos;
    }

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

    public List<String> listarClientes() throws HotelesException {
        return formatoClientes(datos.listarClientes());
    }

    public List<String> buscarClientes(String texto) throws HotelesException {
        return formatoClientes(datos.buscarClientes(texto));
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
}
