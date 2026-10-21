package hoteles.modelo;

/** Categorías de habitación con su capacidad máxima de personas. */
public enum CategoriaHabitacion {
    INDIVIDUAL(1),
    DOBLE(2),
    TRIPLE(3),
    SUITE(4);

    private final int capacidadMaxima;

    CategoriaHabitacion(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }
}
