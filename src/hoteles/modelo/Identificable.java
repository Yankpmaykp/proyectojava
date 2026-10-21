package hoteles.modelo;

/**
 * Contrato común para todas las entidades que tienen un identificador único.
 * Facilitará las búsquedas en colecciones (Fase 2) y la implementación de los DAO (Fase 3).
 */
public interface Identificable {
    int getId();
}
