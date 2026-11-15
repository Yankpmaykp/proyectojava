package hoteles.modelo.dao;

import hoteles.modelo.Cliente;
import hoteles.modelo.HotelesException;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de los clientes, independiente del motor de base de datos. */
public interface ClienteDAO {

    /** Inserta el cliente y devuelve el identificador autonumérico asignado por la BD. */
    int insertar(Cliente cliente) throws HotelesException;

    void actualizar(Cliente cliente) throws HotelesException;

    /** Elimina el cliente; falla si tiene reservas asociadas. */
    void eliminar(int id) throws HotelesException;

    Optional<Cliente> buscarPorId(int id) throws HotelesException;

    Optional<Cliente> buscarPorDni(String dni) throws HotelesException;

    List<Cliente> listar() throws HotelesException;

    /** Clientes cuyo nombre o apellidos contienen el texto, sin distinguir mayúsculas ni tildes. */
    List<Cliente> buscarPorNombre(String texto) throws HotelesException;
}
