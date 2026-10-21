package hoteles.modelo;

/** Se lanza cuando se busca por identificador un elemento que no existe. */
public class ElementoNoEncontradoException extends HotelesException {

    public ElementoNoEncontradoException(String entidad, int id) {
        super("No existe " + entidad + " con identificador " + id + ".");
    }
}
