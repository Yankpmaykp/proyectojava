package hoteles.modelo;

/**
 * Contrato común para las entidades que deben comprobar la coherencia de sus datos
 * antes de ser almacenadas o modificadas.
 */
public interface Validable {

    /**
     * Comprueba los datos del objeto.
     *
     * @throws ValidacionException con un mensaje comprensible si algún dato no es válido
     */
    void validar() throws ValidacionException;
}
