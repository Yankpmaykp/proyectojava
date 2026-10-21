package hoteles.modelo;

import java.math.BigDecimal;

/** Habitación de un hotel. Su capacidad viene determinada por la categoría. */
public class Habitacion implements Identificable, Validable {
    private int id;
    private String numero;
    private int planta;
    private CategoriaHabitacion categoria;
    private BigDecimal precioNoche;
    private boolean enServicio = true;
    private Hotel hotel;

    public Habitacion() {
    }

    public Habitacion(int id, String numero, int planta, CategoriaHabitacion categoria,
                      BigDecimal precioNoche, Hotel hotel) {
        this.id = id;
        this.numero = numero;
        this.planta = planta;
        this.categoria = categoria;
        this.precioNoche = precioNoche;
        this.hotel = hotel;
    }

    public int getCapacidad() {
        return categoria.getCapacidadMaxima();
    }

    public boolean admitePersonas(int numPersonas) {
        return numPersonas > 0 && numPersonas <= getCapacidad();
    }

    @Override
    public void validar() throws ValidacionException {
        if (numero == null || numero.isBlank() || categoria == null) {
            throw new ValidacionException("El número y la categoría de la habitación son obligatorios.");
        }
        if (precioNoche == null || precioNoche.signum() <= 0) {
            throw new ValidacionException("El precio por noche debe ser mayor que 0.");
        }
        if (hotel == null) {
            throw new ValidacionException("La habitación debe pertenecer a un hotel.");
        }
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getPlanta() {
        return planta;
    }

    public void setPlanta(int planta) {
        this.planta = planta;
    }

    public CategoriaHabitacion getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaHabitacion categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(BigDecimal precioNoche) {
        this.precioNoche = precioNoche;
    }

    public boolean isEnServicio() {
        return enServicio;
    }

    public void setEnServicio(boolean enServicio) {
        this.enServicio = enServicio;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public String toString() {
        return "Habitacion{id=" + id + ", numero=" + numero + ", planta=" + planta
                + ", categoria=" + categoria + ", capacidad=" + getCapacidad()
                + ", precioNoche=" + precioNoche + " €, enServicio=" + enServicio
                + ", hotel=" + (hotel != null ? hotel.getNombre() : "-") + "}";
    }
}
