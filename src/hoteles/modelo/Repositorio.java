package hoteles.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Colección genérica de entidades identificables, indexadas por su id.
 * Se usa un {@link TreeMap}: la búsqueda por id es O(log n) y el recorrido sale
 * siempre ordenado por identificador, sin necesidad de ordenar después.
 *
 * @param <T> tipo de entidad almacenada
 */
public class Repositorio<T extends Identificable> {

    private final String nombreEntidad;
    private final Map<Integer, T> elementos = new TreeMap<>();
    private int ultimoId = 0;

    public Repositorio(String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }

    /** Devuelve un identificador nuevo, no usado por ningún elemento. */
    public int siguienteId() {
        return ++ultimoId;
    }

    public void agregar(T elemento) throws ElementoDuplicadoException {
        if (elementos.containsKey(elemento.getId())) {
            throw new ElementoDuplicadoException("Ya existe " + nombreEntidad
                    + " con identificador " + elemento.getId() + ".");
        }
        elementos.put(elemento.getId(), elemento);
        ultimoId = Math.max(ultimoId, elemento.getId());
    }

    public T obtener(int id) throws ElementoNoEncontradoException {
        T elemento = elementos.get(id);
        if (elemento == null) {
            throw new ElementoNoEncontradoException(nombreEntidad, id);
        }
        return elemento;
    }

    public boolean existe(int id) {
        return elementos.containsKey(id);
    }

    public void eliminar(int id) throws ElementoNoEncontradoException {
        if (elementos.remove(id) == null) {
            throw new ElementoNoEncontradoException(nombreEntidad, id);
        }
    }

    /** Devuelve los elementos que cumplen el filtro, ordenados por id. */
    public List<T> buscar(Predicate<? super T> filtro) {
        List<T> resultado = new ArrayList<>();
        for (T elemento : elementos.values()) {
            if (filtro.test(elemento)) {
                resultado.add(elemento);
            }
        }
        return resultado;
    }

    /** Vista de solo lectura de todos los elementos, ordenados por id. */
    public List<T> todos() {
        return Collections.unmodifiableList(new ArrayList<>(elementos.values()));
    }

    public int size() {
        return elementos.size();
    }
}
