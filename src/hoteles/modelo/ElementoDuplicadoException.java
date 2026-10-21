package hoteles.modelo;

/** Se lanza al intentar registrar un elemento que ya existe (mismo id o mismo DNI). */
public class ElementoDuplicadoException extends HotelesException {

    public ElementoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
