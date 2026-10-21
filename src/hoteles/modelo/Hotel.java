package hoteles.modelo;

import java.util.ArrayList;
import java.util.List;

/** Hotel de la cadena. Es el propietario (composición) de sus habitaciones. */
public class Hotel implements Identificable, Validable {

    private int id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private String email;
    private int estrellas;
    private List<Habitacion> habitaciones = new ArrayList<>();

    public Hotel() {
    }

    public Hotel(int id, String nombre, String direccion, String ciudad, String telefono,
                 String email, int estrellas) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.email = email;
        this.estrellas = estrellas;
    }

    /** Añade una habitación al hotel y fija la referencia inversa. */
    public void addHabitacion(Habitacion habitacion) {
        habitacion.setHotel(this);
        habitaciones.add(habitacion);
    }

    public List<Habitacion> getHabitacionesPorCategoria(CategoriaHabitacion categoria) {
        List<Habitacion> resultado = new ArrayList<>();
        for (Habitacion h : habitaciones) {
            if (h.getCategoria() == categoria) {
                resultado.add(h);
            }
        }
        return resultado;
    }

    public int getNumHabitaciones() {
        return habitaciones.size();
    }

    @Override
    public void validar() throws ValidacionException {
        if (nombre == null || nombre.isBlank() || ciudad == null || ciudad.isBlank()) {
            throw new ValidacionException("El nombre y la ciudad del hotel son obligatorios.");
        }
        if (estrellas < 1 || estrellas > 5) {
            throw new ValidacionException("El número de estrellas debe estar entre 1 y 5.");
        }
        if (email != null && !email.isBlank() && !Persona.esEmailValido(email)) {
            throw new ValidacionException("El email del hotel no tiene un formato válido.");
        }
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(int estrellas) {
        this.estrellas = estrellas;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

    @Override
    public String toString() {
        return "Hotel{id=" + id + ", nombre=" + nombre + ", direccion=" + direccion
                + ", ciudad=" + ciudad + ", telefono=" + telefono + ", email=" + email
                + ", estrellas=" + estrellas + ", habitaciones=" + habitaciones.size() + "}";
    }
}
